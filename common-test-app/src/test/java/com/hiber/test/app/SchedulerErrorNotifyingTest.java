package com.hiber.test.app;

import com.hiber.base.config.SupportConfig.SupportNotifier;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ActiveProfiles;

import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@Tag("IntegrationTest")
@Import(SchedulerErrorNotifyingTest.TestConfiguration.class)
@SpringBootTest
class SchedulerErrorNotifyingTest {
	@Autowired
	private ExceptionRecordingSupportNotifier exceptionRecordingSupportNotifier;

	@Autowired
	private PeriodicTask periodicTask;

	@BeforeEach
	public void resetRecorders() {
		exceptionRecordingSupportNotifier.clear();
	}

	@AfterEach
	public void disableTaskExecution() {
		periodicTask.disable();
	}

	@Test
	public void shouldReportExceptionsToConfiguredNotifiers() {
		periodicTask.enableOnce();

		waitAtMost(1, TimeUnit.SECONDS).until(() -> !exceptionRecordingSupportNotifier.getExceptions().isEmpty());

		final Throwable exception = exceptionRecordingSupportNotifier.getExceptions().get(0);
		assertThat(exception.getClass(), is(TaskExecutionException.class));
	}

	/**
	 * Test-specific configuration that sets up an example scheduled task and supporting classes allowing to test if errors thrown
	 * from the task are handled appropriately.
	 */
	@EnableScheduling
	@org.springframework.boot.test.context.TestConfiguration
	public static class TestConfiguration {
		@Bean
		public PeriodicTask periodicTask() {
			return new PeriodicTask();
		}

		@Bean
		public SupportNotifier exceptionRecordingSupportNotifier() {
			return new ExceptionRecordingSupportNotifier();
		}
	}

	/**
	 * An exception thrown by the periodic task.
	 */
	public static class TaskExecutionException extends RuntimeException {
	}

	public static class PeriodicTask {
		private boolean enabled = true;
		private int times = 0;

		@Scheduled(fixedRate = 1)
		public void execute() {
			if (enabled && times > 0) {
				times = times - 1;
				throw new TaskExecutionException();
			}
		}

		void enableOnce() {
			this.enabled = true;
			this.times = 1;
		}

		void disable() {
			this.enabled = false;
		}
	}
}