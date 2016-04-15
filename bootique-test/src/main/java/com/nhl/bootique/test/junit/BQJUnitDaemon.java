package com.nhl.bootique.test.junit;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.nhl.bootique.Bootique;
import com.nhl.bootique.test.BQTestApp;
import com.nhl.bootique.test.BQTestDaemon;

public class BQJUnitDaemon extends BQTestDaemon implements TestRule {

	public static Builder builder() {
		return new Builder();
	}

	protected BQJUnitDaemon(BQTestApp app, Function<BQTestDaemon, Boolean> startupCheck, long timeout,
			TimeUnit timeoutUnit) {
		super(app, startupCheck, timeout, timeoutUnit);
	}

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

	public static class Builder {

		private static final Function<BQTestDaemon, Boolean> AFFIRMATIVE_STARTUP_CHECK = runtime -> true;

		private com.nhl.bootique.test.junit.BQJUnitApp.Builder appBuilder;
		protected Function<BQTestDaemon, Boolean> startupCheck;
		protected long startupTimeout;
		protected TimeUnit startupTimeoutTimeUnit;

		protected Builder() {
			this.startupTimeout = 5;
			this.startupTimeoutTimeUnit = TimeUnit.SECONDS;
			this.appBuilder = BQJUnitApp.builder();
			this.startupCheck = AFFIRMATIVE_STARTUP_CHECK;
		}

		public Builder property(String key, String value) {
			appBuilder.property(key, value);
			return this;
		}

		/**
		 * Appends configurator to any existing configurators.
		 * 
		 * @param configurator
		 *            configurator function.
		 * @return this builder.
		 */
		public Builder configurator(Consumer<Bootique> configurator) {
			appBuilder.configurator(configurator);
			return this;
		}

		public Builder startupCheck(Function<BQTestDaemon, Boolean> startupCheck) {
			this.startupCheck = Objects.requireNonNull(startupCheck);
			return this;
		}

		/**
		 * Adds a startup check that waits till the runtime finishes, within the
		 * startup timeout bounds.
		 * 
		 * @since 0.16
		 * @return this builder
		 */
		public Builder startupAndWaitCheck() {
			this.startupCheck = (runtime) -> runtime.getOutcome().isPresent();
			return this;
		}

		public Builder startupTimeout(long timeout, TimeUnit unit) {
			this.startupTimeout = timeout;
			this.startupTimeoutTimeUnit = unit;
			return this;
		}

		/**
		 * Builds a daemon that ca
		 * 
		 * @param args
		 *            String[] emulating command-line arguments passed to a Java
		 *            app.
		 * @return {@link BQDaemonTestRuntime} instance created by the builder.
		 *         The caller doesn't need to shut it down. Usually JUnit
		 *         lifecycle takes care of it.
		 */
		public BQJUnitDaemon build(String... args) {
			BQJUnitApp app = appBuilder.build(args);
			return createDaemon(app);
		}

		protected BQJUnitDaemon createDaemon(BQJUnitApp app) {
			return new BQJUnitDaemon(app, startupCheck, startupTimeout, startupTimeoutTimeUnit);
		}
	}

}
