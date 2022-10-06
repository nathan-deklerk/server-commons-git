package com.hiber.base.bugsnag;

import brave.Tracer;
import com.bugsnag.Bugsnag;
import com.hiber.base.config.SupportConfig.SupportNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Bugsnag.
 * <p/>
 * Configuration happens only if <code>hiber.bugsnag.api-key</code> is present. The assumption is that for local development
 * we don't need to report bugs via Bugsnag, but all deployment environments (production etc.) provide the key.
 * <p/>
 * As usual for Spring Boot the property (API key) may be provided via any supported mechanism (by system environment variable,
 * by properties file(s), by program argument etc.).
 */
@Configuration
@ConditionalOnProperty(value = "hiber.bugsnag.api-key")
@EnableConfigurationProperties(BugsnagSettings.class)
public class BugsnagConfig {

	@Bean
	public Bugsnag bugsnag(BugsnagSettings bugsnagSettings, Tracer tracer) {
		return BugsnagFactory.create(bugsnagSettings.getApiKey(), tracer);
	}

	@Bean
	public SupportNotifier bugsnagSupport(Bugsnag bugsnag) {
		return new BugsnagNotifier(bugsnag);
	}

	@RequiredArgsConstructor
	static class BugsnagNotifier implements SupportNotifier {
		private final Bugsnag bugsnag;

		@Override
		public void notify(Throwable throwable) {
			bugsnag.notify(throwable);
		}
	}
}
