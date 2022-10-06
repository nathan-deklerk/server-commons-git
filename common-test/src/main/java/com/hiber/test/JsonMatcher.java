package com.hiber.test;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class JsonMatcher extends TypeSafeMatcher<String> {
	private String content = "{}";

	private JSONCompareMode compareMode = JSONCompareMode.LENIENT;

	private String errorMessage;

	public static JsonMatcher aJsonThat() {
		return new JsonMatcher();
	}

	public JsonMatcher hasContent(String content) {
		try {
			JSONAssert.assertEquals("{}", content, false);
		}
		catch (JSONException e) {
			throw new AssertionError("Passed expected content is not a valid json: " + e.getMessage());
		}
		catch (AssertionError e) {
			// do nothing - we only checks if passed content is valid json
		}
		this.content = content;
		return this;
	}

	public JsonMatcher hasStrictOrderContent(String content) {
		try {
			JSONAssert.assertEquals("{}", content, false);
		}
		catch (JSONException e) {
			throw new AssertionError("Passed expected content is not a valid json: " + e.getMessage());
		}
		catch (AssertionError e) {
			// do nothing - we only checks if passed content is valid json
		}
		this.content = content;
		this.compareMode = JSONCompareMode.STRICT_ORDER;
		return this;
	}

	@Override
	protected boolean matchesSafely(String item) {
		try {
			JSONAssert.assertEquals(content, item, compareMode);
		}
		catch (JSONException | AssertionError e) {
			errorMessage = e.getMessage();
			return false;
		}
		return true;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(String.format("a json text with content \"%s\"", content));
	}

	@Override
	protected void describeMismatchSafely(String item, Description mismatchDescription) {
		super.describeMismatchSafely(item, mismatchDescription);
		if (errorMessage != null)
			mismatchDescription.appendText(String.format("\n\nDetail error was: %s", errorMessage));
	}
}
