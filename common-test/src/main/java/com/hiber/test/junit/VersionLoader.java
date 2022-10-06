package com.hiber.test.junit;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static java.util.Arrays.asList;

public class VersionLoader {
	private static final String applicationProperties = "application.properties";
	private static final String applicationVersionProperty = "hiber.build.version";

	/**
	 * Loads application version from application.properties.
	 *
	 * @return Loaded version.
	 *
	 * @throws Exception When application properties or application.version are not specified.
	 */
	public static String loadVersion() throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();
		InputStream resourceStream = loader.getResourceAsStream(applicationProperties);
		if (resourceStream != null) {
			props.load(resourceStream);
		}
		else {
			throw new Exception("Cannot load application version! Specify application.version in application.properties.");
		}
		return props.getProperty(applicationVersionProperty);
	}

	/**
	 * Parses version from string representation to {@link Double}.
	 *
	 * @param version String representation of version.
	 *
	 * @throws Exception If version is absent or malformed.
	 */
	public static BigDecimal parseVersion(String version) throws Exception {
		Objects.requireNonNull(version, "Version is null!");
		List<String> versions = asList(version.split("\\."));

		if (versions.size() >= 2) {
			try {
				BigDecimal major = new BigDecimal((versions.get(0)))
						.setScale(2, RoundingMode.HALF_UP);
				BigDecimal minor = new BigDecimal(versions.get(1))
						.divide(new BigDecimal("1000"));
				return major.add(minor).setScale(3, RoundingMode.HALF_UP);
			}
			catch (NumberFormatException e) {
				throw new Exception(String.format("Version: %s is malformed!", version));
			}
		}
		return null;
	}
}
