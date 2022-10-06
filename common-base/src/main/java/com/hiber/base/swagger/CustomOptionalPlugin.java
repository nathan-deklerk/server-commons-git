package com.hiber.base.swagger;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * Fix optional types.
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 90)
public class CustomOptionalPlugin implements ModelPropertyBuilderPlugin {
	@Override
	public void apply(ModelPropertyContext context) {
		if (context.getBeanPropertyDefinition().isPresent()) {
			ModelProperty property = context.getBuilder().build();
			Class<?> rawType = context.getBeanPropertyDefinition().get().getField().getRawType();
			if (Optional.class.equals(rawType)) {
				if (LocalDate.class.equals(property.getType().getErasedType()))
					context.getBuilder().type(context.getResolver().resolve(java.sql.Date.class));
				if (ZonedDateTime.class.equals(property.getType().getErasedType()))
					context.getBuilder().type(context.getResolver().resolve(Date.class));
			}
		}
	}

	@Override
	public boolean supports(DocumentationType delimiter) {
		return true;
	}
}
