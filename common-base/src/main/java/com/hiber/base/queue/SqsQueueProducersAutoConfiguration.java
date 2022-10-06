package com.hiber.base.queue;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.messaging.MessageChannel;

/**
 * An auto-configuration that provides a factory allowing to create SQS producers in form of @{link {@link MessageChannel}}
 * instances.
 */
@Configuration
@Import(SqsClientConfiguration.class)
@ConditionalOnClass(name = "org.springframework.integration.aws.outbound.SqsMessageHandler")
public class SqsQueueProducersAutoConfiguration {
	@Bean
	public SqsProducerFactory sqsProducerFactory(
			final ObjectMapper objectMapper,
			final AmazonSQSAsync nonBufferedAmazonSqs,
			final IntegrationFlowContext integrationFlowContext
	) {
		return new SqsProducerFactory(objectMapper, nonBufferedAmazonSqs, integrationFlowContext);
	}
}