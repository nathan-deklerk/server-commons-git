package com.hiber.base.rest;

import com.hiber.base.rest.model.ErrorDto;
import java.time.ZonedDateTime;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public abstract class ControllerUtils {
	/**
	 * Extract path from request. If request is forward request than path is get from request attribute
	 * "javax.servlet.forward.request_uri".
	 *
	 * @param req Request data.
	 *
	 * @return Request path.
	 */
	public static String extractPath(HttpServletRequest req) {
		Object forwardRequestUri = req.getAttribute("javax.servlet.forward.request_uri");
		return forwardRequestUri != null ? forwardRequestUri.toString() : req.getRequestURI();
	}

	/**
	 * Response.
	 *
	 * @param ex Exception for logging purpose.
	 * @param message Error message send to client.
	 * @param req Source http servlet request.
	 * @param status Response status.
	 *
	 * @return Response.
	 */
	public static ResponseEntity<ErrorDto> response(Throwable ex, String message, HttpServletRequest req, HttpStatus status) {
		ErrorDto error = new ErrorDto(
				status.getReasonPhrase(),
				ex.getClass().getSimpleName(),
				message,
				extractPath(req),
				status.value(),
				ZonedDateTime.now()
		);
		return response(error, ex, message, status);
	}

	/**
	 * Response.
	 *
	 * @param body Data tha will be generated as body.
	 * @param ex Exception for logging purpose.
	 * @param message Error message send to client.
	 * @param status Response status.
	 *
	 * @return Response.
	 */
	public static <T> ResponseEntity<T> response(T body, Throwable ex, String message, HttpStatus status) {
		if (log.isDebugEnabled())
			log.info("{}", message, ex);
		else
			log.info("{}: {} {}", message, ex.getClass().getSimpleName(), ex.getMessage());
		return ResponseEntity.status(status.value())
				.body(body);
	}
}