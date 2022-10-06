package com.hiber.base.integration.notify;

import com.hiber.base.domain.Support;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

/**
 * Class allows to notify support about queue processing errors.
 */
@RequiredArgsConstructor
public class SupportNotifyExceptionChannelInterceptor implements ChannelInterceptor {
	/**
	 * Channels that should have additional exception notifying.
	 */
	private final Set<MessageChannel> notifyChannels;
	/**
	 * Allows to send notification about error occurred while processing message.
	 */
	private final Support support;

	@Override
	public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
		if (ex != null && notifyChannels.contains(channel)) {
			support.notify(new QueueMessageProcessingException(message, ex));
		}
	}

	/**
	 * Class that wraps messaging exceptions.
	 */
	public static class QueueMessageProcessingException extends RuntimeException {
		/**
		 * Exception thrown while processing the queue message.
		 */
		private final Exception exception;
		/**
		 * Message that caused the processing exception.
		 */
		private final Message<?> message;

		public QueueMessageProcessingException(Message<?> message, Exception exception) {
			super(exception);
			this.exception = exception;
			this.message = message;
		}

		@Override
		public String getMessage() {
			return String.format(
					"Queue message processing exception: %s; Message: %s",
					exception.getMessage(),
					message.toString()
			);
		}
	}
}
