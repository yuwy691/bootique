package com.nhl.bootique.test.junit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;

public class BQJunitAppFactoryIT {

	@Rule
	public BQJUnitAppFactory appFactory = new BQJUnitAppFactory();

	@Test
	public void testDIInternals() {
		BQJUnitApp app = appFactory.newApp().build("a1", "a2");
		assertNotNull(app.getRuntime());

		assertArrayEquals(new String[] { "a1", "a2" }, app.getRuntime().getArgs());
	}

}
