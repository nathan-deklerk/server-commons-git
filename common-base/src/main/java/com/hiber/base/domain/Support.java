package com.hiber.base.domain;

/**
 * Provides ability to send notification about an error occurred in the system.
 */
public interface Support {
	/**
	 * Sends notification about an error.
	 *
	 * @param throwable An exception representing the error.
	 */
	void notify(Throwable throwable);
}
