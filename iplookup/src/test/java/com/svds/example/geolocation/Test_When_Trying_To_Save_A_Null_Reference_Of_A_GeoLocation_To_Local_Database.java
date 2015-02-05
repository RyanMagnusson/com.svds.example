package com.svds.example.geolocation;

import static org.junit.Assert.assertEquals;
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

public class Test_When_Trying_To_Save_A_Null_Reference_Of_A_GeoLocation_To_Local_Database {

	private static final String DATA_DIRECTORY = "target/data/";
	private static final String DATABASE_FILE = DATA_DIRECTORY + "geolocations.sqlite";
	private static Injector guice;
	private GeoLocation location;
	
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
		
		
		
	}

	@Before
	public void setupForEachTest() {}
	
	@AfterClass
	public static void tearDownAfterAllTheTests() {
		guice = null;
	}
	
	@Test
	public void Then_No_Error_Should_Occur_And_Zero_Records_Updated() {
		try {
			int count = guice.getInstance(GeoLocationDAO.class).save(null);
			assertEquals(0,count);
		} catch (GeoLocationException e) {
			LogManager.getLogger().error("An unexpected GeoLocationException was thrown while testing that a GeoLocation can be saved in local database",e);	
			fail("An unexpected GeoLocationException was thrown. See log for details...");
		}
	}

}
