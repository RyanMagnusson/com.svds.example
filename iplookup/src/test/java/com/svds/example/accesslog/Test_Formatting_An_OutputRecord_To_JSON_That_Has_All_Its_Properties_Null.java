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

import com.google.gson.JsonSerializer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

/**
 * @author rmagnus
 *
 */
public class Test_Formatting_An_OutputRecord_To_JSON_That_Has_All_Its_Properties_Null {

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
	
	private final static String EXPECTED_RESULT = "{\"whenRequestProcessed\":\"" + EXPECTED_WHEN_REQUEST_PROCESSED + "\""
												+ ",\"clientRequestURI\":\"" + EXPECTED_REQUEST_URI + "\""
												+ ",\"httpReferer\":\"" + EXPECTED_HTTP_REFERER + "\""
												+ ",\"ipAddress\":\"" + EXPECTED_IP_ADDRESS + "\""
												 + ",\"organization\":\"" + EXPECTED_ORGANIZATION + "\""
			                                    + ",\"latitude\":" + EXPECTED_LATITUDE
			                                    + ",\"longitude\":" + EXPECTED_LONGITUDE
			                                    + ",\"ispName\":\"" + EXPECTED_ISP_NAME + "\""
												+ "}"
			                                    ; 
	
	public static class GuiceModule extends AbstractModule {

		@Override
		protected void configure() {
			
			bind(new TypeLiteral<JsonSerializer<GeoLocation>>() {}).to(GeoLocation.GsonAdapter.class).in(Scopes.SINGLETON);
			bind(new TypeLiteral<JsonSerializer<OutputRecord>>() {}).to(OutputRecord.GsonAdapter.class).in(Scopes.SINGLETON);
			bind(new TypeLiteral<JsonSerializer<InputRecord>>() {}).to(InputRecord.GsonAdapter.class).in(Scopes.SINGLETON);
			bind(new TypeLiteral<Formatter<OutputRecord>>() {}).to(OutputRecordJsonFormatter.class).in(Scopes.SINGLETON);
		}
	}
	
	
	@Before 
	public void setupBeforeEveryTest() {
		guice = Guice.createInjector(new GuiceModule()); 
		record = new OutputRecord();
		record.setIpAddress(EXPECTED_IP_ADDRESS);
		record.setWhenRequestProcessed(TIMESTAMP);
        record.setHttpReferer(EXPECTED_HTTP_REFERER);
        record.setClientRequestedUri(EXPECTED_REQUEST_URI);
        GeoLocation location = new GeoLocation();
        location.setIpAddress(EXPECTED_IP_ADDRESS);
		location.setOrganization(EXPECTED_ORGANIZATION);
		location.setLatitude(360.765432109876D); // this also validates that it remains in the proper precision
		location.setLongitude(190.0987654D);
		location.setNameOfIsp(EXPECTED_ISP_NAME);
		record.setGeoLocation(location);
	}

	@Test
	public void Then_It_Not_Throw_An_Error() {
		
		try {
			Formatter<OutputRecord> formatter = guice.getInstance(new Key<Formatter<OutputRecord>>() {});
			formatter.format(record);
		}
		catch (Exception e) {
				logger.error("An unexpected Exception was thrown while formatting an output record into JSON format\n" + ExceptionUtils.getMessage(e));
				fail("An unexpected Exception was thrown while formatting an output record into JSON format. See log for details.");
		}
	}
	
	
	@Test
	public void Then_It_Should_Format_The_Record_Properly() throws IOException {
		final Formatter<OutputRecord> formatter = guice.getInstance(new Key<Formatter<OutputRecord>>() {});
		final String result = formatter.format(record);
		logger.debug("OutputRecord[JSON] => " + result);
		
		assertNotNull(result);
		assertEquals(EXPECTED_RESULT,result);
	}
}
