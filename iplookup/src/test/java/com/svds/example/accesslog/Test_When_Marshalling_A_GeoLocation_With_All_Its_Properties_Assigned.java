package com.svds.example.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;


public class Test_When_Marshalling_A_GeoLocation_With_All_Its_Properties_Assigned {

	private Logger logger = LogManager.getLogger();
	private GeoLocation location;
	
	private final static String EXPECTED_IP_ADDRESS = "192.168.27.1";
	private final static String EXPECTED_ORGANIZATION = "Mystery House";
	private final static String EXPECTED_LATITUDE = "360.7654321";
	private final static String EXPECTED_LONGITUDE = "190.0987654";
	private final static String EXPECTED_ISP_NAME = "AT&T";
	
	private final static String EXPECTED_RESULT = "{\"ipAddress\":\"" + EXPECTED_IP_ADDRESS + "\""
			                                    + ",\"organization\":\"" + EXPECTED_ORGANIZATION + "\""
			                                    + ",\"latitude\":" + EXPECTED_LATITUDE
			                                    + ",\"longitude\":" + EXPECTED_LONGITUDE
			                                    + ",\"ispName\":\"" + EXPECTED_ISP_NAME + "\"}"
			                                    ; 
	
	@Before
	public void setupEachTest() {
		location = new GeoLocation();
		location.setIpAddress(EXPECTED_IP_ADDRESS);
		location.setOrganization(EXPECTED_ORGANIZATION);
		location.setLatitude(360.765432109876D); // this also validates that it remains in the proper precision
		location.setLongitude(190.0987654D);
		location.setNameOfIsp(EXPECTED_ISP_NAME);
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
