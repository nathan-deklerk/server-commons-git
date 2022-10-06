package com.hiber.base.xlsx;

import lombok.Value;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

public abstract class BaseXlsxExporter {

	@Value
	public static class WorkbookConfig {

		public static final String CURRENCY_FORMAT = "£##0.00";

		/**
		 * Style to display money as pounds.
		 */
		public final CellStyle poundValueStyle;
		/**
		 * Style to display percent value.
		 */
		public final CellStyle percentValueStyle;
		/**
		 * Style to wrap text in row/cell.
		 */
		public final CellStyle textWrapTextStyle;
		/**
		 * Style to bold text.
		 */
		public final CellStyle boldTextStyle;

		/**
		 * Factory method for object.
		 *
		 * @param workbook Source object to create configuration for given workbook.
		 *
		 * @return Created object.
		 */
		public static WorkbookConfig apply(Workbook workbook) {
			CellStyle currencyCellStyle = workbook.createCellStyle();
			currencyCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(CURRENCY_FORMAT));

			CellStyle percentCellStyle = workbook.createCellStyle();
			percentCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("#.0#%"));

			CellStyle wrapTextStyle = workbook.createCellStyle();
			wrapTextStyle.setWrapText(true);

			Font font = workbook.createFont();
			CellStyle headerCellsStyle = workbook.createCellStyle();
			font.setBold(true);
			headerCellsStyle.setFont(font);

			return new WorkbookConfig(currencyCellStyle, percentCellStyle, wrapTextStyle, headerCellsStyle);
		}
	}

}
