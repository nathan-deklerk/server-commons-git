package com.hiber.base.integration.aws.sns;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.hiber.base.integration.aws.sqs.ChangingHeadersDefaults;
import java.util.Collection;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.aws.outbound.SnsMessageHandler;
import org.springframework.integration.aws.outbound.SqsMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Handler that additionally change headers of sending messages. This implementation can change header with {@link Boolean}
 * value to header with {@link String} value and removes headers that are not needed and could not be serialized
 * (e.g. 'nativeHeaders' in value that uses unsupported type {@link org.springframework.util.LinkedMultiValueMap}).
 * Default collection for changing from boolean to string header is {@link ChangingHeadersDefaults#DEFAULT_BOOLEAN_HEADERS}.
 * Default collection for removing headers is {@link ChangingHeadersDefaults#DEFAULT_REMOVE_HEADERS}.
 *
 * @see SnsMessageHandler
 */
@Slf4j
public class ChangingHeadersSnsMessageHandler extends SnsMessageHandler implements ChangingHeadersDefaults {
	/**
	 * Collection of headers to change from boolean to string value (as sns not handling other types in headers).
	 * Defaults to {@link ChangingHeadersDefaults#DEFAULT_BOOLEAN_HEADERS}.
	 */
	@Setter
	private Collection<String> booleanToStringHeaders = DEFAULT_BOOLEAN_HEADERS;

	/**
	 * Collection of headers that will be removed from message and will not be send.
	 * Defaults to {@link ChangingHeadersDefaults#DEFAULT_REMOVE_HEADERS}.
	 */
	@Setter
	private Collection<String> removeHeaders = DEFAULT_REMOVE_HEADERS;

	public ChangingHeadersSnsMessageHandler(AmazonSNSAsync amazonSns) {
		super(amazonSns);
	}

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		MessageBuilder<?> messageBuilder = MessageBuilder.fromMessage(message);
		booleanToStringHeaders.forEach(header -> {
			if (message.getHeaders().containsKey(header) && message.getHeaders().get(header) instanceof Boolean) {
				log.info("Changing '{}' header from Boolean to String", header);
				Object value = message.getHeaders().get(header);
				messageBuilder.setHeader(header, value.toString());
			}
		});
		removeHeaders.forEach(header -> {
			if (message.getHeaders().containsKey(header)) {
				log.info("Removing '{}' header", header);
				messageBuilder.removeHeader(header);
			}
		});
		super.handleMessageInternal(messageBuilder.build());
	}
}
