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

public class Test_When_Parsing_A_Valid_Record_Where_The_Referer_Is_Not_Logged {

	private Logger logger = LogManager.getLogger();
	private static final String LINE = "198.0.200.105 - - [14/Jan/2014:09:36:50 -0800] \"GET /svds.com/rockandroll HTTP/1.1\" 301 241 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36\"";
	private Parser parser;
	private InputRecord expectedResult;
	private InputRecord parsedResult;
	
	private final static String EXPECTED_IP_ADDRESS = "198.0.200.105";
	private final static DateTime TIMESTAMP = new DateTime(2014, 1, 14, 9, 36, 50, DateTimeZone.forOffsetHours(-8));
	private final static String EXPECTED_WHEN_REQUEST_PROCESSED = "2014-01-14T17:36:50+0000";
	private final static String EXPECTED_REQUEST_URI = "/svds.com/rockandroll";
	private final static String EXPECTED_HTTP_METHOD = "GET";
	private final static String EXPECTED_HTTP_VERSION = "HTTP/1.1";
	private final static int EXPECTED_HTTP_STATUS = 301;
	private final static int EXPECTED_RESPONSE_SIZE = 241;
	private final static String EXPECTED_HTTP_REFERER = null;
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
            + ",\"httpReferer\":null"
            + ",\"userAgent\":\"" + EXPECTED_USER_AGENT + "\"}"
            ;
	
	@Before
	public void setupBeforeEachTest() {
		parser = new InputRecordParser();
		parsedResult = null;
		
		expectedResult = new InputRecord();
		expectedResult.setRemoteHost(EXPECTED_IP_ADDRESS);
		expectedResult.setClientRequestUri(EXPECTED_REQUEST_URI);
		expectedResult.setHttpMethod(EXPECTED_HTTP_METHOD);
		expectedResult.setHttpVersion(EXPECTED_HTTP_VERSION);
		expectedResult.setHttpStatus(EXPECTED_HTTP_STATUS);
		expectedResult.setResponseSize(EXPECTED_RESPONSE_SIZE);
		expectedResult.setHttpReferer(EXPECTED_HTTP_REFERER);
		expectedResult.setUserAgent(EXPECTED_USER_AGENT);
		expectedResult.setWhenRequestProcessed(TIMESTAMP);
	}
	
	@Test
	public void Then_No_Errors_Should_Have_Been_Thrown() {
		try {
			parsedResult = parser.parse(LINE);
		}
		catch (Exception e) {
			logger.error("An unexpected error was thrown.\n" + ExceptionUtils.getMessage(e),e);
			fail("An unexpected error was thrown.");
		}
	}
	
	@Test
	public void Then_An_Actual_Result_Should_Have_Been_Returned() {
		
		parsedResult = parser.parse(LINE);
		assertNotNull(parsedResult);
	}
	
	@Test
	public void Then_The_Text_Should_Have_Been_Parsed_Correctly() {
		
		parsedResult = parser.parse(LINE);
		assertEquals(EXPECTED_IP_ADDRESS, parsedResult.getRemoteHost());
		logger.debug("Validating that the dates are equal => expected: " + TIMESTAMP.toString() + ", parsed: " + parsedResult.getWhenRequestProcessed().toString());
		
		assertFalse(parsedResult.getWhenRequestProcessed().isBefore(TIMESTAMP));
		assertFalse(parsedResult.getWhenRequestProcessed().isAfter(TIMESTAMP));
		assertEquals(EXPECTED_REQUEST_URI, parsedResult.getClientRequestUri());
		assertEquals(EXPECTED_HTTP_METHOD, parsedResult.getHttpMethod());
		assertEquals(EXPECTED_HTTP_VERSION, parsedResult.getHttpVersion());
		
		assertNotNull(parsedResult.getHttpStatus());
		assertEquals(EXPECTED_HTTP_STATUS, parsedResult.getHttpStatus().intValue());
		
		assertNotNull(parsedResult.getResponseSize());
		assertEquals(EXPECTED_RESPONSE_SIZE, parsedResult.getResponseSize().intValue());
		assertEquals(EXPECTED_HTTP_REFERER, parsedResult.getHttpReferer());
		assertEquals(EXPECTED_USER_AGENT, parsedResult.getUserAgent());
		assertEquals(EXPECTED_HTTP_REFERER, parsedResult.getHttpReferer());
		
	}

	/**
	 * Although this logic is validated elsewhere, this use case
	 * validates that the PST/PDT timestamp is rendered correctly.
	 */
	@Test
	public void Then_The_Text_Should_Be_Formatted_Correctly() {
		parsedResult = parser.parse(LINE);
		assertEquals(EXPECTED_RESULT, parsedResult.toString());
	}
	
}
