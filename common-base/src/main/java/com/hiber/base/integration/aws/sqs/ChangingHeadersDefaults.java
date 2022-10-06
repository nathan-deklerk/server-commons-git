package com.hiber.base.integration.aws.sqs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.springframework.integration.mapping.support.JsonHeaders;

/**
 * Changing headers defaults.
 */
public interface ChangingHeadersDefaults {
	/**
	 * Sleuth message sent header name.
	 */
	String SLEUTH_MESSAGE_SENT_HEADER = "messageSent";

	/**
	 * Unknown (add by some spring mechanism) native headers name.
	 */
	String UNKNOWN_NATIVE_HEADERS_HEADER = "nativeHeaders";

	/**
	 * Default headers names that should be changed from boolean to string and string to boolean from message.
	 */
	Collection<String> DEFAULT_BOOLEAN_HEADERS = Collections.singleton(SLEUTH_MESSAGE_SENT_HEADER);

	/**
	 * Default headers names that should be removed from message.
	 */
	Collection<String> DEFAULT_REMOVE_HEADERS = Arrays.asList(
			UNKNOWN_NATIVE_HEADERS_HEADER,
			JsonHeaders.TYPE_ID,
			JsonHeaders.CONTENT_TYPE_ID,
			JsonHeaders.KEY_TYPE_ID
	);
}
