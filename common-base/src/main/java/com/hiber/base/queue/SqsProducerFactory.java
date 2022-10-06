package com.hiber.base.queue;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiber.base.integration.aws.sqs.ChangingHeadersSqsMessageHandler;
import com.hiber.base.integration.jackson.Jackson2ObjectToJsonTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.messaging.MessageChannel;

/**
 * A factory simplifying creation of Spring Integration channels allowing to send a message to an SQS queue.
 */
@RequiredArgsConstructor
public class SqsProducerFactory {
	private final ObjectMapper objectMapper;
	private final AmazonSQSAsync amazonSqs;
	private final IntegrationFlowContext integrationFlowContext;

	/**
	 * Creates a message channel allowing to send a message to a SQS queue.
	 *
	 * @param queueUrl The queue URL.
	 *
	 * @return Created message channel.
	 */
	public MessageChannel create(final String queueUrl) {
		final DirectChannel channel = MessageChannels.direct().get();

		final ChangingHeadersSqsMessageHandler handler = new ChangingHeadersSqsMessageHandler(amazonSqs);
		handler.setSync(true);
		handler.setQueue(queueUrl);

		final StandardIntegrationFlow integrationFlow = IntegrationFlows.from(channel)
				.transform(new Jackson2ObjectToJsonTransformer(objectMapper))
				.handle(handler)
				.get();
		integrationFlowContext.registration(integrationFlow).autoStartup(true).register();

		return channel;
	}

	/**
	 * Creates a message channel allowing to send a message to a FIFO SQS queue.
	 *
	 * @param queueUrl The queue URL.
	 * @param messageGroupId Message group id SQS header.
	 *
	 * @return Created message channel.
	 */
	public MessageChannel create(final String queueUrl, final String messageGroupId) {
		final DirectChannel channel = MessageChannels.direct().get();

		final ChangingHeadersSqsMessageHandler handler = new ChangingHeadersSqsMessageHandler(amazonSqs);
		handler.setSync(true);
		handler.setQueue(queueUrl);
		handler.setMessageGroupId(messageGroupId);

		final StandardIntegrationFlow integrationFlow = IntegrationFlows.from(channel)
				.transform(new Jackson2ObjectToJsonTransformer(objectMapper))
				.handle(handler)
				.get();
		integrationFlowContext.registration(integrationFlow).autoStartup(true).register();

		return channel;
	}
}