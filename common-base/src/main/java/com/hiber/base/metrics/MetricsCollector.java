package com.hiber.base.metrics;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;

class MetricsCollector {

	/**
	 * Collects database metrics and sending into InfluxDB at specified time interval
	 *
	 * @param meterRegistry Meter registry to which we register job.
	 * @param requestUri Request URI.
	 * @param method Http method. (e.g GET, POST)
	 * @param queryCount How many db queries request generated.
	 */
	static void collect(MeterRegistry meterRegistry, String requestUri, String method, Long queryCount) {
		DistributionSummary
				.builder("hibernate_statements_per_request")
				.tags(
						List.of(
								Tag.of("request_url", requestUri),
								Tag.of("method", method)
						)
				)
				.publishPercentileHistogram()
				.register(meterRegistry)
				.record(queryCount);

	}
}
