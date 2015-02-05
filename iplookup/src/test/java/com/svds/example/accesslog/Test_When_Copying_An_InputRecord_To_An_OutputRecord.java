package com.svds.example.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class Test_When_Copying_An_InputRecord_To_An_OutputRecord {

	private final static Logger logger = LogManager.getLogger();
	private final static String EXPECTED_IP_ADDRESS = "198.0.200.105";
	private final static DateTime TIMESTAMP = new DateTime(2014, 1, 14, 9, 36, 50, DateTimeZone.forOffsetHours(-8));
	private final static String EXPECTED_REQUEST_URI = "/svds.com/rockandroll";
	private final static String EXPECTED_HTTP_METHOD = "GET";
	private final static String EXPECTED_HTTP_VERSION = "HTTP/1.1";
	private final static int EXPECTED_HTTP_STATUS = 301;
	private final static int EXPECTED_RESPONSE_SIZE = 241;
	private final static String EXPECTED_HTTP_REFERER = null;
	private final static String EXPECTED_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36";
	private final static String EXPECTED_ORGANIZATION = "Mystery House";
	private final static String EXPECTED_ISP_NAME = "AT&T";
	
	private InputRecord inputRec;
	private OutputRecord outRec;
	private GeoLocationService locationService;
	private GeoLocation location;
	private Main main;
	
	@Before
	public void setupBeforeEachTest() throws Exception {
		outRec = null;
		
		location = new GeoLocation();
		location.setIpAddress(EXPECTED_IP_ADDRESS);
		location.setLatitude(360.7654321D);
		location.setLatitude(190.0987654D);
		location.setOrganization(EXPECTED_ORGANIZATION);
		location.setNameOfIsp(EXPECTED_ISP_NAME);
		
		locationService = Mockito.mock(GeoLocationService.class);
		Mockito.when(locationService.find(Mockito.anyString())).thenReturn(location);
		
		main = new Main(null,null,locationService);
		
		inputRec = new InputRecord();
		inputRec.setRemoteHost(EXPECTED_IP_ADDRESS);
		inputRec.setClientRequestUri(EXPECTED_REQUEST_URI);
		inputRec.setHttpMethod(EXPECTED_HTTP_METHOD);
		inputRec.setHttpVersion(EXPECTED_HTTP_VERSION);
		inputRec.setHttpStatus(EXPECTED_HTTP_STATUS);
		inputRec.setResponseSize(EXPECTED_RESPONSE_SIZE);
		inputRec.setHttpReferer(EXPECTED_HTTP_REFERER);
		inputRec.setUserAgent(EXPECTED_USER_AGENT);
		inputRec.setWhenRequestProcessed(TIMESTAMP);
	}

	@Test
	public void Then_No_Errors_Should_Be_Thrown() {
		try {
			main.createFrom(inputRec);
		}
		catch (Exception e) {
			logger.error("An unexpected Exception was caught.\n" + ExceptionUtils.getMessage(e),e);
			fail("An unexpected Exception was caught");
		}
	}
	
	@Test
	public void Then_All_Of_The_Correct_Property_Values_Should_Be_Set() {
		outRec = main.createFrom(inputRec);
		assertEquals(inputRec.getRemoteHost(), outRec.getIpAddress());
		assertEquals(inputRec.getClientRequestUri(), outRec.getClientRequestedUri());
		assertEquals(inputRec.getHttpReferer(), outRec.getHttpReferer());
		assertFalse(inputRec.getWhenRequestProcessed().isBefore(outRec.getWhenRequestProcessed()));
		assertFalse(inputRec.getWhenRequestProcessed().isAfter(outRec.getWhenRequestProcessed()));
		
		assertNotNull(outRec.getGeoLocation());
		assertEquals(location.getIpAddress(), outRec.getIpAddress());
		assertEquals(location.getOrganization(), outRec.getGeoLocation().getOrganization());
		assertEquals(location.getNameOfIsp(), outRec.getGeoLocation().getNameOfIsp());
		assertEquals(location.getLongitude(), outRec.getGeoLocation().getLongitude());
		assertEquals(location.getLatitude(), outRec.getGeoLocation().getLatitude());
	}
	
}
