package com.svds.example.accesslog;

/**
 * @author rmagnus
 */
public interface GeoLocationService {
	
	GeoLocation find(String ipAddress) throws GeoLocationException;

}
