package com.hiber.base.integration.aws.sqs;


import com.amazonaws.services.sqs.AmazonSQS;
import com.hiber.base.queue.SqsBaseSettings;
import java.util.Optional;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

/**
 * Exponential back-off policy.
 * Visibility timeout calculated on demand.
 */
public class ExponentialBackoffPolicySqsMessageChannelInterceptor extends VisibilityTimeoutCalculator
		implements ChannelInterceptor {
	private final AmazonSQS amazonSQS;

	public ExponentialBackoffPolicySqsMessageChannelInterceptor(
			final AmazonSQS amazonSQS,
			final SqsBaseSettings sqsBaseSettings
	) {
		super(sqsBaseSettings.getExponentialBackOffPolicyMinVisibilityTimeout(), sqsBaseSettings.getMaxSQSVisibilityTimeout());
		this.amazonSQS = amazonSQS;
	}

	@Override
	public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
		if (ex != null) {
			final String queueUrl = getQueueUrl(message);
			final String receiptHandler = getReceiptHandler(message);
			final Optional<Integer> maybeApproximateReceiveCount = getNumberOfAttempts(message);

			maybeApproximateReceiveCount.ifPresent(approximateReceiveCount -> {
				final int nextVisibilityTimeout = getNextVisibilityTimeout(approximateReceiveCount);

				amazonSQS.changeMessageVisibility(queueUrl, receiptHandler, nextVisibilityTimeout);
			});
		}
	}

	private Optional<Integer> getNumberOfAttempts(final Message<?> message) {
		return Optional.ofNullable(message.getHeaders().get("ApproximateReceiveCount", String.class)).map(Integer::parseInt);
	}

	private  String getReceiptHandler(final Message<?> message) {
		return message.getHeaders().get("aws_receiptHandle", String.class);
	}

	private String getQueueUrl(final Message<?> message) {
		return message.getHeaders().get("aws_receivedQueue", String.class);
	}

}