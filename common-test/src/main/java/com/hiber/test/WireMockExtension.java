package com.hiber.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * An JUnit 5 extension that is a bare-bones replacement for JUnit 4 WireMockRule (a method-level rule).
 * <p/>
 * To use this extension annotate a <b>non-private</b> field being an instance of this class with
 * {@link org.junit.jupiter.api.extension.RegisterExtension}. A {@link WireMockServer} instance will be started before each test
 * method and stopped after method finishes its work. This extension also configures {@link WireMock} to use this instance of
 * <code>WireMockServer</code> as the default one so one can use all static methods provided by <code>WireMock</code> (e.g.
 * <code>stubFor</code>) to perform stubbing or verifying.
 * <p/>
 * Since this class actually extends <code>WireMockServer</code> it is also possibly to perform stubbing or verifying by calling
 * methods on the annotated field.
 */
public class WireMockExtension extends WireMockServer implements BeforeEachCallback, AfterEachCallback {
	public WireMockExtension(Options options) {
		super(options);
	}

	public WireMockExtension(int port) {
		this(wireMockConfig().port(port));
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) {
		start();
		WireMock.configureFor("localhost", port());
	}

	@Override
	public void afterEach(ExtensionContext extensionContext) {
		stop();
	}
}