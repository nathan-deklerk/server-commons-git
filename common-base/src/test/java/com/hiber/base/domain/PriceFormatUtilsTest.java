package com.hiber.base.domain;

import org.junit.jupiter.api.Test;

import static com.hiber.base.domain.PriceFormatUtils.formatPriceToPounds;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PriceFormatUtilsTest {

	@Test
	void formatPriceToPounds_shouldFormatCommaSeparatedThousand() {
		assertThat(formatPriceToPounds(12200000), is("£122,000.00"));
	}

	@Test
	void formatPriceToPounds_shouldFormatHundreds() {
		assertThat(formatPriceToPounds(92300), is("£923.00"));
	}

	@Test
	void formatPriceToPounds_shouldFormatDecimalPennies() {
		assertThat(formatPriceToPounds(12), is("£0.12"));
	}
}

