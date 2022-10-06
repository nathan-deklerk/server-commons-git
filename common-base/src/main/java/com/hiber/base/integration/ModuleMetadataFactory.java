package com.hiber.base.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides metadata about this module.
 * </p>
 * The metadata is read from <code>META-INF/build-info.properties</code> file that is normally generated during build.
 */
public class ModuleMetadataFactory {
	private static final Logger logger = LoggerFactory.getLogger(ModuleMetadataFactory.class);

	public static ModuleMetadata create() {
		try {
			Properties properties = new Properties();
			InputStream in = ModuleMetadataFactory.class.getResourceAsStream("/META-INF/build-info.properties");
			if (in == null) {
				logger.warn("Module metadata requested but could not be read: /META-ING/build-info.properties is not available.");
				return new ModuleMetadata(Optional.empty(), Optional.empty());
			}
			properties.load(in);
			return new ModuleMetadata(
					Optional.ofNullable(properties.getProperty("build.version")),
					Optional.ofNullable(properties.getProperty("build.artifact"))
			);
		}
		catch (IOException e) {
			logger.warn(String.format("Module metadata requested but could not be read: %s.", e.getMessage()));
			return new ModuleMetadata(Optional.empty(), Optional.empty());
		}
	}

	@Value
	public static class ModuleMetadata {
		/**
		 * The version of the module.
		 */
		private final Optional<String> version;

		/**
		 * The name of the module.
		 */
		private final Optional<String> name;
	}
}
