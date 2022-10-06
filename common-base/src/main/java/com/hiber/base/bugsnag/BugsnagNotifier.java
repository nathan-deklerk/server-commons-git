package com.hiber.base.bugsnag;

import com.bugsnag.Bugsnag;
import com.hiber.base.config.SupportConfig.SupportNotifier;
import com.hiber.base.domain.Support;
import javax.management.relation.RelationSupportMBean;

/**
 * Used to send exceptions to Bugsnag on demand.
 */
public class BugsnagNotifier  {
	/**
	 * Notify Bugsnag.
	 *
	 * @param throwable {@link Throwable}.
	 */
	public static void notifyBugsnag(Throwable throwable) {
		String bugsnagApiKey = System.getenv("HIBER_BUGSNAG_API_KEY");
		if (bugsnagApiKey != null) {
			Bugsnag bugsnag = BugsnagFactory.create(bugsnagApiKey);
			bugsnag.notify(throwable);
		}
	}
}
