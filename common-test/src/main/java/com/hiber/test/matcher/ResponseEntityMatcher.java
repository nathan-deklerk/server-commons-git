package com.hiber.test.matcher;

import com.mistraltech.smog.core.annotation.Matches;
import org.hamcrest.Matcher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.mistraltech.smog.proxy.javassist.JavassistMatcherGenerator.matcherOf;

public interface ResponseEntityMatcher<P1> extends Matcher<ResponseEntity<P1>> {
	ResponseEntityMatcher<P1> like(final ResponseEntity<P1> template);

	ResponseEntityMatcher<P1> hasHeaders(final HttpHeaders headers);

	ResponseEntityMatcher<P1> hasHeaders(final Matcher<? super HttpHeaders> headersMatcher);

	ResponseEntityMatcher<P1> hasStatusCodeValue(final int statusCodeValue);

	ResponseEntityMatcher<P1> hasStatusCodeValue(final Matcher<? super Integer> statusCodeValueMatcher);

	ResponseEntityMatcher<P1> hasBody(final P1 body);

	ResponseEntityMatcher<P1> hasBody(final Matcher<? super P1> bodyMatcher);

	ResponseEntityMatcher<P1> hasStatusCode(final HttpStatus statusCode);

	ResponseEntityMatcher<P1> hasStatusCode(final Matcher<? super HttpStatus> statusCodeMatcher);

	@Matches(value = ResponseEntity.class, description = "a ResponseEntity")
	interface StringResponseEntityMatcher extends ResponseEntityMatcher<String> {
		static StringResponseEntityMatcher aStringResponseEntityThat() {
			return matcherOf(StringResponseEntityMatcher.class);
		}
	}
}