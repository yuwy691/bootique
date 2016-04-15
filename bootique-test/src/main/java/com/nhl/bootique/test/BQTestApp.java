package com.nhl.bootique.test;

import java.util.function.Consumer;

import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.log.DefaultBootLogger;

/**
 * A base class of test "shells" that allow to run various modules in Bootique
 * environment.
 * 
 * @since 0.14
 */
public class BQTestApp {

	private InMemoryPrintStream stdout;
	private InMemoryPrintStream stderr;
	private Consumer<Bootique> configurator;
	private String[] args;

	private BQRuntime runtime;

	public BQTestApp(Consumer<Bootique> configurator, String... args) {
		this.stdout = new InMemoryPrintStream(System.out);
		this.stderr = new InMemoryPrintStream(System.err);
		this.configurator = configurator;
		this.args = args;
	}

	public void start() {
		this.runtime = createRuntime(args);
	}

	public void stop() {
		BQRuntime localRuntime = this.runtime;
		this.runtime = null;

		if (localRuntime != null) {
			localRuntime.shutdown();
		}
	}

	/**
	 * @since 0.16
	 * @return internal BQRuntime.
	 */
	public BQRuntime getRuntime() {
		return runtime;
	}

	public String getStdout() {
		return stdout.toString();
	}

	public String getStderr() {
		return stderr.toString();
	}

	protected BootLogger createBootLogger() {
		return new DefaultBootLogger(true, stdout, stderr);
	}

	protected BQRuntime createRuntime(String... args) {
		Bootique bootique = Bootique.app(args).bootLogger(createBootLogger());
		configurator.accept(bootique);
		return bootique.createRuntime();
	}

	/**
	 * Executes runtime default command, returning the outcome.
	 */
	public CommandOutcome run() {
		return runtime.getRunner().run();
	}
}
