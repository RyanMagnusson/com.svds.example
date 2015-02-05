package com.svds.example.geolocation.ipinfo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.svds.example.accesslog.GeoLocation;
import com.svds.example.accesslog.GeoLocationException;
import com.svds.example.accesslog.GeoLocationService;
import com.svds.example.annotations.DatabaseFile;
import com.svds.example.annotations.ServiceURI;
import com.svds.example.geolocation.GeoLocationDAO;
import com.svds.example.geolocation.sqlite.SqliteGeoLocationDAO;

public class IntegratedTest_When_Calling_The_IpInfo_GeoLocation_Service_For_A_Valid_IP_Address {

	private Logger logger = LogManager.getLogger();
	private Injector guice;
	private static final String ipAddress = "64.132.218.186";
	private static final String DATA_DIRECTORY = "target/data/";
	private static final String DATABASE_FILE = DATA_DIRECTORY + "geolocations.sqlite";
	
	static class GuiceModule extends AbstractModule {

		@Override
		protected void configure() {
			bindConstant().annotatedWith(ServiceURI.class).to("http://ipinfo.io");
			bindConstant().annotatedWith(DatabaseFile.class).to(DATABASE_FILE);
			bind(GeoLocationDAO.class).to(SqliteGeoLocationDAO.class);
			bind(GeoLocationService.class).to(IpInfoGeoLocationService.class);
		}
	}
	
	@Before
	public void setUp() throws Exception {
		guice = Guice.createInjector(new GuiceModule());
	}

	@After
	public void tearDown() throws Exception {
		guice = null;
	}

	@Test
	public void Then_It_Should_Not_Throw_An_Error() {
		try {
			guice.getInstance(GeoLocationService.class).find(ipAddress);
		}
		catch (Exception e) {
			logger.error("An unexpected Exception was thrown while querying the MaxMind geo location database\n" + ExceptionUtils.getMessage(e),e);
			fail("An unexpected Exception was thrown while querying the MaxMind geo location database. See log for details.");
		}
	}

	@Test
	public void Then_It_Should_Return_A_Valid_GeoLocation() throws GeoLocationException {
		
		GeoLocation location = guice.getInstance(GeoLocationService.class).find(ipAddress);
		assertNotNull(location);
	}
}
