package com.hiber.base.metrics;

import org.hibernate.EmptyInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects number of db statements.
 */
public class HibernateStatementCounter extends EmptyInterceptor {

	private final Logger log = LoggerFactory.getLogger(RequestDatabaseMetricsCollector.class.getName());
	private ThreadLocal<Long> statementCount = new ThreadLocal<>();

	void startCounter() {
		statementCount.set(0L);
	}

	Long getStatementCount() {
		return statementCount.get();
	}

	void clearCounter() {
		statementCount.remove();
	}

	@Override
	public String onPrepareStatement(String sql) {
		Long count = statementCount.get();
		if (count != null) {
			statementCount.set(count + 1);
		}
		else {
			log.warn("Counter wasn't initialized properly.");
			statementCount.set(1L);
		}
		return super.onPrepareStatement(sql);
	}
}
