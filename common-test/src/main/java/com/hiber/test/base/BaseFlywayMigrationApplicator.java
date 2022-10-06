package com.hiber.test.base;

import com.hiber.test.MigrationScript;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import wiremock.com.google.common.collect.ObjectArrays;

/**
 * Base class for tests of migration scripts.
 * Child should have {@link MigrationScript} annotation and also should provide {@link DataSource}.
 */
public interface BaseFlywayMigrationApplicator {

	/**
	 * Data source to which you will apply migrations.
	 */
	DataSource getDataSource();

	@BeforeEach
	default void prepareDb() {
		MigrationScriptVersion migrationScriptVersion = getCurrentlyRunningScriptVersion();

		cleanAndMigrate(migrationScriptVersion);
	}

	/**
	 * Get version of currently running script of child class.
	 *
	 * @return Version script in format like '2.10.3'
	 */
	private MigrationScriptVersion getCurrentlyRunningScriptVersion() {
		Class<?> testClass = this.getClass();
		if (!testClass.isAnnotationPresent(MigrationScript.class)) {
			throw new RuntimeException("MigrationScript annotation is not placed on test class");
		}
		MigrationScript migrationScript = testClass.getAnnotation(MigrationScript.class);
		return MigrationScriptVersion.fromDotSeparatedString(migrationScript.version());
	}

	/**
	 * Migrate database schema to version from {@link MigrationScript} annotation.
	 */
	default void migrate() {
		buildFlyway(getCurrentlyRunningScriptVersion().toDotSeparatedString())
				.migrate();
	}

	/**
	 * Clean and migrate database schema to indicated version.
	 *
	 * @param targetVersion Requested schema version.
	 */
	private void cleanAndMigrate(MigrationScriptVersion targetVersion) {
		Flyway flyway = buildFlyway(
				targetVersion.getPrevious().format(),
				getScriptLocation(targetVersion)
		);
		flyway.clean();
		flyway.migrate();
	}

	/**
	 * Build flyway configuration object.
	 *
	 * @param targetVersion Requested schema version.
	 *
	 * @return Configured flyway object.
	 */
	private Flyway buildFlyway(String targetVersion, String... location) {
		return Flyway.configure()
				.dataSource(getDataSource())
				.table("schema_version")
				.locations(ObjectArrays.concat("classpath:/db/migration", location))
				.target(targetVersion)
				.load();
	}

	/**
	 * Get migration script file location.
	 */
	private String getScriptLocation(MigrationScriptVersion version) {
		return "classpath:/migrationscript/" + "V" + version.toUnderscoreSeparatedString();
	}
}
