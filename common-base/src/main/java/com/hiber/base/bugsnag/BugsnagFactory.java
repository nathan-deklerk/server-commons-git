package com.hiber.base.bugsnag;


import brave.Tracer;
import com.bugsnag.Bugsnag;
import com.hiber.base.integration.EnvironmentProvider;
import com.hiber.base.integration.ModuleMetadataFactory;
import com.hiber.base.integration.ModuleMetadataFactory.ModuleMetadata;

/**
 * Creates a Bugsnag instance.
 */
public class BugsnagFactory {
	/**
	 * Creates a Bugsnag instance with provided API key.
	 *
	 * @param apiKey API key.
	 *
	 * @return Bugsnag instance.
	 */
	public static Bugsnag create(String apiKey) {
		Bugsnag bugsnag = new Bugsnag(apiKey);
		ModuleMetadata moduleMetadata = ModuleMetadataFactory.create();
		moduleMetadata.getVersion().ifPresent(bugsnag::setAppVersion);
		bugsnag.setReleaseStage(EnvironmentProvider.getEnvironment());
		bugsnag.addCallback(
				report -> moduleMetadata.getName().ifPresent(module -> report.addToTab("app", "module", module)));
		return bugsnag;
	}

	/**
	 * Creates a Bugsnag instance with provided API key.
	 * Additionally add tab with trace logs.
	 *
	 * @param apiKey Api key.
	 * @param tracer Brave tracer bean.
	 *
	 * @return Bugsnag instance.
	 */
	public static Bugsnag create(String apiKey, Tracer tracer) {
		Bugsnag bugsnag = create(apiKey);

		bugsnag.addCallback(
				report -> {
					report.addToTab("tracer", "parentSpanId", tracer.currentSpan().context().parentIdString());
					report.addToTab("tracer", "spanId", tracer.currentSpan().context().spanIdString());
					report.addToTab("tracer", "traceId", tracer.currentSpan().context().traceId());
				}
		);

		return bugsnag;
	}
}
