package com.hiber.base.integration.logging;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;

/**
 * Log exception on specific channels interceptor. This is needed for trace identifier and span identifier to be visible in
 * log entry as default exception handler is after tracing handler/interceptor. Unfortunately this leads to duplicate
 * exceptions in logs. Should be created with order near 100 (e.g. by annotation @GlobalChannelInterceptor(order = 100)).
 */
@Slf4j
@RequiredArgsConstructor
public class LogExceptionChannelInterceptor extends ChannelInterceptorAdapter {
	/**
	 * Channels that should have additional exception logging.
	 */
	private final Set<MessageChannel> logChannels;

	@Override
	public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
		if (ex != null && logChannels.contains(channel))
			log.error("Error processing message for channel: {}", channel.toString(), ex);
	}
}
