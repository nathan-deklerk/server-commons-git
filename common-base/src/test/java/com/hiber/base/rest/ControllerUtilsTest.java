package com.hiber.base.rest;

import com.hiber.base.rest.model.ErrorDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static com.hiber.base.matcher.http.ResponseEntityMatcher.ErrorDtoResponseEntityMatcher.anErrorDtoResponseEntityMatcher;
import static com.hiber.base.matcher.http.ResponseEntityMatcher.ObjectResponseEntityMatcher.anObjectResponseEntityMatcher;
import static com.hiber.base.matcher.rest.model.ErrorDtoMatcher.anErrorDtoThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class ControllerUtilsTest {
	@Test
	void extractPath_shouldExtractPathFromRequest() {
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest("GET", "/request_path");

		String path = ControllerUtils.extractPath(mockHttpServletRequest);
		assertThat(path, is("/request_path"));
	}

	@Test
	void extractPath_shouldExtractPathFromForwardedRequest() {
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest("GET", "/request_path");
		mockHttpServletRequest.setAttribute("javax.servlet.forward.request_uri", "/forwarded_request");

		String path = ControllerUtils.extractPath(mockHttpServletRequest);
		assertThat(path, is("/forwarded_request"));
	}

	@Test
	void response_shouldReturnErrorDtoResponse() {
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest("GET", "/request_path");
		ResponseEntity<ErrorDto> response = ControllerUtils.response(
				new RuntimeException("Test exception"),
				"Test message",
				mockHttpServletRequest,
				HttpStatus.CONFLICT
		);
		assertThat(response, anErrorDtoResponseEntityMatcher()
				.hasBody(anErrorDtoThat()
						.hasError("Conflict")
						.hasException("RuntimeException")
						.hasMessage("Test message")
						.hasPath("/request_path")
						.hasStatus(409)
						.hasTimestamp(notNullValue())
				)
				.hasStatusCode(HttpStatus.CONFLICT)
		);
	}

	@Test
	void response_shouldReturnCustomBodyResponse() {
		Object object = new Object();
		ResponseEntity<Object> response = ControllerUtils.response(
				object,
				new RuntimeException("Test body exception"),
				"Test body message",
				HttpStatus.PRECONDITION_FAILED
		);
		assertThat(response, anObjectResponseEntityMatcher()
				.hasBody(is(object))
				.hasStatusCode(HttpStatus.PRECONDITION_FAILED)
		);
	}
}