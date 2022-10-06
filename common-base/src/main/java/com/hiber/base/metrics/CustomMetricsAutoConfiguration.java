package com.hiber.base.metrics;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Adds a set of additional metrics to the metrics collected by Spring Boot Micrometer integration.
 */
@Configuration
@ConditionalOnClass(name = "io.micrometer.core.annotation.Timed")
public class CustomMetricsAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public MeterBinder processMemoryMetrics() {
		return new ProcessMemoryMetrics();
	}

	@Bean
	@ConditionalOnMissingBean
	public MeterBinder processThreadMetrics() {
		return new ProcessThreadMetrics();
	}

	/**
	 * Configuration of interceptors required to collect metrics about statements per HTTP request.
	 *
	 * The configuration uses {@link HibernatePropertiesCustomizer) to register a required Hibernate interceptor.
	 * This is normally done automatically by the Spring Boot JPA autoconfigurer.
	 * If you set up an instance of EntityManager manually you need to make sure that this customizer is applied.
	 * Also if you need to add an additional Hibernate interceptor this approach may not work
	 * and you should probably register {@link RequestDatabaseMetricsCollector) and any number of your interceptors manually.
	 */
	@Configuration
	@ConditionalOnClass(name = "org.hibernate.EmptyInterceptor")
	public class InterceptorsConfiguration implements WebMvcConfigurer {

		@Override
		public void addInterceptors(InterceptorRegistry registry) {
			registry.addInterceptor(requestStatisticsInterceptor());
		}

		@Bean
		public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(HibernateStatementCounter hibernateStatementCounter) {
			return hibernateProperties -> hibernateProperties
					.put("hibernate.session_factory.interceptor", hibernateStatementCounter);
		}


		@Bean
		HibernateStatementCounter hibernateStatementCounter() {
			return new HibernateStatementCounter();
		}

		@Bean
		RequestDatabaseMetricsCollector requestStatisticsInterceptor() {
			return new RequestDatabaseMetricsCollector();
		}
	}

}
