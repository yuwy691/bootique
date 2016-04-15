package com.nhl.bootique.test.junit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.nhl.bootique.Bootique;
import com.nhl.bootique.test.BQTestDaemon;

/**
 * Manages a "daemon" Bootique stack within a lifecycle of the a JUnit test.
 * This allows to start background servers so that tests can execute requests
 * against them, etc.
 * <p>
 * Instances should be annotated within the unit tests with {@link Rule} or
 * {@link ClassRule}. E.g.:
 * 
 * <pre>
 * public class MyTest {
 * 
 * 	&#64;Rule
 * 	public BQDaemonTestFactory testFactory = new BQDaemonTestFactory();
 * }
 * </pre>
 * 
 * @since 0.15
 */
public class BQJUnitDaemonFactory implements TestRule {

	protected Collection<BQJUnitDaemon> daemons;

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
		this.daemons = new ArrayList<>();
	}

	protected void stop() {
		Collection<BQJUnitDaemon> localDaemons = this.daemons;

		if (localDaemons != null) {
			localDaemons.forEach(d -> {
				try {
					d.stop();
				} catch (Exception e) {
					// ignore...
				}
			});
		}
	}

	public Builder newDaemon() {
		return new Builder(daemons, BQJUnitDaemon.builder());
	}

	public static class Builder {

		private com.nhl.bootique.test.junit.BQJUnitDaemon.Builder daemonBuilder;
		private Collection<BQJUnitDaemon> daemons;

		public Builder(Collection<BQJUnitDaemon> daemons,
				com.nhl.bootique.test.junit.BQJUnitDaemon.Builder daemonBuilder) {
			this.daemons = daemons;
			this.daemonBuilder = daemonBuilder;
		}

		public Builder property(String key, String value) {
			daemonBuilder.property(key, value);
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
			daemonBuilder.configurator(configurator);
			return this;
		}

		public Builder startupCheck(Function<BQTestDaemon, Boolean> startupCheck) {
			daemonBuilder.startupCheck(startupCheck);
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
			daemonBuilder.startupAndWaitCheck();
			return this;
		}

		public Builder startupTimeout(long timeout, TimeUnit unit) {
			daemonBuilder.startupTimeout(timeout, unit);
			return this;
		}

		/**
		 * Builds a daemon that ca
		 * 
		 * @param args
		 *            String[] emulating command-line arguments passed to a Java
		 *            app.
		 * @return {@link BQJUnitDaemon} instance created by the builder. The
		 *         caller doesn't need to shut it down. JUnit lifecycle takes
		 *         care of it.
		 */
		public BQJUnitDaemon build(String... args) {
			BQJUnitDaemon daemon = daemonBuilder.build(args);
			daemons.add(daemon);
			daemon.start();
			return daemon;
		}
	}
}
