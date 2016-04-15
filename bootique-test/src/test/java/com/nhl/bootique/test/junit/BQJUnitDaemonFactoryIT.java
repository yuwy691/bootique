package com.nhl.bootique.test.junit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

public class BQJUnitDaemonFactoryIT {

	@Rule
	public BQJUnitDaemonFactory daemonFactory = new BQJUnitDaemonFactory();

	@Test
	public void testDIInternals() {
		BQJUnitDaemon daemon = daemonFactory.newDaemon().startupAndWaitCheck().build("a1", "a2");

		assertTrue(daemon.getOutcome().isPresent());
		assertEquals(0, daemon.getOutcome().get().getExitCode());
		assertArrayEquals(new String[] { "a1", "a2" }, daemon.getApp().getRuntime().getArgs());
		assertTrue("help wasn't printed by the daemon", daemon.getApp().getStdout().contains("--config"));
	}

}
