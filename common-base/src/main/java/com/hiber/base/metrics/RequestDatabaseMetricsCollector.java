package com.hiber.base.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor that triggers collecting metrics.
 */
public class RequestDatabaseMetricsCollector implements HandlerInterceptor {

	@Autowired
	private HibernateStatementCounter statisticsInterceptor;

	@Autowired
	private MeterRegistry meterRegistry;

	private final Logger log = LoggerFactory.getLogger(RequestDatabaseMetricsCollector.class.getName());


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		statisticsInterceptor.startCounter();
		return true;
	}


	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
		Long queryCount = statisticsInterceptor.getStatementCount();

		try {
			MetricsCollector.collect(
					meterRegistry,
					request.getRequestURI(),
					request.getMethod(),
					queryCount
			);
		}
		catch (Exception e) {
			log.error("Something went wrong while collecting metrics : " + e);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		statisticsInterceptor.clearCounter();
	}
}
