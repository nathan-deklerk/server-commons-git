package com.hiber.base.integration;

import java.util.Optional;

public class EnvironmentProvider {
	/**
	 * Provides environment that this instance of the system is running within, e.g. staging, production.
	 *
	 * @return Environment.
	 */
	public static String getEnvironment() {
		return Optional.ofNullable(System.getenv("HIBER_ENVIRONMENT")).orElse("undefined");
	}
}
