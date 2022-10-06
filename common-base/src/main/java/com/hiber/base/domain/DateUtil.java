package com.hiber.base.domain;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtil {
	private static final DateTimeFormatter MONTH_FORMATTER_UK = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH);

	/**
	 * Format day with ordinal suffix (f.eg. 23rd, 31st).
	 *
	 * @param dayOfMonth Day of month.
	 *
	 * @return Formatted day of month.
	 *
	 * @throws IllegalDayOfMonthException When day of month is less than 1 or greater then 31.
	 */
	public static String getDayOfMonthWithSuffix(int dayOfMonth) {
		if (dayOfMonth < 1 || dayOfMonth > 31) {
			throw new IllegalDayOfMonthException(dayOfMonth);
		}
		if (dayOfMonth >= 11 && dayOfMonth <= 13) {
			return dayOfMonth + "th";
		}
		switch (dayOfMonth % 10) {
			case 1:
				return dayOfMonth + "st";
			case 2:
				return dayOfMonth + "nd";
			case 3:
				return dayOfMonth + "rd";
			default:
				return dayOfMonth + "th";
		}
	}

	/**
	 * Format day of month and month name.
	 *
	 * @param dateTime Date to format.
	 *
	 * @return Formatted day of month with month.
	 */
	public static String getFormattedDayOfMonthWithMonth(ZonedDateTime dateTime) {
		String month = dateTime.format(MONTH_FORMATTER_UK);
		return String.format(
				"%s %s",
				getDayOfMonthWithSuffix(dateTime.getDayOfMonth()),
				month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase()
		);
	}

	/**
	 * Signals that day of month is illegal (< 1 || > 31)
	 */
	public static class IllegalDayOfMonthException extends RuntimeException {
		/**
		 * @param dayOfMonth Day of month.
		 */
		IllegalDayOfMonthException(int dayOfMonth) {
			super(String.format("Illegal day of month '%s'", dayOfMonth));
		}
	}
}
