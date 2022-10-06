package com.hiber.test.junit;

import java.math.BigDecimal;

import static com.hiber.test.junit.VersionLoader.loadVersion;
import static com.hiber.test.junit.VersionLoader.parseVersion;

public class RunScriptTestPolicy {
	private BigDecimal applicationVersion;
	private BigDecimal scriptVersion;

	public RunScriptTestPolicy(String scriptVersion) throws Exception {
		this.applicationVersion = parseVersion(loadVersion());
		this.scriptVersion = parseVersion(scriptVersion);
	}

	/**
	 * Constructor for test purpose
	 */
	public RunScriptTestPolicy(String applicationVersion, String scriptVersion) throws Exception {
		this.applicationVersion = parseVersion(applicationVersion);
		this.scriptVersion = parseVersion(scriptVersion);
	}

	/**
	 * Checks if annotated class should be ignored.
	 *
	 * @return True if test class should be ignored, otherwise false.
	 */
	public boolean testClassShouldBeIgnored() {
		if (applicationVersion != null && scriptVersion != null) {
			return applicationVersion.subtract(scriptVersion).compareTo(new BigDecimal("0.002")) >= 0;
		}
		return false;
	}
}
