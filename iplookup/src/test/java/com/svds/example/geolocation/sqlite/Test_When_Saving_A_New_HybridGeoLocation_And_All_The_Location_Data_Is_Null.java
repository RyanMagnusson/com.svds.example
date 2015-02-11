package com.svds.example.geolocation.sqlite;

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
import com.google.inject.name.Names;
import com.svds.example.accesslog.GeoLocationException;
import com.svds.example.geolocation.HybridGeoLocation;
import com.svds.example.geolocation.HybridGeoLocationDAO;

public class Test_When_Saving_A_New_HybridGeoLocation_And_All_The_Location_Data_Is_Null {

	private static final String DATA_DIRECTORY = "target/data/";
	private static final String DATABASE_FILE = DATA_DIRECTORY + "geolocations.sqlite";
	private static Injector guice;
	
	private static final String IP_ADDRESS = "140.27.98.1";
	private HybridGeoLocation location;
	
	static class GuiceModule extends AbstractModule {

		@Override
		protected void configure() {
			bindConstant().annotatedWith(Names.named("hybrid db")).to(DATABASE_FILE);
			bind(HybridGeoLocationDAO.class).to(SqliteHybridGeoLocationDAO.class);
		}
	}
	
	
	@BeforeClass
	public static void setUpOnlyOnce() throws Exception {
		guice = Guice.createInjector(new GuiceModule());
		File directory = new File(DATA_DIRECTORY);
		if (!directory.exists()) {
			directory.mkdir();
		}
		guice.getInstance(HybridGeoLocationDAO.class).delete(IP_ADDRESS);
	}

	@Before
	public void setupForEachTest() {
		location = new HybridGeoLocation();
		location.setIpAddress(IP_ADDRESS);
	}
	
	@AfterClass
	public static void tearDownAfterAllTheTests() {
		guice = null;
	}
	
	@Test
	public void Then_No_Error_Should_Occur_And_Zero_Records_Updated() {
		try {
			int count = guice.getInstance(HybridGeoLocationDAO.class).save(location);
			assertEquals(1,count);
		} catch (GeoLocationException e) {
			LogManager.getLogger().error("An unexpected GeoLocationException was thrown while testing that a GeoLocation can be saved in local database",e);	
			fail("An unexpected GeoLocationException was thrown. See log for details...");
		}
	}

}
