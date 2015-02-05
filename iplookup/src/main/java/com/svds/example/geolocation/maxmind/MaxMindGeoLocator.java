package com.svds.example.geolocation.maxmind;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.IspResponse;
import com.maxmind.geoip2.record.Location;
import com.svds.example.accesslog.GeoLocation;
import com.svds.example.accesslog.GeoLocationException;
import com.svds.example.accesslog.GeoLocationService;
import com.svds.example.annotations.MaxMindDB;
import com.svds.example.annotations.MaxMindDB.Type;

/**
 * @author rmagnus
 */
public class MaxMindGeoLocator implements GeoLocationService {

	private Logger logger = LogManager.getLogger();
	private File cityDbFile;
	private volatile DatabaseReader cityDbReader;
	private File ispDbFile;
	private volatile DatabaseReader ispDbReader;
	
	/**
	 * 
	 */
	public MaxMindGeoLocator() {}
	
	@Inject
	private MaxMindGeoLocator(@Named("MaxMind city") File cityDB, @Named("MaxMind isp") File ispDB) {
		this.cityDbFile = cityDB;
		this.ispDbFile = ispDB;
	}

	public void setCityDatabase(File f) {
		this.cityDbFile = f;
	}
	
	public File getCityDatabase() { return cityDbFile; }
	
	public void setIspDatabase(File f) {
		this.ispDbFile = f;
	}
	
	public File getIspDatabase() { return ispDbFile; }
	
	public DatabaseReader getCityDbReader() throws IOException {
		DatabaseReader readr = cityDbReader;
		if (null == readr) {
			synchronized(this) {
				readr = cityDbReader;
				if (null == readr) {
					cityDbReader = readr = new DatabaseReader.Builder(getCityDatabase()).build();
				}
			}
		}
		return readr;
	}
	
	public DatabaseReader getIspDbReader() throws IOException {
		DatabaseReader readr = ispDbReader;
		if (null == readr) {
			synchronized(this) {
				readr = ispDbReader;
				if (null == readr) {
					ispDbReader = readr = new DatabaseReader.Builder(getIspDatabase()).build();
				}
			}
		}
		return readr;
	}
	
	/**
	 * @see com.svds.example.accesslog.GeoLocationService#find(java.lang.String)
	 */
	@Override
	public GeoLocation find(String ipAddress) throws GeoLocationException {
		if (StringUtils.isBlank(ipAddress)) { return null; }
		
		InetAddress inetAddr = null;
		try {
			inetAddr = InetAddress.getByName(ipAddress);
		} 
		catch (UnknownHostException e) {
			throw new GeoLocationException("UnknownHost: " + ipAddress,e).setContextValue("ipAddress", ipAddress);
		}
		
		GeoLocation result = new GeoLocation();
		result.setIpAddress(ipAddress);
		
		DatabaseReader cityReader = null;
		try {
			cityReader = getCityDbReader();
		} catch (IOException e) {
			throw new GeoLocationException("IOException trying to build a new database.\n" + ExceptionUtils.getMessage(e),e).setContextValue("ipAddress", ipAddress);
		}
		
		try {
			CityResponse response = cityReader.city(inetAddr);
			Location location = response.getLocation();
			result.setLatitude(location.getLatitude());
			result.setLongitude(location.getLongitude());
		} 
		catch (IOException e) {
			throw new GeoLocationException("IOException querying the GeoLocation database about a city for an IP address.\n" + ExceptionUtils.getMessage(e),e).setContextValue("ipAddress", ipAddress);
		} catch (GeoIp2Exception e) {
			throw new GeoLocationException("GeoIp2Exception querying the GeoLocation database about a city for an IP address.\n" + ExceptionUtils.getMessage(e),e).setContextValue("ipAddress", ipAddress);
		}

		if (null == getIspDatabase()) {
			logger.trace("Unable to find the ISP database.");
		}
		else if (getIspDatabase().canRead()) {
			DatabaseReader ispReader = null;
			try {
				ispReader = getIspDbReader();
			} catch (IOException e) {
				throw new GeoLocationException("IOException trying to build a new database.\n" + ExceptionUtils.getMessage(e),e).setContextValue("ipAddress", ipAddress);
			}
			
			try {
				IspResponse response = ispReader.isp(inetAddr);
				result.setOrganization(response.getOrganization());
				result.setNameOfIsp(response.getIsp());
			} 
			catch (IOException e) {
				throw new GeoLocationException("IOException querying the GeoLocation database about the ISP for an IP address.\n" + ExceptionUtils.getMessage(e),e).setContextValue("ipAddress", ipAddress);
			} catch (GeoIp2Exception e) {
				throw new GeoLocationException("GeoIp2Exception querying the GeoLocation database about the ISP for an IP address.\n" + ExceptionUtils.getMessage(e),e).setContextValue("ipAddress", ipAddress);
			}
		}
		else {
			logger.debug("Cannot open the ISP database at {}",getIspDatabase());
		}
		
		return result;
	}

}
