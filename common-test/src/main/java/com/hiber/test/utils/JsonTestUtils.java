package com.hiber.test.utils;


import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import java.util.regex.Pattern;
import org.hamcrest.MatcherAssert;
import org.hamcrest.text.MatchesPattern;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONParser;

import static org.hamcrest.Matchers.notNullValue;

/**
 * Utility methods related to handling JSON format.
 */
public class JsonTestUtils {

	/**
	 * Extracts string from json value.
	 *
	 * @param json Test json object.
	 * @param jsonPath Json path with value field identifier.
	 *
	 * @return Extracted uuid identifier.
	 */
	public static String extractString(String json, String jsonPath) {
		String value = JsonPath.read(json, jsonPath);
		MatcherAssert.assertThat(value, notNullValue());
		return value;
	}

	/**
	 * Extracts uuid from json value.
	 *
	 * @param json Test json object.
	 * @param jsonPath Json path with value field identifier.
	 *
	 * @return Extracted uuid identifier.
	 */
	public static UUID extractUuid(String json, String jsonPath) {
		String uuid = JsonPath.read(json, jsonPath);
		MatcherAssert.assertThat(uuid, notNullValue());
		MatcherAssert.assertThat(uuid, new MatchesPattern(
				Pattern.compile("^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$")));
		return UUID.fromString(uuid);
	}

	/**
	 * Translates provided text by replacing <code>'</code> character with <code>"</code> character and producing (hopefully) a
	 * valid JSON.
	 *
	 * @param value Input JSON.
	 *
	 * @return Output JSON.
	 */
	public static String json(String value) {
		try {
			return JSONParser.parseJSON(value).toString();
		}
		catch (JSONException e) {
			throw new RuntimeException("Could not parse JSON", e);
		}
	}

	/**
	 * Translates provided text by replacing <code>'</code> character with <code>"</code> character and producing (hopefully) a
	 * valid JSON.
	 *
	 * @param value Input JSON.
	 * @param variables Variables to populate '%s' placeholders.
	 *
	 * @return Output JSON.
	 */
	public static String json(String value, Object... variables) {
		try {
			return JSONParser.parseJSON(String.format(value, variables)).toString();
		}
		catch (JSONException e) {
			throw new RuntimeException("Could not parse JSON", e);
		}
	}

	/**
	 * Extracts string from json value.
	 *
	 * @param json Test json object.
	 * @param jsonPath Json path with value field identifier.
	 *
	 * @return Extracted uuid identifier.
	 */
	public static long extractLong(String json, String jsonPath) {
		Number value = JsonPath.read(json, jsonPath);
		MatcherAssert.assertThat(value, notNullValue());
		return value.longValue();
	}

}