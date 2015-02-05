package com.svds.example.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;


public class Test_When_Marshalling_A_GeoLocation_With_All_Its_Properties_Null {

	private Logger logger = LogManager.getLogger();
	private GeoLocation location;
	private final static String EXPECTED_RESULT = "{\"ipAddress\":null"
			                                    + ",\"organization\":null"
			                                    + ",\"latitude\":null"
			                                    + ",\"longitude\":null"
			                                    + ",\"ispName\":null}"
			                                    ; 
	
	@Before
	public void setupEachTest() {
		location = new GeoLocation();
	}
	
	
	@Test
	public void Then_It_Should_Not_Throw_Any_Errors() {
		try {
			logger.info(location);
		}
		catch(Exception e) {
			logger.error(e.getMessage(),e);
			fail();
		}
	}
	
	@Test
	public void Then_It_Should_Be_Formatted_Properly() {
		assertEquals("The #toString() result did not format properly", EXPECTED_RESULT, location.toString());
	}

}
