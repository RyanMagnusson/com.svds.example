package com.svds.example.geolocation;

import java.lang.reflect.Type;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.svds.example.accesslog.GeoLocation;
import com.svds.example.geolocation.selenium.whatismyip.Ip2LocationResults;
import com.svds.example.geolocation.selenium.whatismyip.IpAddressLabsResults;

public class HybridGeoLocation {

	private String ipAddress;
	private GeoLocation maxmindData;
	private GeoLocation ipInfoData;
	private Ip2LocationResults ip2LocationData;
	private IpAddressLabsResults ipAddressLabsData;
	
	public GeoLocation getMaxmindData() {
		return maxmindData;
	}
	
	public void setIpAddress(String address) {
		this.ipAddress = address;
	}
	
	public String getIpAddress() { return ipAddress; }
	
	public void setMaxmindData(GeoLocation maxmindData) {
		this.maxmindData = maxmindData;
	}
	
	public GeoLocation getIpInfoData() {
		return ipInfoData;
	}
	
	public void setIpInfoData(GeoLocation ipInfoData) {
		this.ipInfoData = ipInfoData;
	}
	
	public Ip2LocationResults getIp2LocationData() {
		return ip2LocationData;
	}
	
	public void setIp2LocationData(Ip2LocationResults ip2LocationData) {
		this.ip2LocationData = ip2LocationData;
	}
	
	public IpAddressLabsResults getIpAddressLabsData() {
		return ipAddressLabsData;
	}
	
	public void setIpAddressLabsData(IpAddressLabsResults ipAddressLabsData) {
		this.ipAddressLabsData = ipAddressLabsData;
	}

	@Inject
	private volatile Gson gson;
	private Gson getGson() {
		Gson g = gson;
		if (null == g) {
			synchronized(this) {
				g = gson;
				if (null == g) {
					gson = g = new GsonBuilder()
										.disableHtmlEscaping()
										.serializeNulls()
										.registerTypeAdapter(GeoLocation.class, new GeoLocation.GsonAdapter())
										.registerTypeAdapter(Ip2LocationResults.class, new Ip2LocationResults.GsonAdapter())
										.registerTypeAdapter(IpAddressLabsResults.class, new IpAddressLabsResults.GsonAdapter())
										.registerTypeAdapter(HybridGeoLocation.class, new HybridGeoLocation.GsonAdapter())
										.create();
				}
			}
		}
		return g;
	}
	
	
	@Override
	public String toString() {
		return getGson().toJson(this);
	}
	
	public static class GsonAdapter implements JsonSerializer<HybridGeoLocation> {

//		@Inject
//		private JsonSerializer<GeoLocation> geoLocationSerializer;
//		
//		@Inject 
//		private JsonSerializer<Ip2LocationResults> ip2LocationSerializer;
//		
//		@Inject
//		private JsonSerializer<IpAddressLabsResults> ipAddressLabsSerializer;
		
		@Override
		public JsonElement serialize(HybridGeoLocation hybrid, Type type, JsonSerializationContext context) {
			if (null == hybrid) { return JsonNull.INSTANCE; }
			
			JsonObject json = new JsonObject();
			json.addProperty("ipAddress", hybrid.ipAddress);
			json.add("maxMind", context.serialize(hybrid.maxmindData, GeoLocation.class));
			json.add("ipInfo", context.serialize(hybrid.ipInfoData, GeoLocation.class));
			json.add("ip2Location", context.serialize(hybrid.ip2LocationData, Ip2LocationResults.class));
			json.add("ipAddressLabs", context.serialize(hybrid.ipAddressLabsData, IpAddressLabsResults.class));
			return json;
		}
	}
}
