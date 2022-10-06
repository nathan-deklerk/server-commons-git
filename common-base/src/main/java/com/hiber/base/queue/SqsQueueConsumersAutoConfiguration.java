package com.hiber.base.queue;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiber.base.domain.Support;
import com.hiber.base.integration.aws.sqs.ExponentialBackoffPolicySqsMessageChannelInterceptor;
import com.hiber.base.integration.aws.sqs.ChangingHeadersSqsMessageDrivenChannelAdapter;
import com.hiber.base.integration.jackson.Jackson2JsonToObjectTransformer;
import com.hiber.base.integration.notify.SupportNotifyExceptionChannelInterceptor;
import com.hiber.base.integration.validation.Jsr303ValidationSelector;
import com.hiber.base.queue.SqsQueueConsumersAutoConfiguration.QueueConsumer;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.aws.support.AwsHeaders;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.RouterSpec;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.router.ExpressionEvaluatingRouter;
import org.springframework.messaging.MessageChannel;

import static com.hiber.base.queue.SqsQueueConsumersAutoConfiguration.QueueConsumer.BackoffPolicy.CONFIGURED;
import static com.hiber.base.queue.SqsQueueConsumersAutoConfiguration.QueueConsumer.BackoffPolicy.EXPONENTIAL;

/**
 * Auto-configuration that sets up necessary Spring Integration and SQS infrastructure classes so requested SQS queues are polled
 * and incoming messages routed to consumers.
 * <p/>
 * The auto-configuration is activated by one or more beans of the <code>QueueCustomer</code> class. It can be explicitly disabled
 * by setting <code>hiber.sqs.consumer-enabled</code> to <code>true</code>.
 * <p/>
 * This implementation creates a single poller for all queues. It is possible to have an independently configured poller per a
 * queue, but it requires a slightly different way to create necessary Spring Integration infrastructure.
 * <p/>
 * Configured Spring Integration channels and handlers obtain default support for metrics and distributed tracing.
 */
@Configuration
@Import(SqsClientConfiguration.class)
@ConditionalOnBean(QueueConsumer.class)
@EnableConfigurationProperties(SqsBaseSettings.class)
@ConditionalOnProperty(value = "hiber.sqs.consumer-enabled", havingValue = "true", matchIfMissing = true)
public class SqsQueueConsumersAutoConfiguration {
	/**
	 * Creates a component that will be polling requested SQS queues. This is the first step in the overall flow, received
	 * messages will be sent to <code>rawReceiveMessageChannel</code>.
	 *
	 * @param amazonSqs A SQS client.
	 * @param queuesConsumers Consumers defining which queues should be polled.
	 * @param rawReceiveMessageChannel The channel that will receive messages obtained from SQS queues.
	 *
	 * @return Polling component.
	 */
	@Bean
	public MessageProducerSupport sqsMessageProducerSupport(
			final AmazonSQSAsync amazonSqs,
			final List<QueueConsumer<?>> queuesConsumers,
			final MessageChannel rawReceiveMessageChannel,
			final SqsBaseSettings sqsBaseSettings
	) {
		final String[] urls = queuesConsumers.stream()
				.map(q -> q.url)
				.collect(Collectors.toList())
				.toArray(new String[queuesConsumers.size()]);

		final ChangingHeadersSqsMessageDrivenChannelAdapter adapter = new ChangingHeadersSqsMessageDrivenChannelAdapter(
				amazonSqs,
				urls
		);
		adapter.setWaitTimeOut(sqsBaseSettings.getWaitTimeOut());
		adapter.setMaxNumberOfMessages(2);
		adapter.setMessageDeletionPolicy(SqsMessageDeletionPolicy.ON_SUCCESS);
		adapter.setOutputChannel(rawReceiveMessageChannel);
		return adapter;
	}

	/**
	 * A channel that will receive messages from SQS queues. The channel server two purposes.
	 * <p/>
	 * First, it serves as a top-level handler for any exceptions occurring from that point. For example any exception that
	 * happens during de-serialisation from JSON or exceptions thrown by consumers of messages will be handled here and forwarded
	 * to any support notifiers (e.g. Bugsnag).
	 * <p/>
	 * Secondly, as it is an explicitly named bean it can be used to hook-up various interceptors useful during testing.
	 *
	 * @return A channel for incoming messages.
	 */
	@Bean
	public DirectChannel rawReceiveMessageChannel(
			final Support support
	) {
		final DirectChannel channel = MessageChannels.direct().get();
		channel.addInterceptor(new SupportNotifyExceptionChannelInterceptor(Set.of(channel), support));
		return channel;
	}

