package com.hiber.base.integration.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import java.util.Collection;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.aws.inbound.SqsMessageDrivenChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Adapter that additionally change headers of sending messages. This implementation can change header with {@link String}
 * value to header with {@link Boolean} value.
 * Default collection for changing from string to boolean header is {@link ChangingHeadersDefaults#DEFAULT_BOOLEAN_HEADERS}.
 *
 * @see SqsMessageDrivenChannelAdapter
 */
@Slf4j
public class ChangingHeadersSqsMessageDrivenChannelAdapter extends SqsMessageDrivenChannelAdapter
		implements ChangingHeadersDefaults {
	/**
	 * Collection of headers to change from string to boolean value (as sqs not handling other types in headers).
	 * Defaults to {@link ChangingHeadersDefaults#DEFAULT_BOOLEAN_HEADERS}.
	 */
	@Setter
	private Collection<String> stringToBooleanHeaders = DEFAULT_BOOLEAN_HEADERS;

	public ChangingHeadersSqsMessageDrivenChannelAdapter(AmazonSQSAsync amazonSqs, String... queues) {
		super(amazonSqs, queues);
	}

	@Override
	protected void sendMessage(final Message<?> sourceMessage) {
		Message<?> message;
		try {
			MessageBuilder<?> messageBuilder = MessageBuilder.fromMessage(sourceMessage);
			stringToBooleanHeaders.forEach(header -> {
				if (sourceMessage.getHeaders().containsKey(header) &&
						sourceMessage.getHeaders().get(header) instanceof String) {
					log.info("Changing '{}' header from String to Boolean", header);
					Object value = sourceMessage.getHeaders().get(header);
					messageBuilder.setHeader(header, Boolean.valueOf(value.toString()));
				}
			});
			message = messageBuilder.build();
		}
		catch (RuntimeException e) {
			throw new MessagingException("Error converting message", e);
		}
		super.sendMessage(message);
	}
}
