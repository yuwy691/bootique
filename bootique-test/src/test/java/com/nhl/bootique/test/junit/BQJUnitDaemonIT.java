package com.nhl.bootique.test.junit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

public class BQJUnitDaemonIT {

	@Rule
	public BQJUnitDaemon daemon = BQJUnitDaemon.builder().startupAndWaitCheck().build("a1", "a2");

	@Test
	public void testDIInternals() {

		assertTrue(daemon.getOutcome().isPresent());
		assertEquals(0, daemon.getOutcome().get().getExitCode());
		assertArrayEquals(new String[] { "a1", "a2" }, daemon.getApp().getRuntime().getArgs());
		assertTrue("help wasn't printed by the daemon", daemon.getApp().getStdout().contains("--config"));
	}

}
