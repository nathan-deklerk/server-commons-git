package junit;

import com.hiber.test.junit.RunScriptTestPolicy;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RunScriptTestPolicyTest {

	@Test
	public void testClassShouldBeIgnored_whenScriptMinorVersionNumberIsLessBy2() throws Exception {
		RunScriptTestPolicy runScriptTestPolicy = new RunScriptTestPolicy("1.50.1", "1.48.1");

		assertThat(runScriptTestPolicy.testClassShouldBeIgnored(), is(true));
	}

	@Test
	public void testClassShouldBeIgnored_whenVersionHasOnlyMajorAndMinor() throws Exception {
		RunScriptTestPolicy runScriptTestPolicy = new RunScriptTestPolicy("1.50", "1.48");

		assertThat(runScriptTestPolicy.testClassShouldBeIgnored(), is(true));
	}

	@Test
	public void testClassShouldNotBeIgnored_whenScriptMinorVersionNumberIsNotLessBy2() throws Exception {
		RunScriptTestPolicy runScriptTestPolicy = new RunScriptTestPolicy("1.50.1", "1.49.1");

		assertThat(runScriptTestPolicy.testClassShouldBeIgnored(), is(false));
	}

	@Test
	public void testClassShouldNotBeIgnored_whenScriptMajorVersionNumberIsGreaterThanAppVersion() throws Exception {
		RunScriptTestPolicy runScriptTestPolicy = new RunScriptTestPolicy("1.50.1", "2.0.0");

		assertThat(runScriptTestPolicy.testClassShouldBeIgnored(), is(false));
	}
}