	/**
	 * Creates an integration flow that routes incoming messages from the <code>rawReceiveMessageChannel</code> channel to a
	 * proper consumer. The messages are converted from JSON format to appropriate objects. If DTO contains any validation
	 * annotations they will be checked.
	 *
	 * @param queuesConsumers Consumers defining which queues should be polled.
	 * @param rawReceiveMessageChannel A channel where messages from SQS are forwarded.
	 * @param validator A configured JSR 303 validator,
	 * @param objectMapper A configured Jackson object mapper instance that will be used to convert JSON to DTO.
	 *
	 * @return Created integrated flow.
	 */
	@Bean
	IntegrationFlow sqsRouter(
			final List<QueueConsumer<?>> queuesConsumers,
			final DirectChannel rawReceiveMessageChannel,
			final Validator validator,
			final ObjectMapper objectMapper,
			final AmazonSQS amazonSQS,
			final SqsBaseSettings sqsBaseSettings
	) {
		final Consumer<RouterSpec<Object, ExpressionEvaluatingRouter>> routerSpecConsumer = router -> {
			for (final QueueConsumer consumer : queuesConsumers) {
				final DirectChannel channel = MessageChannels.direct().get();
				channel.setComponentName(consumer.name);
				channel.subscribe(message -> consumer.consumer.accept(message.getPayload()));

				if (EXPONENTIAL == consumer.getBackoffPolicy())
					channel.addInterceptor(new ExponentialBackoffPolicySqsMessageChannelInterceptor(amazonSQS, sqsBaseSettings));

				final IntegrationFlow flow = f -> f
						.transform(new Jackson2JsonToObjectTransformer(consumer.clazz, objectMapper))
						.filter(new Jsr303ValidationSelector(validator))
						.channel(channel);
				router.subFlowMapping(consumer.url, flow);
			}
		};

		final String queueHeaderExpression = "headers." + AwsHeaders.RECEIVED_QUEUE;

		return IntegrationFlows
				.from(rawReceiveMessageChannel)
				.route(queueHeaderExpression, routerSpecConsumer)
				.get();
	}

	/**
	 * Describes a SQS queue consumer.
	 *
	 * @param <T> The class of objects accepted by the consumer.
	 */
	@Value
	@AllArgsConstructor
	public static class QueueConsumer<T> {
		/**
		 * The URL of the queue available in SQS.
		 */
		String url;

		/**
		 * Provides a consumer for objects that were received from the queue.
		 */
		Consumer<T> consumer;

		/**
		 * The class of objects accepted by the consumer.
		 */
		Class<T> clazz;

		/**
		 * A text representation for the queue. This is a free-form value that will be assigned to a Spring Integration
		 * channel created specifically for this consumer. The name will be reflected in metrics collected by Spring
		 * Integration and tracing information when send to Zipkin.
		 */
		String name;

		/**
		 * Back-off strategy that should be applied when problems with consuming message occurs.
		 */
		BackoffPolicy backoffPolicy;

		public QueueConsumer(final String url, final Consumer<T> consumer, final Class<T> clazz, final String name) {
			this.url = url;
			this.consumer = consumer;
			this.clazz = clazz;
			this.name = name;
			this.backoffPolicy = BackoffPolicy.CONFIGURED;
		}

		public enum BackoffPolicy {
			/**
			 * CONFIGURED means that attempts to deliver/consume a message will
			 * happen according to the configuration of the queue at AWS level
			 * (for the time being it is a constant, configurable per-queue interval)
			 */
			CONFIGURED,

			/**
			 * EXPONENTIAL means that time between attempts to deliver/consume
			 * a message will increase exponentially.
			 *
			 * For ex.
			 * 1st attempt after fail starts after 1 min.
			 * 2nd attempt after fail starts after 2 min.
			 * 3nd attempt after fail starts after 4 min.
			 */
			EXPONENTIAL
		}
	}
}