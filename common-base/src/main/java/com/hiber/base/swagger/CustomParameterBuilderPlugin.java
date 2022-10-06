package com.hiber.base.swagger;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * To allow spring annotation {@link PathVariable#required()}, {@link RequestParam#required()} or
 * {@link RequestHeader#required()} override {@link io.swagger.annotations.ApiParam#required()}.
 * This is dirty fix.
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 100)
public class CustomParameterBuilderPlugin implements ParameterBuilderPlugin {
	@Override
	public void apply(ParameterContext context) {
		ResolvedMethodParameter resolvedMethodParameter = context.resolvedMethodParameter();
		PathVariable pathVariable = resolvedMethodParameter.findAnnotation(PathVariable.class).orNull();
		if (pathVariable != null) {
			context.parameterBuilder().required(pathVariable.required());
			return;
		}
		RequestParam requestParam = resolvedMethodParameter.findAnnotation(RequestParam.class).orNull();
		if (requestParam != null) {
			context.parameterBuilder().required(requestParam.required());
			return;
		}
		RequestHeader requestHeader = resolvedMethodParameter.findAnnotation(RequestHeader.class).orNull();
		if (requestHeader != null) {
			context.parameterBuilder().required(requestHeader.required());
		}
	}

	@Override
	public boolean supports(DocumentationType delimiter) {
		return true;
	}
}
