package com.svds.example.geolocation;

import com.svds.example.accesslog.GeoLocation;
import com.svds.example.accesslog.GeoLocationException;
import com.svds.example.accesslog.GeoLocationService;

/**
 * @author rmagnus
 */
public interface GeoLocationDAO extends GeoLocationService {

	int save(GeoLocation location) throws GeoLocationException;
	
}
