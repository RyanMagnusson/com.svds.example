package com.svds.example.geolocation;

import com.svds.example.accesslog.GeoLocationException;

/**
 * @author rmagnus
 */
public interface HybridGeoLocationDAO {

	int save(HybridGeoLocation location) throws GeoLocationException;
	
	HybridGeoLocation get(String address) throws GeoLocationException;
	
	HybridGeoLocation delete(String address) throws GeoLocationException;
	
}
