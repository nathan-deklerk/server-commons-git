package com.hiber.base.bugsnag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Settings related to Bugsnag.
 */
@Configuration
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("hiber.bugsnag")
public class BugsnagSettings {
	/**
	 * API key that should be used by Bugsnag while reporting errors.
	 */
	private String apiKey;
}
