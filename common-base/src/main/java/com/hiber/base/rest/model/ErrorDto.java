package com.hiber.base.rest.model;

import io.swagger.annotations.ApiModelProperty;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Return error object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ErrorDto {
	@ApiModelProperty("Error name")
	private final String error;
	@ApiModelProperty("Exception that cause error")
	private final String exception;
	@ApiModelProperty("Error message")
	private final String message;
	@ApiModelProperty("Request path")
	private final String path;
	@ApiModelProperty("Response status code")
	private final int status;
	@ApiModelProperty("Error timestamp")
	private final ZonedDateTime timestamp;
}
