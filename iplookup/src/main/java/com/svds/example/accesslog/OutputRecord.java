package com.svds.example.accesslog;

import java.lang.reflect.Type;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OutputRecord {
	
	/**
	 * date & time request was processed by the web server as epoch (from the file)
	 */
	private DateTime whenRequestProcessed;
	
	/**
	 * uri user click on (from the file)
	 */
	private String clientRequestedURI;
	
	/**
	 * referer (from the file)
	 */
	private String httpReferer;
	
	/**
	 * ip address (from the file)
	 */
	private String ipAddress;
	
	/**
	 * organization (from the ip address)
	 * latitude (from the ip address)
	 * longitude (from the ip address)
	 * isp name (from the ip address)
	 */
	private GeoLocation geoLocation;
	
	public DateTime getWhenRequestProcessed() {
		return whenRequestProcessed;
	}

	public void setWhenRequestProcessed(DateTime whenRequestProcessed) {
		this.whenRequestProcessed = whenRequestProcessed;
	}

	public String getClientRequestedUri() {
		return clientRequestedURI;
	}

	public void setClientRequestedUri(String uri) {
		this.clientRequestedURI = uri;
	}

	public String getHttpReferer() {
		return httpReferer;
	}

	public void setHttpReferer(String httpReferer) {
		this.httpReferer = httpReferer;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

	 
	
	@Override
	public String toString() {
		return GsonHelper.getGson().toJson(this);
	}
	
	public static class GsonAdapter implements JsonSerializer<OutputRecord> {
		
		protected static final String LABEL_WHEN_REQUEST_PROCESSED = "whenRequestProcessed";
		protected static final String LABEL_CLIENT_REQUEST_URI = "clientRequestURI";
		protected static final String LABEL_HTTP_REFERER = "httpReferer";
		protected static final String LABEL_IP_ADDRESS = "ipAddress";
		protected static final String LABEL_GEOLOCATION = "geolocation";
		
		protected static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
		
		private volatile JsonSerializer<GeoLocation> geoLocationSerializer;
		
		/**
		 * default empty constructor like all well-behaved java beans
		 */
		public GsonAdapter() {} 
		
		@Inject
		GsonAdapter(JsonSerializer<GeoLocation> geoLocationSerializer) {
			this.geoLocationSerializer = geoLocationSerializer;
		}
		
		public void setGeoLocationSerializer(JsonSerializer<GeoLocation> serializer) {
			this.geoLocationSerializer = serializer;
		}
		
		/**
		 * Allows for us to inject a value using a DI framework like Spring or Guice
		 * or uses the default the default if this has not been set.
		 * 
		 * @return
		 */
		public JsonSerializer<GeoLocation> getGeoLocationSerializer() {
			JsonSerializer<GeoLocation> cerealizer = geoLocationSerializer;
			if (null == cerealizer) {
				synchronized(this) {
					cerealizer = geoLocationSerializer;
					if (null == cerealizer) {
						LogManager.getLogger().trace("A JsonSerializer was not assigned for a GeoLocation. Using the default: {}", GeoLocation.GsonAdapter.class.getName());
						geoLocationSerializer = cerealizer = new GeoLocation.GsonAdapter();
					}
				}
			}
			return cerealizer;
		}
		
		/**
		 * Properly serializes a joda-time DateTime into a JsonElement string.
		 * <p />
		 * Returns a JsonPrimitive of type String
		 * formatted using ISO-8601 format: {@code yyyy-MM-ddTHH:mm:ssZ}
		 * with the original timezone parsed from the log.
		 * 
		 * @param dt The DateTime to marshal into a JSON format, can be {@code null}.
		 */
		protected JsonElement serializeDateTime(DateTime dt) {
			if (null == dt) { return JsonNull.INSTANCE; }
			
			return new JsonPrimitive(dt.toDateTime(DateTimeZone.UTC).toString(ISO8601_PATTERN));
		}
		
		/**
		 * @see JsonSerializer#serialize(Object, Type, JsonSerializationContext)
		 */
		public JsonElement serialize(OutputRecord record, Type type, JsonSerializationContext context) {
			if (null == record) { return JsonNull.INSTANCE; }
			
			JsonObject json = new JsonObject();
			json.add(LABEL_WHEN_REQUEST_PROCESSED,serializeDateTime(record.whenRequestProcessed));
			json.addProperty(LABEL_CLIENT_REQUEST_URI,record.getClientRequestedUri());
			json.addProperty(LABEL_HTTP_REFERER,record.getHttpReferer());
			json.addProperty(LABEL_IP_ADDRESS,record.getIpAddress());
			json.add(LABEL_GEOLOCATION, getGeoLocationSerializer().serialize(record.getGeoLocation(), GeoLocation.class, context));
			return json;
		}
	}

}
