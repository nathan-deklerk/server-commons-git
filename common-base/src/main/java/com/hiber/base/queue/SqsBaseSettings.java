package com.hiber.base.queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Base configuration items related to integration with SQS.
 */
@Data
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("hiber.sqs")
public class SqsBaseSettings {
	/**
	 * A wait time used when polling for messages from SQS queues. See https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-short-and-long-polling.html
	 * for details.
	 */
	private int waitTimeOut = 20;

	/**
	 * Max value in seconds of visibility timeout parameter.
	 * 12 hours is max value in SQS (see docs - link above).
	 */
	private int maxSQSVisibilityTimeout = 43200;

	/**
	 * Parameter used in calculations in exponential back-off policy.
	 */
	private int exponentialBackOffPolicyMinVisibilityTimeout = 60;
}