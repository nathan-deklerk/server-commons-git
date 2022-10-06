package com.hiber.base.queue;

import java.util.concurrent.atomic.AtomicLong;

public class ConsumerMessageCounter {

	private final AtomicLong messageCounter = new AtomicLong(0);

	/**
	 * Message consumed counter. This is mainly for testing purpose to check if messages has bean consumed.
	 *
	 * @return Message consumed counter.
	 */
	public long getCounterValue() {
		return messageCounter.get();
	}

	/**
	 * Increments counter.
	 */
	public void incrementCounter() {
		messageCounter.incrementAndGet();
	}
}
