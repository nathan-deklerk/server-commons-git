package com.hiber.base.integration.aws.sqs;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class VisibilityTimeoutCalculator {
	/**
	 * Min visibility timeout in seconds.
	 */
	private final int minVisibilityTimeout;

	/**
	 * Max visibility timeout in seconds.
	 * In SQS it is 12h.
	 */
	private final int maxVisibilityTimeout;

	/**
	 * Return visibility timeout depending on number of attempts of consuming message.
	 *
	 * @param numberOfAttempts Number of attempts.
	 *
	 * @return calculated value of visibility timeout in seconds.
	 */
	public int getNextVisibilityTimeout(final int numberOfAttempts) {
		return exponentialBackoffPolicy(minVisibilityTimeout, numberOfAttempts, maxVisibilityTimeout);
	}

	private int exponentialBackoffPolicy(final int initialInterval, final int numberOfAttempts, final int maxInterval) {
		double exponentialBackoffResult = Math.pow(2, numberOfAttempts - 1) * initialInterval;

		return (int) Math.min(exponentialBackoffResult, maxInterval);
	}
}
