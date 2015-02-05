package com.svds.example.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;

/**
 * @author rmagnus
 *
 */
public class Test_Formatting_An_OutputRecord_To_A_String_With_All_Its_Properties_Null {

private Logger logger = LogManager.getLogger();
	
	private Injector guice;
	private OutputRecord record;
	
	private final static String EXPECTED_IP_ADDRESS = "192.168.27.1";
	private final static String EXPECTED_ORGANIZATION = "Mystery House";
	private final static String EXPECTED_LATITUDE = "360.7654321";
	private final static String EXPECTED_LONGITUDE = "190.0987654";
	private final static String EXPECTED_ISP_NAME = "AT&T";
	private final static DateTime TIMESTAMP = new DateTime(1976, 6, 18, 9, 27, 35, DateTimeZone.UTC);
	private final static String EXPECTED_WHEN_REQUEST_PROCESSED = "1976-06-18T09:27:35+0000";
	private final static String EXPECTED_REQUEST_URI = "/more.sug.ar";
	private final static String EXPECTED_HTTP_REFERER = "http://www.diabetes.com/index.html";
	
	private final static String EXPECTED_RESULT = "{\"whenRequestProcessed\":null"
												+ ",\"clientRequestURI\":null"
												+ ",\"httpReferer\":null"
												+ ",\"ipAddress\":null"
												+ ",\"geolocation\":null"
												+ "}"
												;
	
	@Before 
	public void setupBeforeEveryTest() {
		record = new OutputRecord();
//		record.setIpAddress(EXPECTED_IP_ADDRESS);
//		record.setWhenRequestProcessed(TIMESTAMP);
//        record.setHttpReferer(EXPECTED_HTTP_REFERER);
//        record.setClientRequestedUri(EXPECTED_REQUEST_URI);
//        GeoLocation location = new GeoLocation();
//        location.setIpAddress(EXPECTED_IP_ADDRESS);
//		location.setOrganization(EXPECTED_ORGANIZATION);
//		location.setLatitude(360.765432109876D); // this also validates that it remains in the proper precision
//		location.setLongitude(190.0987654D);
//		location.setNameOfIsp(EXPECTED_ISP_NAME);
//		record.setGeoLocation(location);
	}

	@Test
	public void Then_It_Not_Should_Not_Throw_An_Error() {
		
		try {
			record.toString();
		}
		catch (Exception e) {
				logger.error("An unexpected Exception was thrown while formatting an output record into JSON format\n" + ExceptionUtils.getMessage(e),e);
				fail("An unexpected Exception was thrown while formatting an output record into JSON format. See log for details.");
		}
	}
	
	
	@Test
	public void Then_The_Record_Should_Be_Formatted_Properly() throws IOException {
		final String result = record.toString();
		logger.debug("OutputRecord#toString() => " + result);
		
		assertNotNull(result);
		assertEquals(EXPECTED_RESULT,result);
	}
}
