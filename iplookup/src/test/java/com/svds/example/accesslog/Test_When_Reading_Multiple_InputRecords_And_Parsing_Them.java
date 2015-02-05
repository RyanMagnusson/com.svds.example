package com.svds.example.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author rmagnus
 *
 */
public class Test_When_Reading_Multiple_InputRecords_And_Parsing_Them {

	private Logger logger = LogManager.getLogger();
	private LogReader reader;
	private Injector guice;
	
	private static final InputRecord[] EXPECTED_RECORDS = new InputRecord[] {
		InputRecord.fromAddress("198.0.200.105")
		           .whenProcessed(new DateTime(2014, 1, 14, 9, 36, 50, DateTimeZone.forOffsetHours(-8)))
		           .httpMethodUsed("GET")
		           .uriRequested("/svds.com/rockandroll")
		           .referedFrom((String)null)
		           .httpStatusReturned(301)
		           .sizeOfTheResponse(241)
		           .browserUsed("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36")
		           .create()
	 , InputRecord.fromAddress("198.0.200.105")
                  .whenProcessed(new DateTime(2014, 1, 14, 9, 36, 50, DateTimeZone.forOffsetHours(-8)))
                  .httpMethodUsed("GET")
                  .uriRequested("/svds.com/rockandroll")
                  .referedFrom((String)null)
                  .httpStatusReturned(200)
                  .sizeOfTheResponse(8301)
                  .browserUsed("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36")
                  .create()
     , InputRecord.fromAddress("64.132.218.186")
                  .whenProcessed(new DateTime(2014, 10, 22, 6, 15, 14, DateTimeZone.forOffsetHours(-7)))
                  .httpMethodUsed("GET")
                  .uriRequested("/svds.com/rockandroll/fonts/icons/entypo.ttf")
                  .referedFrom("http://svds.com/rockandroll/css/gumby.css")
                  .httpStatusReturned(404)
                  .sizeOfTheResponse(4030)
                  .browserUsed("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.104 Safari/537.36")
                  .create()
	};
	private static final String[] MOCKED_LOG_RECORDS = new String[] {
		"198.0.200.105 - - [14/Jan/2014:09:36:50 -0800] \"GET /svds.com/rockandroll HTTP/1.1\" 301 241 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36\""
	  , "198.0.200.105 - - [14/Jan/2014:09:36:50 -0800] \"GET /svds.com/rockandroll/ HTTP/1.1\" 200 8301 \"-\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36\""
	  , "64.132.218.186 - - [22/Oct/2014:06:15:14 -0700] \"GET /svds.com/rockandroll/fonts/icons/entypo.ttf HTTP/1.1\" 404 4030 \"http://svds.com/rockandroll/css/gumby.css\" \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.104 Safari/537.36\""
	};	
	
	static class GuiceModule extends com.google.inject.AbstractModule {

		@Override
		protected void configure() {
			
			BufferedReader bufferedIn = Mockito.mock(BufferedReader.class);
			try {
				Mockito.when(bufferedIn.readLine()).thenReturn(MOCKED_LOG_RECORDS[0], MOCKED_LOG_RECORDS[1], MOCKED_LOG_RECORDS[2]);
			}
			catch (IOException e) {
				throw new RuntimeException("Not sure how, but an IOException was thrown while mocking the BufferedReader for testing",e);
			}
			
			AccessLogReader reader = new AccessLogReader();
			reader.setReader(bufferedIn);
			
			bind(LogReader.class).toInstance(reader);
			bind(Parser.class).to(InputRecordParser.class);
		}
		
	}
	
	private List<InputRecord> records;
	
	@Before 
	public void setupBeforeEveryTest() {
		guice = Guice.createInjector(new GuiceModule());
		records = new ArrayList<InputRecord>();
	}

	@Test
	public void Then_It_Should_Read_All_The_Records_Without_Any_Errors() throws IOException {
		int count = 0;
		reader = guice.getInstance(LogReader.class);
		try {
			records.add(reader.readLine());
			count++;
			records.add(reader.readLine());
			count++;
			records.add(reader.readLine());
			count++;
		}
		catch (IOException e) {
			logger.error("An unexpected IOException was thrown after reading " + count + " records.\n" + ExceptionUtils.getMessage(e));
			fail("An unexpected IOException was thrown. See log for details.");
		}
	}
	
	
	@Test
	public void Then_It_Should_Read_All_The_Records_Accurately() throws IOException {
		reader = guice.getInstance(LogReader.class);
		try {
			records.add(reader.readLine());
			records.add(reader.readLine());
			records.add(reader.readLine());
		}
		catch (IOException e) {
			logger.error("An unexpected IOException was thrown while trying to read all the records.\n" + ExceptionUtils.getMessage(e));
			fail("An unexpected IOException was thrown. See log for details.");
		}
		assertFalse(records.isEmpty());
		assertEquals(3, records.size());
		
		assertEquals(EXPECTED_RECORDS[0].getRemoteHost(), records.get(0).getRemoteHost());
		assertEquals(EXPECTED_RECORDS[0].getClientRequestUri(), records.get(0).getClientRequestUri());
		assertEquals(EXPECTED_RECORDS[0].getHttpReferer(), records.get(0).getHttpReferer());
		assertFalse(EXPECTED_RECORDS[0].getWhenRequestProcessed().isAfter(records.get(0).getWhenRequestProcessed()));
		assertFalse(EXPECTED_RECORDS[0].getWhenRequestProcessed().isBefore(records.get(0).getWhenRequestProcessed()));
		
		assertEquals(EXPECTED_RECORDS[2].getRemoteHost(), records.get(2).getRemoteHost());
		assertEquals(EXPECTED_RECORDS[2].getClientRequestUri(), records.get(2).getClientRequestUri());
		assertEquals(EXPECTED_RECORDS[2].getHttpReferer(), records.get(2).getHttpReferer());
		assertFalse(EXPECTED_RECORDS[2].getWhenRequestProcessed().isAfter(records.get(2).getWhenRequestProcessed()));
		assertFalse(EXPECTED_RECORDS[2].getWhenRequestProcessed().isBefore(records.get(2).getWhenRequestProcessed()));
		
	}
}
