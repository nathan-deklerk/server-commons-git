package com.hiber.test.app;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties("hiber.test-app")
public class SqsSettings {
	@NotNull
	private String fifoQueueName;

	@NotNull
	private String firstQueueName;

	@NotNull
	private String secondQueueName;
}