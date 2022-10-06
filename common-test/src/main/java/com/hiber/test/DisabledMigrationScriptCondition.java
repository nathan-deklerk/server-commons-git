package com.hiber.test;

import com.hiber.test.junit.RunScriptTestPolicy;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

/**
 * Handles {@link MigrationScript} annotation by disabling migration scripts tests that were added in older versions of the
 * module.
 * <p/>
 * The exact logic of deciding if given version is old enough to be disabled is placed in {@link RunScriptTestPolicy}.
 */
class DisabledMigrationScriptCondition implements ExecutionCondition {
	private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled("@MigrationScript is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
		Optional<AnnotatedElement> element = extensionContext.getElement();
		Optional<MigrationScript> annotation = findAnnotation(element, MigrationScript.class);
		return annotation.map(a -> {
			try {
				boolean ignored = new RunScriptTestPolicy(a.version()).testClassShouldBeIgnored();
				if (ignored) {
					String reason = element.map(e -> e + " is disabled").orElse("");
					return ConditionEvaluationResult.disabled(reason);
				}
				else
					return ENABLED;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).orElse(ENABLED);
	}
}