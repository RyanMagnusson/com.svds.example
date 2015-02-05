package com.svds.example.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;


public class Test_When_Marshalling_An_InputRecord_With_All_Its_Properties_Assigned {

	private Logger logger = LogManager.getLogger();
	private InputRecord record;
	protected static final String LABEL_CLIENT_REQUEST_URI = "requestedUri";
	protected static final String LABEL_HTTP_METHOD = "httpMethod";
	protected static final String LABEL_HTTP_VERSION = "httpVersion";
	protected static final String LABEL_HTTP_STATUS = "httpStatus";
	protected static final String LABEL_RESPONSE_SIZE = "responseSize";
	protected static final String LABEL_HTTP_REFERER = "httpReferer";
	protected static final String LABEL_USER_AGENT = "userAgent";
	
	
	private final static String EXPECTED_IP_ADDRESS = "192.25.72.12";
	private final static DateTime TIMESTAMP = new DateTime(1976, 6, 18, 9, 27, 35, DateTimeZone.UTC);
	private final static String EXPECTED_WHEN_REQUEST_PROCESSED = "1976-06-18T09:27:35+0000";
	private final static String EXPECTED_REQUEST_URI = "/want.fri.es/with/that";
	private final static String EXPECTED_HTTP_METHOD = "POST";
	private final static String EXPECTED_HTTP_VERSION = "HTTP/92.7";
	private final static int EXPECTED_HTTP_STATUS = 404;
	private final static int EXPECTED_RESPONSE_SIZE = 1979;
	private final static String EXPECTED_HTTP_REFERER = "http://www.eat-healthy.com/index.html";
	private final static String EXPECTED_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36";

	
	private final static String EXPECTED_RESULT = "{\"remoteHost\":\"" + EXPECTED_IP_ADDRESS + "\""
			                                    + ",\"identdUserId\":null"
			                                    + ",\"httpAuthUserName\":null"
			                                    + ",\"whenRequestProcessed\":\"" + EXPECTED_WHEN_REQUEST_PROCESSED + "\""
			                                    + ",\"requestedUri\":\"" + EXPECTED_REQUEST_URI + "\""
			                                    + ",\"httpMethod\":\"" + EXPECTED_HTTP_METHOD + "\""
			                                    + ",\"httpVersion\":\"" + EXPECTED_HTTP_VERSION + "\""
			                                    + ",\"httpStatus\":" + EXPECTED_HTTP_STATUS
			                                    + ",\"responseSize\":" + EXPECTED_RESPONSE_SIZE
			                                    + ",\"httpReferer\":\"" + EXPECTED_HTTP_REFERER + "\""
			                                    + ",\"userAgent\":\"" + EXPECTED_USER_AGENT + "\"}"
			                                    ;
	
	@Before
	public void setupEachTest() {
		record = new InputRecord();
		record.setRemoteHost(EXPECTED_IP_ADDRESS);
		record.setClientRequestUri(EXPECTED_REQUEST_URI);
		record.setWhenRequestProcessed(TIMESTAMP);
		record.setHttpMethod(EXPECTED_HTTP_METHOD);
		record.setHttpVersion(EXPECTED_HTTP_VERSION);
		record.setHttpStatus(EXPECTED_HTTP_STATUS);
		record.setResponseSize(EXPECTED_RESPONSE_SIZE);
		record.setHttpReferer(EXPECTED_HTTP_REFERER);
		record.setUserAgent(EXPECTED_USER_AGENT);
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
