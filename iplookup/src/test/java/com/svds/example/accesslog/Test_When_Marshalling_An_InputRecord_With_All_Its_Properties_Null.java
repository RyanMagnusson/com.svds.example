package com.svds.example.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;


public class Test_When_Marshalling_An_InputRecord_With_All_Its_Properties_Null {

	private Logger logger = LogManager.getLogger();
	private InputRecord record;
	private final static String EXPECTED_RESULT = "{\"remoteHost\":null"
			                                    + ",\"identdUserId\":null"
			                                    + ",\"httpAuthUserName\":null"
			                                    + ",\"whenRequestProcessed\":null"
			                                    + ",\"requestedUri\":null"
			                                    + ",\"httpMethod\":null"
			                                    + ",\"httpVersion\":null"
			                                    + ",\"httpStatus\":null"
			                                    + ",\"responseSize\":null"
			                                    + ",\"httpReferer\":null"
			                                    + ",\"userAgent\":null}"
			                                    ;
	
	@Before
	public void setupEachTest() {
		record = new InputRecord();
	}
	
	
	@Test
	public void Then_It_Should_Not_Throw_Any_Errors() {
		try {
			logger.info(record.toString());
		}
		catch(Exception e) {
			logger.error(e.getMessage(),e);
			fail();
		}
	}
	
	@Test
	public void Then_It_Should_Be_Formatted_Properly() {
		assertEquals("The #toString() result did not format properly", EXPECTED_RESULT, record.toString());
	}

}
