package com.hiber.test.sleuth;

import brave.Span;
import brave.Tracer;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;

/**
 * Interceptor for channel that logs current sleuth span information.
 */
@RequiredArgsConstructor
public class SpanLogChannelInterceptor extends ChannelInterceptorAdapter {
	@Getter
	private final List<Span> loggedSpans = new ArrayList<>();

	private final Tracer tracer;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		loggedSpans.add(tracer.currentSpan());
		return super.preSend(message, channel);
	}
}
