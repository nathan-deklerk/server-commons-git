package com.hiber.base.domain;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

import static com.hiber.base.domain.DateUtil.getDayOfMonthWithSuffix;
import static com.hiber.base.domain.DateUtil.getFormattedDayOfMonthWithMonth;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DateUtilTest {

	@Test
	void getDayOfMonthWithSuffix_shouldReturnCorrectFormattedDayWithSuffix() {
		assertThat(getDayOfMonthWithSuffix(22), is("22nd"));
	}

	@Test
	void getDayOfMonthWithSuffix_shouldReturnCorrectFormattedDayFor_11() {
		assertThat(getDayOfMonthWithSuffix(11), is("11th"));
	}

	@Test
	void getFormattedDayOfMonthWithMonth_shouldReturnCorrectlyFormattedDate() {
		assertThat(getFormattedDayOfMonthWithMonth(ZonedDateTime.parse("2018-09-23T11:54:29.217+02:00")), is("23rd September"));
	}
}

