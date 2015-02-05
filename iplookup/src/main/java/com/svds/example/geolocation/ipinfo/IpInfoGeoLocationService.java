package com.svds.example.geolocation.ipinfo;

import java.math.BigDecimal;
import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.svds.example.accesslog.GeoLocation;
import com.svds.example.accesslog.GeoLocationException;
import com.svds.example.accesslog.GeoLocationService;
import com.svds.example.annotations.ServiceURI;
import com.svds.example.geolocation.GeoLocationDAO;

/**
 * @author rmagnus
 *
 */
public class IpInfoGeoLocationService implements GeoLocationService {

	//private final Map<String,GeoLocation> locations = new HashMap<String,GeoLocation>();
	
	
	private static final String DEFAULT_URI = "http://ipinfo.io/";
	
	private volatile String uri;
	public void setURI(String uri) { synchronized(this) { this.uri = uri; } }
	public void setURI(URI uri) { 
		String address = null == uri ? null : uri.toString();
		synchronized(this) { 
			this.uri = address; 
		} 
	}
	
	public String getURI() {
		String address = uri;
		if (StringUtils.isBlank(address)) {
			synchronized(this) {
				address = uri;
				if (StringUtils.isBlank(address)) {
					uri = address = DEFAULT_URI;
				}
			}
		}
		return address;
	}
	
	private GeoLocationDAO dao;
	public void setDAO(GeoLocationDAO dao) {
		this.dao = dao;
	}
	
	public GeoLocationDAO getDAO() {
		return dao;
	}
	
	
	public IpInfoGeoLocationService() {}
	
	@Inject
	public IpInfoGeoLocationService(@ServiceURI String uri, GeoLocationDAO dao) {
		this.uri = uri;
		this.dao = dao;
	}
	
	/**
	 * @see com.svds.example.accesslog.GeoLocationService#find(java.lang.String)
	 */
	@Override
	public GeoLocation find(String ipAddress) throws GeoLocationException {
		if (StringUtils.isBlank(ipAddress));
		
		GeoLocation location = getDAO().find(ipAddress);
		if (null != location) { return location; }
		
		location = new GeoLocation();
		ClientConfig config = new ClientConfig();
	    Client client = ClientBuilder.newClient(config);
	    URI uri = UriBuilder.fromUri(getURI()).build();
	    WebTarget target = client.target(uri).path(ipAddress);
		
	    String response = target.request().header("User-Agent", "curl/7.31.0").accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN).get(String.class);
	    JsonObject json = new Gson().fromJson(response, JsonObject.class);
	    if (json.has("org")) {
	    	final String name = json.get("org").getAsString();
	    	if (StringUtils.isNotBlank(name)) {
	    		location.setOrganization(name);
	    	}
	    }
	    if (json.has("loc")) {
	    	final String point = json.get("loc").getAsString();
	    	if (StringUtils.isNotBlank(point)) {
	    		String[] tokens = StringUtils.split(point,",");
	    		if (StringUtils.isNotBlank(tokens[0])) { 
	    			BigDecimal longitude = new BigDecimal(tokens[0]);
	    			location.setLongitude(longitude);
	    		}
	    		if (StringUtils.isNotBlank(tokens[1])) { 
	    			BigDecimal latitude = new BigDecimal(tokens[0]);
	    			location.setLatitude(latitude);
	    		}
	    	}
	    }
	    dao.save(location);
	    return location;
	}
}
