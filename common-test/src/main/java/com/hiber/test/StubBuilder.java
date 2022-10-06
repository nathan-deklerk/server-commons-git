package com.hiber.test;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

public interface StubBuilder {
	String loadResource(String path);

	default void stubGetJson(String url, String responseJson, HttpStatus httpStatus) {
		stubFor(get(url)
				.willReturn(aResponse()
						.withStatus(httpStatus.value())
						.withHeader("Content-Type", "application/json")
						.withBody(responseJson)
				));
	}

	default void stubPostJson(String url, HttpStatus responseStatus) {
		stubFor(post(urlEqualTo(url))
				.willReturn(aResponse()
						.withStatus(responseStatus.value())
				));
	}

	default void stubPostJson(String url, HttpStatus responseStatus, String responseJson) {
		stubFor(post(urlEqualTo(url))
				.willReturn(aResponse()
						.withStatus(responseStatus.value())
						.withHeader("Content-Type", "application/json")
						.withBody(responseJson)
				));
	}

	default void stubPostJson(String url, String requestBody, HttpStatus responseStatus) {
		stubFor(post(urlEqualTo(url))
				.withHeader("Content-Type", containing("application/json"))
				.withRequestBody(equalToJson(requestBody))
				.willReturn(aResponse()
						.withStatus(responseStatus.value())
				));
	}

	default void stubPostJson(String url, HttpStatus responseStatus, String requestBody, String responseJson) {
		stubFor(post(urlEqualTo(url))
				.withHeader("Content-Type", containing("application/json"))
				.withRequestBody(equalToJson(requestBody))
				.willReturn(aResponse()
						.withStatus(responseStatus.value())
						.withHeader("Content-Type", "application/json")
						.withBody(responseJson)
				));
	}

	default void stubPutJson(String url, HttpStatus responseStatus) {
		stubFor(put(url)
				.withHeader("Content-Type", containing("application/json"))
				.willReturn(aResponse()
						.withStatus(responseStatus.value())
				));
	}

	default void stubPutJson(String url, String responseJson, HttpStatus responseStatus) {
		stubFor(put(url)
				.withHeader("Content-Type", containing("application/json"))
				.willReturn(aResponse()
						.withStatus(responseStatus.value())
						.withHeader("Content-Type", "application/json")
						.withBody(responseJson)
				));
	}

	default void stubPutJson(String url, HttpStatus responseStatus, String requestBody) {
		stubFor(put(url)
				.withHeader("Content-Type", containing("application/json"))
				.withRequestBody(equalToJson(requestBody))
				.willReturn(aResponse()
						.withStatus(responseStatus.value())
				));
	}

	default void stubPutJson(String url, HttpStatus responseStatus, String requestBody, String responseJson) {
		stubFor(put(url)
				.withHeader("Content-Type", containing("application/json"))
				.withRequestBody(equalToJson(requestBody))
				.willReturn(aResponse()
						.withStatus(responseStatus.value())
						.withHeader("Content-Type", "application/json")
						.withBody(responseJson)
				));
	}


	default void stubDeleteJson(String url, HttpStatus responseStatus) {
		WireMock.stubFor(
				delete(url)
						.withHeader("Content-Type", containing("application/json"))
						.willReturn(
								aResponse()
										.withStatus(responseStatus.value())
										.withHeader("Content-Type", "application/json")
						)
		);
	}

	default void stubVerifyPostJson(String url, String expectedRequestJson) {
		verify(postRequestedFor(urlEqualTo(url))
				.withHeader("Content-Type", containing("application/json"))
				.withRequestBody(equalToJson(expectedRequestJson, true, true))
		);
	}

	default void stubVerifyPutJson(String url, String expectedRequestJson) {
		verify(putRequestedFor(urlEqualTo(url))
				.withHeader("Content-Type", containing("application/json"))
				.withRequestBody(equalToJson(expectedRequestJson, true, true))
		);
	}

	default void verifyStubGet(String url) {
		verify(getRequestedFor(urlEqualTo(url))
				.withHeader("Content-Type", containing("application/json"))
		);
	}

	default void verifyStubDeleteJson(String url, String expectedRequestJson) {
		WireMock.verify(
				deleteRequestedFor(WireMock.urlEqualTo(url))
						.withHeader("Content-Type", containing("application/json"))
						.withRequestBody(WireMock.equalToJson(expectedRequestJson, true, true)));
	}
}