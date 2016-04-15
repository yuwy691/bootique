package com.nhl.bootique.test.junit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.multibindings.MapBinder;
import com.nhl.bootique.BQCoreModule;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.test.BQTestApp;

/**
 * A Bootique "app" stack that can be used as a JUnit test rule.
 */
public class BQJUnitApp extends BQTestApp implements TestRule {

	public static Builder builder() {
		return new Builder();
	}

	protected BQJUnitApp(Consumer<Bootique> configurator, String... args) {
		super(configurator, args);
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

		private static final Consumer<Bootique> DO_NOTHING_CONFIGURATOR = bootique -> {
		};

		private Consumer<Bootique> configurator;
		private Map<String, String> properties;

		Builder() {
			this.properties = new HashMap<>();
			this.configurator = DO_NOTHING_CONFIGURATOR;
		}

		public Builder property(String key, String value) {
			properties.put(key, value);
			return this;
		}

		public Builder configurator(Consumer<Bootique> configurator) {
			this.configurator = this.configurator.andThen(Objects.requireNonNull(configurator));
			return this;
		}

		public BQJUnitApp build(String... args) {

			Consumer<Bootique> localConfigurator = configurator;

			if (!properties.isEmpty()) {

				Consumer<Bootique> propsConfigurator = bootique -> bootique.module(binder -> {
					MapBinder<String, String> mapBinder = BQCoreModule.contributeProperties(binder);
					properties.forEach((k, v) -> mapBinder.addBinding(k).toInstance(v));
				});

				localConfigurator = localConfigurator.andThen(propsConfigurator);
			}

			return new BQJUnitApp(localConfigurator, args);
		}
	}
}
