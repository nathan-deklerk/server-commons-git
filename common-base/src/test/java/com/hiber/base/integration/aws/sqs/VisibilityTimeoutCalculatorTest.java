package com.hiber.base.integration.aws.sqs;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class VisibilityTimeoutCalculatorTest {
	private static final int INITIAL_VISIBILITY_TIMEOUT = 60;
	private static final int MAX_VISIBILITY_TIMEOUT = 43200;

	private final VisibilityTimeoutCalculator visibilityTimeoutCalculator = new VisibilityTimeoutCalculator(
			INITIAL_VISIBILITY_TIMEOUT,
			MAX_VISIBILITY_TIMEOUT
	);

	@Test
	void shouldReturnCorrectValuesOfPauseTime() {
		assertThat(visibilityTimeoutCalculator.getNextVisibilityTimeout(1)).isEqualTo(60);
		assertThat(visibilityTimeoutCalculator.getNextVisibilityTimeout(2)).isEqualTo(120);
		assertThat(visibilityTimeoutCalculator.getNextVisibilityTimeout(3)).isEqualTo(240);
		assertThat(visibilityTimeoutCalculator.getNextVisibilityTimeout(4)).isEqualTo(480);
		// ...
		// ...
		assertThat(visibilityTimeoutCalculator.getNextVisibilityTimeout(9)).isEqualTo(15360);
		assertThat(visibilityTimeoutCalculator.getNextVisibilityTimeout(10)).isEqualTo(30720);
		assertThat(visibilityTimeoutCalculator.getNextVisibilityTimeout(11)).isEqualTo(43200);
		assertThat(visibilityTimeoutCalculator.getNextVisibilityTimeout(12)).isEqualTo(43200);
	}

}