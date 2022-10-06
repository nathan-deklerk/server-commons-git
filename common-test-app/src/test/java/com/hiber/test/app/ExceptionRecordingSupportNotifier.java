package com.hiber.test.app;

import com.hiber.base.config.SupportConfig.SupportNotifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link SupportNotifier} that simply records all exceptions and allows to check them later.
 */
public class ExceptionRecordingSupportNotifier implements SupportNotifier {
	private final List<Throwable> exceptions = new ArrayList<>();

	@Override
	public void notify(final Throwable throwable) {
		exceptions.add(throwable);
	}

	public List<Throwable> getExceptions() {
		return exceptions;
	}

	public void clear() {
		exceptions.clear();
	}
}