package com.hiber.test.xlsx;

import com.hiber.base.xlsx.BaseXlsxExporter.WorkbookConfig;
import com.hiber.base.xlsx.XlsxUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import static com.hiber.base.xlsx.BaseXlsxExporter.WorkbookConfig.CURRENCY_FORMAT;
import static com.hiber.base.xlsx.XlsxUtils.convertPriceToPounds;
import static com.hiber.base.xlsx.XlsxUtils.createHeaderRow;
import static com.hiber.base.xlsx.XlsxUtils.insertBigDecimalValue;
import static com.hiber.base.xlsx.XlsxUtils.insertBooleanValue;
import static com.hiber.base.xlsx.XlsxUtils.insertDateTimeValue;
import static com.hiber.base.xlsx.XlsxUtils.insertIntValue;
import static com.hiber.base.xlsx.XlsxUtils.insertOptionalBigDecimalValue;
import static com.hiber.base.xlsx.XlsxUtils.insertOptionalStringValue;
import static com.hiber.base.xlsx.XlsxUtils.insertPriceValue;
import static com.hiber.base.xlsx.XlsxUtils.insertValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;

public class XlsxTestUtils {

	private WorkbookConfig config;

	private Sheet sheet;

	private List<String> columnsHeader = Arrays.asList("Header 1", "Header 2", "Header 3");

	/**
	 * Converts all cells from given row to string values.
	 *
	 * @param row Row.
	 *
	 * @return String values, one for each cell.
	 */
	public static String[] toStringValues(Row row) {
		String[] values = new String[row.getLastCellNum()];
		for (int i = 0; i < values.length; i++) {
			Cell cell = row.getCell(i);
			DataFormatter dataFormatter = new DataFormatter(Locale.US);
			values[i] = dataFormatter.formatCellValue(cell);
		}
		return values;
	}

