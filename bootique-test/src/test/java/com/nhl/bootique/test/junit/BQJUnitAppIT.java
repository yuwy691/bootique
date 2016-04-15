package com.nhl.bootique.test.junit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;

public class BQJUnitAppIT {

	@Rule
	public BQJUnitApp app = BQJUnitApp.builder().build("a1", "a2");

	@Test
	public void testDIInternals() {
		assertNotNull(app.getRuntime());
		assertArrayEquals(new String[] { "a1", "a2" }, app.getRuntime().getArgs());
	}

}
