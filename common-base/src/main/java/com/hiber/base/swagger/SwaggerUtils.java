package com.hiber.base.swagger;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import springfox.documentation.service.ApiKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class SwaggerUtils {
	/**
	 * @return Security with "Authorization" header.
	 */
	public static ApiKey authorizationHeader() {
		return new ApiKey("Authorization header: e.g. oAuth Bearer Token", "Authorization", "header");
	}

	/**
	 * @return Security with "X-API-Key" header.
	 */
	public static ApiKey customXApiKeyHeader() {
		return new ApiKey("Api Auth Key", "X-API-Key", "header");
	}

	/**
	 * @return Security with "X-Auth-Key" header.
	 */
	public static ApiKey customerXAuthKeyHeader() {
		return new ApiKey("Customer Auth Key", "X-Auth-Key", "header");
	}
}