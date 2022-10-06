package com.hiber.base.swagger;

import com.fasterxml.classmate.ResolvedType;
import java.time.Period;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * Add example values for additional types.
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 100)
public class CustomExamplePlugin implements ModelPropertyBuilderPlugin {
	@Override
	public void apply(ModelPropertyContext context) {
		if (context.getBeanPropertyDefinition().isPresent()) {
			ModelProperty property = context.getBuilder().build();
			if (!StringUtils.hasLength(property.getExample())) {
				ResolvedType type = property.getType();
				Class<?> rawType = type.getErasedType();
				if (UUID.class.equals(rawType))
					context.getBuilder().example("00000000-0000-0000-0000-000000000000");
				else if (Period.class.equals(rawType))
					context.getBuilder().example("P11M");
			}
		}
	}

	@Override
	public boolean supports(DocumentationType delimiter) {
		return true;
	}
}