	/**
	 * Returns xlsx sheet by given name.
	 *
	 * @param file file which should be read.
	 * @param name sheet name.
	 *
	 * @return {@link XSSFSheet}.
	 */
	public static XSSFSheet getSheetByName(File file, String name) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
		return workbook.getSheet(name);
	}

	/**
	 * Decrypts xlsx file.
	 *
	 * @param inputStream file as input stream.
	 * @param password password used to decrypt.
	 *
	 * @return decrypted {@link XSSFWorkbook} workbook.
	 */
	public static XSSFWorkbook getDecryptedXlsx(InputStream inputStream, String password) {
		try (POIFSFileSystem filesystem = new POIFSFileSystem(inputStream)) {

			EncryptionInfo info = new EncryptionInfo(filesystem);
			Decryptor d = Decryptor.getInstance(info);

			if (!d.verifyPassword(password)) {
				throw new RuntimeException(
						"Unable to decrypt: enter valid password");
			}

			InputStream dataStream = d.getDataStream(filesystem);

			return new XSSFWorkbook(dataStream);
		}
		catch (IOException | GeneralSecurityException ex) {
			throw new RuntimeException("Unable to process encrypted document", ex);
		}
	}

	@Before
	public void setup() {
		Workbook workbook = new XSSFWorkbook();
		config = WorkbookConfig.apply(workbook);
		sheet = workbook.createSheet("test");
	}

	@Test
	public void createHeaderRow_shouldCreateHeaderRow() {
		createHeaderRow(sheet, config, columnsHeader);

		assertThat(
				toStringValues(sheet.getRow(0)),
				arrayContaining("Header 1", "Header 2", "Header 3")
		);
	}

	@Test
	public void insertValue_shouldInsertValue() {
		insertValue(sheet.createRow(1), 0, "Test value");

		assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is("Test value"));
	}

	@Test
	public void insertPriceValue_shouldInsertPrice() {
		insertPriceValue(sheet.createRow(1), 0, config, 500);

		assertThat(sheet.getRow(1).getCell(0).getNumericCellValue(), is(5.0));
	}

	@Test
	public void insertIntValue_shouldInsertIntValue() {
		insertIntValue(sheet.createRow(1), 0, 21);

		assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is("21"));
	}

	@Test
	public void insertBooleanValue_shouldInsertBooleanValue() {
		insertBooleanValue(sheet.createRow(1), 0, true);

		assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is("TRUE"));
	}

	@Test
	public void insertBigDecimalValue_shouldInsertBigDecimalValue() {
		insertBigDecimalValue(sheet.createRow(1), 0, new BigDecimal("1267.89"));

		assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is("1267.89"));
	}

	@Test
	public void insertBigDecimalValue_shouldICorrectlyFormat10() {
		insertBigDecimalValue(sheet.createRow(1), 0, new BigDecimal("10"));

		assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is("10"));
	}

	@Test
	public void insertBigDecimalValue_shouldICorrectlyFormatMinus20() {
		insertBigDecimalValue(sheet.createRow(1), 0, new BigDecimal("-20"));

		assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is("-20"));
	}

	@Test
	public void insertBigDecimalValue_shouldICorrectlyFormat1000WithoutTrailingZeros() {
		insertBigDecimalValue(sheet.createRow(1), 0, new BigDecimal("1000.00"));

		assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is("1000"));
	}

	@Test
	public void insertDateTimeValue_shouldInsertDateTimeValue() {
		insertDateTimeValue(sheet.createRow(1), 0, ZonedDateTime.parse("2018-03-23T11:20:08Z[UTC]"));

		assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is("Fri, 23 Mar 2018 11:20:08 GMT"));
	}

	@Test
	public void insertOptionalStringValue_shouldInsert() {
		insertOptionalStringValue(sheet.createRow(1), 0, Optional.of("Test optional value"));

		assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is("Test optional value"));
	}

	@Test
	public void insertOptionalBigDecimalValue_shouldInsert() {
		insertOptionalBigDecimalValue(sheet.createRow(1), 0, Optional.of(new BigDecimal(100.00)));

		assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), is("100"));
	}

	/**
	 * Gets row in first sheet.
	 *
	 * @param bytes Workbook written in byte[]
	 * @param rowIndex Index of row (starting from 0)
	 * @return Fetched row.
	 */
	public static Row getRow(byte[] bytes, int rowIndex) {
		try {
			Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
			Sheet sheet = workbook.getSheetAt(0);
			return sheet.getRow(rowIndex);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Represents {@link ZonedDateTime} value as a string formatting it according to RFC 1123.
	 *
	 * @param value {@link ZonedDateTime} value.
	 *
	 * @return String representation.
	 */
	public static String asString(ZonedDateTime value) {
		return XlsxUtils.asString(value);
	}

	/**
	 * Represents boolean value as a TRUE or FALSE string
	 *
	 * @param value boolean value.
	 *
	 * @return String representation.
	 */
	public static String asString(boolean value) {
		return XlsxUtils.asString(value);
	}

	/**
	 * Represents {@link BigDecimal} value as a string.
	 *
	 * @param value {@link BigDecimal} value.
	 *
	 * @return String representation.
	 */
	public static String asString(BigDecimal value) {
		return value + "";
	}

	/**
	 * Represents {@link Enum} value as a string using name().
	 *
	 * @param value {@link Enum} value.
	 *
	 * @return String representation.
	 */
	public static String asString(Enum value) {
		return value.name();
	}

	/**
	 * Represents {@link UUID} value as a string.
	 *
	 * @param value {@link UUID} value.
	 *
	 * @return String representation.
	 */
	public static String asString(UUID value) {
		return value.toString();
	}

	/**
	 * Represents int value as a string.
	 *
	 * @param value int value.
	 *
	 * @return String representation.
	 */
	public static String asString(int value) {
		return XlsxUtils.asString(value);
	}

	/**
	 * Returns itself to achieve name consistency in tests.
	 *
	 * @param value {@link String} value.
	 *
	 * @return Itself.
	 */
	public static String asString(String value) {
		return value;
	}

	/**
	 * Represents {@link Optional} value as a string. If it is empty then empty string is returned.
	 *
	 * @param value {@link Optional} value.
	 *
	 * @return String representation.
	 */
	public static String asString(Optional<?> value) {
		return value
				.map(Object::toString)
				.orElse("");
	}

	/**
	 * Represents price stored in pens as a string in pounds.
	 *
	 * @param priceInPens Price stored in pens.
	 *
	 * @return String representation.
	 */
	public static String asStringInPounds(Long priceInPens) {
		double price = convertPriceToPounds(priceInPens);
		NumberFormat numberFormat = new DecimalFormat(CURRENCY_FORMAT);

		return numberFormat.format(price);
	}
}
