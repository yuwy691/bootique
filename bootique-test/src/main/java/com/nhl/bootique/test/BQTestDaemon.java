package com.nhl.bootique.test;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.log.BootLogger;

/**
 * A background runner for a {@link BQTestApp}.
 * 
 * @since 0.16
 */
public class BQTestDaemon {

	private BQTestApp app;
	private ExecutorService executor;
	private Function<BQTestDaemon, Boolean> startupCheck;
	private Optional<CommandOutcome> outcome;
	private long timeout;
	private TimeUnit timeoutUnit;

	public BQTestDaemon(BQTestApp app, Function<BQTestDaemon, Boolean> startupCheck, long timeout,
			TimeUnit timeoutUnit) {
		this.app = Objects.requireNonNull(app);
		this.startupCheck = Objects.requireNonNull(startupCheck);
		this.executor = Executors.newCachedThreadPool();
		this.outcome = Optional.empty();
		this.timeout = Objects.requireNonNull(timeout);
		this.timeoutUnit = Objects.requireNonNull(timeoutUnit);
	}

	public BQTestApp getApp() {
		return app;
	}

	/**
	 * @return an optional outcome, available if the test runtime has finished.
	 */
	public Optional<CommandOutcome> getOutcome() {
		return outcome;
	}

	public void start() {

		app.start();
		this.executor.submit(() -> outcome = Optional.of(app.run()));

		BootLogger logger = getApp().getRuntime().getBootLogger();

		Future<Boolean> startupFuture = executor.submit(() -> {

			try {
				while (!startupCheck.apply(this)) {
					logger.stderr("Daemon runtime hasn't started yet...");
					Thread.sleep(500);
				}

				return true;
			} catch (InterruptedException e) {
				logger.stderr("Timed out waiting for server to start");
				return false;
			} catch (Throwable th) {
				logger.stderr("Server error", th);
				return false;
			}

		});

		boolean success;
		try {
			success = startupFuture.get(timeout, timeoutUnit);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException(String.format("Daemon failed to start in %s ms", timeoutUnit.toMillis(timeout)));
		}

		if (success) {
			logger.stdout("Daemon runtime started successfully...");
		} else {
			throw new RuntimeException("Daemon failed to start");
		}
	}

	public void stop() {

		BootLogger logger = app.getRuntime().getBootLogger();
		app.stop();

		// must interrupt execution (using "shutdown()" is not enough to stop
		// Jetty for instance
		executor.shutdownNow();
		try {
			executor.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.stderr("Interrupted while waiting for shutdown", e);
		}
	}

}
