package com.hiber.base.xlsx;

import com.hiber.base.xlsx.BaseXlsxExporter.WorkbookConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public abstract class XlsxUtils {

	/**
	 * Convert price to pounds.
	 *
	 * @param price price to convert.
	 *
	 * @return price in pounds.
	 */
	public static double convertPriceToPounds(Long price) {
		return BigDecimal.valueOf(price)
				.setScale(2, RoundingMode.HALF_UP)
				.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
				.doubleValue();
	}

	/**
	 * Parses price to double value.
	 *
	 * @param priceValue price as string.
	 *
	 * @return price as double
	 */
	public static double parsePrice(String priceValue) {
		try {
			return NumberFormat.getCurrencyInstance(Locale.UK).parse(priceValue).doubleValue();
		}
		catch (ParseException ignored) {
			try {
				return NumberFormat.getInstance(Locale.UK).parse(priceValue).doubleValue();
			}
			catch (ParseException e) {
				throw new RuntimeException("Cannot parse price");
			}
		}
	}

	/**
	 * Parses BigDecimal value.
	 *
	 * @param value BigDecimal value
	 *
	 * @return Parsed BigDecimal
	 */
	public static BigDecimal parseBigDecimal(String value) {
		try {
			return new BigDecimal(value);
		}
		catch (NumberFormatException ex) {
			throw new RuntimeException("Cannot parse big decimal value");
		}
	}

	/**
	 * Parses value to a {@link Integer}.
	 *
	 * @param value Source integer as a string.
	 *
	 * @return Parsed {@link Integer}.
	 */
	public static Integer parseInteger(String value) {
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException ex) {
			throw new RuntimeException(String.format("Cannot parse `%s` to a Integer", value));
		}
	}

	/**
	 * Create first row with headers.
	 *
	 * @param sheet Sheet to modify.
	 * @param config Workbook config contains some global properties.
	 * @param columnsHeader Columns header.
	 */
	public static void createHeaderRow(Sheet sheet, WorkbookConfig config, List<String> columnsHeader) {
		createHeaderRow(sheet, config, columnsHeader, 0);
	}

	/**
	 * Create row with headers.
	 *
	 * @param sheet Sheet to modify.
	 * @param config Workbook config contains some global properties.
	 * @param columnsHeader Columns header.
	 */
	public static void createHeaderRow(Sheet sheet, WorkbookConfig config, List<String> columnsHeader, int rowNum) {
		Row row = sheet.createRow(rowNum);
		row.setRowStyle(config.boldTextStyle);
		for (int i = 0; i < columnsHeader.size(); i++)
			insertValue(row, i, columnsHeader.get(i));
	}

	/**
	 * Inserts value to row.
	 *
	 * @param row Row to modify.
	 * @param columnIndex Index of column.
	 * @param value Value to be inserted.
	 */
	public static void insertValue(Row row, int columnIndex, String value) {
		row.createCell(columnIndex).setCellValue(value);
	}

	/**
	 * Inserts price value.
	 *
	 * @param row Row to modify
	 * @param columnIndex Index of column.
	 * @param config {@link WorkbookConfig} config
	 * @param price Price to be inserted.
	 */
	public static void insertPriceValue(Row row, int columnIndex, WorkbookConfig config, Integer price) {
		insertPriceValue(row, columnIndex, config, price.longValue());
	}

	/**
	 * Inserts price value.
	 *
	 * @param row Row to modify
	 * @param columnIndex Index of column.
	 * @param config {@link WorkbookConfig} config
	 * @param price Price to be inserted.
	 */
	public static void insertPriceValue(Row row, int columnIndex, WorkbookConfig config, Long price) {
		Cell priceCell = row.createCell(columnIndex);
		priceCell.setCellStyle(config.poundValueStyle);
		priceCell.setCellValue(convertPriceToPounds(price));
	}

	/**
	 * Inserts int value.
	 *
	 * @param row Row to modify
	 * @param columnIndex Index of column.
	 * @param intValue Value to be inserted.
	 */
	public static void insertIntValue(Row row, int columnIndex, int intValue) {
		final String value = asString(intValue);
		row.createCell(columnIndex).setCellValue(value);
	}

	/**
	 * Inserts boolean value.
	 *
	 * @param row Row to modify
	 * @param columnIndex Index of column.
	 * @param booleanValue Value to be inserted.
	 */
	public static void insertBooleanValue(Row row, int columnIndex, Boolean booleanValue) {
		final String value = asString(booleanValue);
		row.createCell(columnIndex).setCellValue(value);
	}

	/**
	 * Inserts boolean value.
	 *
	 * @param row Row to modify
	 * @param columnIndex Index of column.
	 * @param numericValue Value to be inserted.
	 */
	public static void insertBigDecimalValue(Row row, int columnIndex, BigDecimal numericValue) {
		row.createCell(columnIndex).setCellValue(numericValue.stripTrailingZeros().toPlainString());
	}

	/**
	 * Inserts boolean value.
	 *
	 * @param row Row to modify
	 * @param columnIndex Index of column.
	 * @param dateTime Value to be inserted.
	 */
	public static void insertDateTimeValue(Row row, int columnIndex, ZonedDateTime dateTime) {
		row.createCell(columnIndex).setCellValue(asString(dateTime));
	}

	/**
	 * Inserts optional Big Decimal value to row.
	 *
	 * @param row Row to modify.
	 * @param columnIndex Index of column.
	 * @param value Value to be inserted.
	 */
	public static void insertOptionalBigDecimalValue(Row row, int columnIndex, Optional<BigDecimal> value) {
		insertOptionalValue(row, columnIndex, value.map(v -> v.stripTrailingZeros().toPlainString()));
	}


	/**
	 * Inserts optional String value to row.
	 *
	 * @param row Row to modify.
	 * @param columnIndex Index of column.
	 * @param value Value to be inserted.
	 */
	public static void insertOptionalStringValue(Row row, int columnIndex, Optional<String> value) {
		insertOptionalValue(row, columnIndex, value);
	}

	/**
	 * Inserts optional value to row.
	 *
	 * @param row Row to modify.
	 * @param columnIndex Index of column.
	 * @param value Value to be inserted.
	 */
	private static <Type> void insertOptionalValue(Row row, int columnIndex, Optional<Type> value) {
		value.ifPresent(v -> row.createCell(columnIndex)
				.setCellValue(v.toString()));
	}

	/**
	 * Represents boolean value as a TRUE or FALSE string
	 *
	 * @param value boolean value.
	 *
	 * @return string representation.
	 */
	public static String asString(boolean value) {
		return value ? "TRUE" : "FALSE";
	}

	/**
	 * Represents {@link ZonedDateTime} value as a string formatting it according to RFC 1123.
	 *
	 * @param value {@link ZonedDateTime} value.
	 *
	 * @return string representation.
	 */
	public static String asString(ZonedDateTime value) {
		return value.format(DateTimeFormatter.RFC_1123_DATE_TIME);
	}

	/**
	 * Represents int value as a string.
	 *
	 * @param value int value.
	 *
	 * @return String representation.
	 */
	public static String asString(int value) {
		return String.valueOf(value);
	}
}
