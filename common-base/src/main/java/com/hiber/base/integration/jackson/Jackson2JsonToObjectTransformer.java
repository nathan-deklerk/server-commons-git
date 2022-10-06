package com.hiber.base.integration.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;

/**
 * Transforms json string to object using jackson 2.
 */
public class Jackson2JsonToObjectTransformer extends JsonToObjectTransformer {
	/**
	 * @param targetClass Target class.
	 * @param objectMapper Jackson object mapper.
	 */
	public Jackson2JsonToObjectTransformer(Class<?> targetClass, ObjectMapper objectMapper) {
		super(targetClass, new Jackson2JsonObjectMapper(objectMapper));
	}
}
