package com.hiber.base.config;


import com.hiber.base.domain.Support;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@Configuration
public class SupportConfig {
	@Bean
	public Support support(List<SupportNotifier> notifiers) {
		return throwable -> {
			for (SupportNotifier notifier : notifiers)
				notifier.notify(throwable);
		};
	}

	/**
	 * Represent a notifier, i.e. class that sends a notification in response to some error.
	 */
	public interface SupportNotifier {
		void notify(Throwable throwable);
	}

	@Bean
	public SupportNotifier loggingSupport() {
		return new LoggingNotifier();
	}

	public class LoggingNotifier implements SupportNotifier {
		private Logger logger = LoggerFactory.getLogger(getClass());

		@Override
		public void notify(Throwable throwable) {
			String message = String.format("Error occurred: %s.", throwable.getMessage());
			logger.error(message, throwable);
		}
	}

	// All modules are using Spring web layer - even if they won't expose any REST API they will provide Actuator endpoints. This
	// is why we always declare the bean below.
	@Bean
	public UncaughtExceptionMvcHandler uncaughtExceptionHandler(final Support support) {
		return new UncaughtExceptionMvcHandler(support);
	}

	/**
	 * Handles all uncaught exceptions propagated from MVC layer and send notification about them.
	 * <p/>
	 * Note that exceptions handled by {@link org.springframework.web.bind.annotation.ExceptionHandler} won't be forwarded here so
	 * if you want to send notifications about them you need to do that in your exception handler.
	 */
	@RequiredArgsConstructor
	public static class UncaughtExceptionMvcHandler implements HandlerExceptionResolver {
		private final Support support;

		@Override
		public ModelAndView resolveException(
				final HttpServletRequest httpServletRequest,
				final HttpServletResponse httpServletResponse,
				final Object o,
				final Exception e
		) {
			support.notify(e);
			return null;    // As per contract - allow other handlers to handle exception, usually by generating HTTP response.
		}
	}

	// This bean will be created even if no scheduled tasks are configured. There is no way around it as Spring Boot built-in
	// TaskSchedulingAutoConfiguration is triggered by the presence of ThreadPoolTaskScheduler, which being provided by the
	// spring-context library is in practice always present. It is not a big deal as the overhead is minimal and the configured
	// error handler will be never called if not task are scheduled.
	@Bean
	public TaskSchedulerCustomizer taskSchedulerCustomizer(final Support support) {
		return taskScheduler -> {
			taskScheduler.setErrorHandler(new NotifyingErrorHandler(support));
		};
	}

	@RequiredArgsConstructor
	private static class NotifyingErrorHandler implements ErrorHandler {
		private final Support support;

		@Override
		public void handleError(Throwable t) {
			support.notify(t);
		}
	}
}
