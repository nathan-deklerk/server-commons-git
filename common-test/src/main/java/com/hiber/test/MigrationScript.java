package com.hiber.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * This annotation can be used on migration scripts tests that can be disabled after few subsequent versions of the module as
 * there is little sense of running them by then.
 */
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisabledMigrationScriptCondition.class)
@Target(ElementType.TYPE)
public @interface MigrationScript {
	/**
	 * Version of the application in which the script was added.
	 */
	String version();
}