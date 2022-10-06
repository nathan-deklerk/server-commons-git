package com.hiber.test.app;

import com.bugsnag.Bugsnag;
import com.hiber.base.config.SupportConfig.SupportNotifier;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@Tag("IntegrationTest")
@Import(WebErrorNotifyingTest.TestConfiguration.class)
@SpringBootTest(
		webEnvironment = WebEnvironment.RANDOM_PORT,
		properties = "hiber.bugsnag.api-key=KEY-00001"
)
class WebErrorNotifyingTest {
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private ExceptionRecordingSupportNotifier exceptionRecordingSupportNotifier;

	@MockBean
	private Bugsnag bugsnag;

	@BeforeEach
	public void resetRecorders() {
		exceptionRecordingSupportNotifier.clear();
	}

	/**
	 * Tests that any uncaught exception occurring in the web (REST) layer will be forwarded to configured notifiers. This
	 * represents a scenario where some unexpected error happens during processing of an HTTP request.
	 */
	@Test
	void uncaughtErrorInWebShouldBeReportedToNotifiers() {
		final ResponseEntity<String> response =
				testRestTemplate.exchange("/unmapped", HttpMethod.GET, RequestEntity.EMPTY, String.class);

		assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
		final Throwable parsingException = exceptionRecordingSupportNotifier.getExceptions().get(0);
		assertThat(parsingException.getClass(), is(UnmappedInternalException.class));
	}

	/**
	 * Tests that if an exceptions occurs in the web (REST) layer but is mapped explicitly to some status (HTTP response) then
	 * such exception is not forwarded to configured notifiers.
	 */
	@Test
	void mappedErrorInWebShouldNotBeReportedToNotifiers() {
		final ResponseEntity<String> response =
				testRestTemplate.exchange("/mapped", HttpMethod.GET, RequestEntity.EMPTY, String.class);

		assertThat(response.getStatusCode(), is(HttpStatus.CONFLICT));
		assertThat(exceptionRecordingSupportNotifier.getExceptions(), is(empty()));
	}

	/**
	 * Tests that a mapped exception should not be in any way forwarded to Bugsnag. This verifies that errors happening in the web
	 * (REST) layer are handled only by our supporting code. Specifically it makes sure that no built-in Bugsnag related
	 * configuration takes place.
	 */
	@Test
	void mappedErrorInWebShouldNotBeHandledDirectlyByBugsnag() {
		testRestTemplate.exchange("/mapped", HttpMethod.GET, RequestEntity.EMPTY, String.class);

		Mockito.verifyZeroInteractions(bugsnag);
	}

	/**
	 * Test-specific configuration that sets an example REST controller and supporting classes allowing to test if errors thrown
	 * by the controller are handled appropriately.
	 */
	@org.springframework.boot.test.context.TestConfiguration
	public static class TestConfiguration {
		@Bean
		public SomeRestController someRestController() {
			return new SomeRestController();
		}

		/**
		 * Defines a mapping of an exception to an HTTP status. Such exceptions should be considered as handled and not reported
		 * to the configured notifiers.
		 */
		@RestControllerAdvice
		public static class CustomErrorController {
			@ExceptionHandler(MappedConflictException.class)
			ResponseEntity<String> handleNotFoundException(
					final MappedConflictException exception,
					final HttpServletRequest request
			) {
				return ResponseEntity.status(HttpStatus.CONFLICT).build();
			}
		}

		@Bean
		public SupportNotifier exceptionRecordingSupportNotifier() {
			return new ExceptionRecordingSupportNotifier();
		}
	}

	/**
	 * Example REST controller that provides endpoints allowing to exercise handling both mapped and unmapped exceptions.
	 */
	@RestController
	public static class SomeRestController {
		@GetMapping("/mapped")
		public String mappedException() {
			throw new MappedConflictException();
		}


		@GetMapping("/unmapped")
		public String unmappedException() {
			throw new UnmappedInternalException();
		}
	}

	/**
	 * An exception that is mapped to some HTTP status.
	 */
	public static class MappedConflictException extends RuntimeException {
	}

	/**
	 * An unmapped exception.
	 */
	public static class UnmappedInternalException extends RuntimeException {
	}
}