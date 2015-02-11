package com.svds.example.geolocation.selenium.whatismyip;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.svds.example.accesslog.GeoLocation;
import com.svds.example.accesslog.GeoLocationException;
import com.svds.example.accesslog.GeoLocationService;
import com.svds.example.geolocation.GeoLocationDAO;

/**
 * @author rmagnus
 */
public class WhatIsMyIpGeoLocationService implements GeoLocationService {

	private transient Logger logger = LogManager.getLogger(); 
	private volatile WebDriver driver;
	
	private GeoLocationDAO dao;
	public void setDAO(GeoLocationDAO dao) {
		this.dao = dao;
	}
	
	public GeoLocationDAO getDAO() {
		return dao;
	}
	
	public void setDriver(WebDriver driver) {
		synchronized(this) {
			this.driver = driver;
		}
	}

	public WebDriver getDriver() {
		WebDriver drvr = driver;
		if (null == drvr) {
			synchronized(this) {
				drvr = driver;
				if (null == drvr) {
					driver = drvr = new FirefoxDriver();
				}
			}
		}
		return drvr;
	}
	
	public WhatIsMyIpGeoLocationService() {}
	
	@Inject
	public WhatIsMyIpGeoLocationService(GeoLocationDAO dao, WebDriver driver) {
		this.dao = dao;
		this.driver = driver;
	}
		
	GeoLocation meld (Ip2LocationResults ip2Location, IpAddressLabsResults ipAddressLabs) {
		if (null == ip2Location) {
			return null == ipAddressLabs ? null : ipAddressLabs;
		}
		
		if (null == ipAddressLabs) {
			return ip2Location;
		}
		
		GeoLocation location = ip2Location.copy();
		if (StringUtils.isBlank(location.getNameOfIsp())) {
			location.setNameOfIsp(ipAddressLabs.getNameOfIsp());
		}
		
		if (null != ipAddressLabs.getLatitude()) {
			location.setLatitude(ipAddressLabs.getLatitude());
		}
		
		if (null != ipAddressLabs.getLongitude()) {
			location.setLongitude(ipAddressLabs.getLongitude());
		}
		
		return location;
	}
	
	/**
	 * @see com.svds.example.accesslog.GeoLocationService#find(java.lang.String)
	 */
	@Override
	public GeoLocation find(String ipAddress) throws GeoLocationException {
		if (StringUtils.isBlank(ipAddress)) { return null; }
		
		GeoLocation location = getDAO().find(ipAddress);
		if (null != location) { 
			logger.trace("found a saved entry for IP address: {}",ipAddress);
			return location; 
		}
		
		location = new GeoLocation();
		location.setIpAddress(ipAddress);
		
		IpAddressLookupPage page = new IpAddressLookupPage(getDriver()).open();
		IpAddressLookupPage results = page.submitQuery(ipAddress.trim());
		
		Ip2LocationResults ip2Results = results.getIp2LocationResults();
		IpAddressLabsResults ipAddressLabsResults = results.getIpAddressLabsResults();
		
		location = meld(ip2Results,ipAddressLabsResults);
		
		getDAO().save(location);
		return location;
	}

}
