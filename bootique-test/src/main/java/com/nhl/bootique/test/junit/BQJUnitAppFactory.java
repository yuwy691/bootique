package com.nhl.bootique.test.junit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.nhl.bootique.Bootique;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.test.BQTestApp;

/**
 * Manages a simple Bootique stack within a lifecycle of the a JUnit test. It
 * doesn't run any commands by default and is usually used for accessing
 * initialized standard services, such as {@link ConfigurationFactory}, etc.
 * <p>
 * Instances should be annotated within the unit tests with {@link Rule} or
 * {@link ClassRule}. E.g.:
 * 
 * <pre>
 * public class MyTest {
 * 
 * 	&#64;Rule
 * 	public BQTestFactory testFactory = new BQTestFactory();
 * }
 * </pre>
 * 
 * @since 0.15
 */
public class BQJUnitAppFactory implements TestRule {

	private Collection<BQTestApp> apps;

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				start();
				try {
					base.evaluate();
				} finally {
					stop();
				}
			}
		};
	}

	protected void start() {
		this.apps = new ArrayList<>();
	}

	protected void stop() {
		Collection<BQTestApp> localRuntimes = this.apps;

		if (localRuntimes != null) {
			localRuntimes.forEach(runtime -> {
				try {
					runtime.stop();
				} catch (Exception e) {
					// ignore...
				}
			});
		}
	}

	public Builder newApp() {
		return new Builder(apps);
	}

	public static class Builder {

		private Collection<BQTestApp> apps;
		private com.nhl.bootique.test.junit.BQJUnitApp.Builder appBuilder;

		private Builder(Collection<BQTestApp> apps) {
			this.apps = Objects.requireNonNull(apps);
			this.appBuilder = BQJUnitApp.builder();
		}

		public Builder property(String key, String value) {
			appBuilder.property(key, value);
			return this;
		}

		public Builder configurator(Consumer<Bootique> configurator) {
			appBuilder.configurator(configurator);
			return this;
		}

		public BQJUnitApp build(String... args) {
			BQJUnitApp app = appBuilder.build(args);
			app.start();
			apps.add(app);
			return app;
		}
	}
}
