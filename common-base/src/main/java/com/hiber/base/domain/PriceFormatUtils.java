package com.hiber.base.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class PriceFormatUtils {
	/**
	 * Format price in pennies to £##0.00
	 *
	 * @param penniesPrice Price in pennies.
	 *
	 * @return Formatted price.
	 */
	public static String formatPriceToPounds(Integer penniesPrice) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.UK);
		return nf.format(new BigDecimal(penniesPrice).divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY));
	}

}
