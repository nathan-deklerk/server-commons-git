package com.hiber.base.matcher.rest.model;

import com.hiber.base.rest.model.ErrorDto;
import com.mistraltech.smog.core.annotation.Matches;
import java.time.ZonedDateTime;
import org.hamcrest.Matcher;

import static com.mistraltech.smog.proxy.javassist.JavassistMatcherGenerator.matcherOf;

@Matches(value = com.hiber.base.rest.model.ErrorDto.class, description = "an ErrorDto")
public interface ErrorDtoMatcher extends Matcher<ErrorDto> {
	static ErrorDtoMatcher anErrorDtoThat() {
		return matcherOf(ErrorDtoMatcher.class);
	}

	static ErrorDtoMatcher anErrorDtoLike(final ErrorDto template) {
		return matcherOf(ErrorDtoMatcher.class).like(template);
	}

	ErrorDtoMatcher like(final ErrorDto template);

	ErrorDtoMatcher hasException(final String exception);

	ErrorDtoMatcher hasException(final Matcher<? super String> exceptionMatcher);

	ErrorDtoMatcher hasPath(final String path);

	ErrorDtoMatcher hasPath(final Matcher<? super String> pathMatcher);

	ErrorDtoMatcher hasError(final String error);

	ErrorDtoMatcher hasError(final Matcher<? super String> errorMatcher);

	ErrorDtoMatcher hasMessage(final String message);

	ErrorDtoMatcher hasMessage(final Matcher<? super String> messageMatcher);

	ErrorDtoMatcher hasStatus(final int status);

	ErrorDtoMatcher hasStatus(final Matcher<? super Integer> statusMatcher);

	ErrorDtoMatcher hasTimestamp(final ZonedDateTime timestamp);

	ErrorDtoMatcher hasTimestamp(final Matcher<? super ZonedDateTime> timestampMatcher);
}
