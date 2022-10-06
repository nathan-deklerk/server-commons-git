package com.hiber.base.integration.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;

/**
 * Transforms object to json string using jackson 2.
 */
public class Jackson2ObjectToJsonTransformer extends ObjectToJsonTransformer {
	/**
	 * @param objectMapper Jackson object mapper.
	 */
	public Jackson2ObjectToJsonTransformer(ObjectMapper objectMapper) {
		super(new Jackson2JsonObjectMapper(objectMapper));
	}
}
