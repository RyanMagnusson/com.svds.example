package com.svds.example.geolocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.svds.example.accesslog.GeoLocation;
import com.svds.example.accesslog.GeoLocationException;
import com.svds.example.annotations.DatabaseFile;
import com.svds.example.geolocation.sqlite.SqliteGeoLocationDAO;


public class Test_When_Retrieving_A_GeoLocation_From_Local_Database {

	private static final String DATA_DIRECTORY = "target/data/";
	private static final String DATABASE_FILE = DATA_DIRECTORY + "geolocations.sqlite";
	private static Injector guice;
	private static GeoLocation recordForDatabase;
	private static final String IP_ADDRESS = "127.0.0.1";
	
	static class GuiceModule extends AbstractModule {

		@Override
		protected void configure() {
			bindConstant().annotatedWith(DatabaseFile.class).to(DATABASE_FILE);
			bind(GeoLocationDAO.class).to(SqliteGeoLocationDAO.class);
		}
	}
	
	
	@BeforeClass
	public static void setUpOnlyOnce() throws Exception {
		guice = Guice.createInjector(new GuiceModule());
		File directory = new File(DATA_DIRECTORY);
		if (!directory.exists()) {
			directory.mkdir();
		}
		
		recordForDatabase = new GeoLocation();
		recordForDatabase.setIpAddress(IP_ADDRESS);
		recordForDatabase.setLatitude(36.3018D);
		recordForDatabase.setLongitude(-94.1215D);
		recordForDatabase.setNameOfIsp("AT&T U-Verse");
		recordForDatabase.setOrganization("AS7018 AT&T Services, Inc.");
		// need to make sure it is there before we query...
		SqliteGeoLocationDAO dao = (SqliteGeoLocationDAO)guice.getInstance(GeoLocationDAO.class);
		dao.save(recordForDatabase);
		
		
	}

	@Before
	public void setupForEachTest() throws Exception {
		
	}
	
	@AfterClass
	public static void tearDownAfterAllTheTests() {
		guice = null;
	}
	
	@Test
	public void Then_The_Record_Will_Be_Successfully_Read() {
		try {
			GeoLocation result = guice.getInstance(GeoLocationDAO.class).find(IP_ADDRESS);
			assertNotNull(result);
			assertEquals(IP_ADDRESS, result.getIpAddress());
		} catch (GeoLocationException e) {
			LogManager.getLogger().error("An unexpected GeoLocationException was thrown while testing that a GeoLocation can be read from a local database",e);	
			fail("An unexpected GeoLocationException was thrown. See log for details...");
		}
	}

}
