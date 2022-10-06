package com.hiber.base.swagger;

import org.springframework.core.annotation.Order;
import springfox.bean.validators.plugins.schema.NotNullAnnotationPlugin;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * To allow {@link javax.validation.constraints.NotNull} annotation to override
 * {@link io.swagger.annotations.ApiModelProperty#required()}.
 * This is dirty fix. See https://github.com/springfox/springfox/issues/1231.
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 100)
public class CustomNotNullAnnotationPlugin extends NotNullAnnotationPlugin {
}
