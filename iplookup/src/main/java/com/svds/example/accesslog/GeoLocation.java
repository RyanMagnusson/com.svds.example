package com.svds.example.accesslog;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.svds.example.common.Copyable;


public class GeoLocation 
				implements Serializable, Comparable<GeoLocation>, Copyable<GeoLocation> {

	private static final long serialVersionUID = 6910177120392395601L;

	/**
	 * The IP Address used for geolocation
	 */
	private String ipAddress;
	/**
	 * Name of the ISP
	 */
	private String ispName;
	
	private String organization;
	
	private static final int DECIMAL_PRECISION = 7; 
	public BigDecimal longitude;
	public BigDecimal latitude;
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	/**
	 * The IP Address that is used to determine geo-location.
	 * @param address
	 */
	public void setIpAddress(String address) {
		this.ipAddress = StringUtils.isBlank(address) ? null : address.trim();
	}
	
	public String getNameOfIsp() {
		return ispName;
	}
	
	public void setNameOfIsp(String ispName) {
		this.ispName = ispName;
	}
	
	public String getOrganization() {
		return organization;
	}
	
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	public BigDecimal getLongitude() {
		return longitude;
	}
	
	public void setLongitude(Double longitude) {
		if (null == latitude) { this.latitude = null; return; }
		this.longitude = new BigDecimal(longitude, MathContext.DECIMAL64).setScale(DECIMAL_PRECISION, RoundingMode.HALF_UP);
	}
	
	public void setLongitude(BigDecimal longitude) {
		this.longitude = null == longitude ? null : longitude.setScale(DECIMAL_PRECISION, RoundingMode.HALF_UP);
	}
	
	public BigDecimal getLatitude() {
		return latitude;
	}
	
	public void setLatitude(Double latitude) {
		if (null == latitude) { this.latitude = null; return; }
		this.latitude = new BigDecimal(latitude, MathContext.DECIMAL64).setScale(DECIMAL_PRECISION, RoundingMode.HALF_UP);
	}
	
	public void setLatitude(BigDecimal latitude) {
		this.latitude = null == latitude ? null : latitude.setScale(DECIMAL_PRECISION, RoundingMode.HALF_UP);
	}

	/**
	 * Since this is used for coll
	 */
	@Override
	public int hashCode() {
		return null == ipAddress ? 0 : ipAddress.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true;  }
		if (obj == null) { return false; }
		
		if (getClass() != obj.getClass()) { return false; }

		GeoLocation other = (GeoLocation) obj;
		if (StringUtils.isBlank(getIpAddress())) {
			return StringUtils.isBlank(other.getIpAddress());
		}
		
		if (StringUtils.isBlank(other.getIpAddress())) { return false; }
		
		// because the internet really isn't case sensitive... ;)
		return getIpAddress().equalsIgnoreCase(other.getIpAddress());
	}

	public int compareTo(GeoLocation other) {
		if (null == other) { return -1; } // nulls go last
		if (this == other) { return  0; }
		
		if (getIpAddress() == other.getIpAddress()) { return 0; }
		
		if (StringUtils.isBlank(getIpAddress())) {
			return StringUtils.isBlank(other.getIpAddress()) ? 0 : +1; // nulls go last...
		}
		
		if (StringUtils.isBlank(other.getIpAddress())) { return 0; }
		
		return ComparisonChain.start()
							  .compare(getIpAddress().toLowerCase(), other.getIpAddress().toLowerCase(), Ordering.natural().nullsLast())
							  .compare(getLongitude(), other.getLongitude(), Ordering.natural().nullsLast())
							  .compare(getLatitude(), other.getLatitude(), Ordering.natural().nullsLast())
							  .result();
	}
	
	public static class GsonAdapter implements JsonSerializer<GeoLocation> {

		protected static final String LABEL_IP_ADDRESS = "ipAddress";
		protected static final String LABEL_ORGANIZATION = "organization";
		protected static final String LABEL_LATITUDE = "latitude";
		protected static final String LABEL_LONGITUDE = "longitude";
		protected static final String LABEL_ISP_NAME = "ispName";
		
		public JsonElement serialize(GeoLocation location, Type type, JsonSerializationContext context) {
			if (null == location) { return JsonNull.INSTANCE; }
			
			JsonObject json = new JsonObject();
			json.addProperty(LABEL_IP_ADDRESS, location.getIpAddress());
			json.addProperty(LABEL_ORGANIZATION, location.getOrganization());
			json.addProperty(LABEL_LATITUDE, location.getLatitude());
			json.addProperty(LABEL_LONGITUDE, location.getLongitude());
			json.addProperty(LABEL_ISP_NAME, location.getNameOfIsp());
			
			return json;
		}
		
	}
	
	@Override
	public String toString() {
		return GsonHelper.getGson().toJson(this);
	}

	@Override
	public GeoLocation copy() {
		GeoLocation location = new GeoLocation();
		copyTo(location);
		return location;
	}

	@Override
	public void copyTo(GeoLocation target) {
		if (null == target) { return; }
		if (this == target) { return; }
		
		target.ipAddress = getIpAddress();
		target.ispName = getNameOfIsp();
		target.latitude = getLatitude();
		target.longitude = getLongitude();
		target.organization = getOrganization();
	}
}
