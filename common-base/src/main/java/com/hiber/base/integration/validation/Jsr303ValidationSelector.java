package com.hiber.base.integration.validation;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.core.GenericSelector;

/**
 * Selector that checks if object validates jsr 303 constraints on {@code object}.
 */
@Slf4j
@RequiredArgsConstructor
public class Jsr303ValidationSelector implements GenericSelector<Object> {
	/**
	 * Validator used to validate beans.
	 */
	private final Validator validator;

	@Override
	public boolean accept(Object source) {
		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(source);
		if (!constraintViolations.isEmpty())
			throw new ConstraintViolationException("Bean validation violations: " + constraintViolations,
					constraintViolations);
		return true;
	}
}
