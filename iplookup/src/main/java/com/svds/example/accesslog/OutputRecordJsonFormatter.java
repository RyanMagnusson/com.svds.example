package com.svds.example.accesslog;

import java.lang.reflect.Type;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author rmagnus
 */
public class OutputRecordJsonFormatter 
		extends OutputRecord.GsonAdapter
		implements Formatter<OutputRecord>, JsonSerializer<OutputRecord> {

	protected static final String LABEL_ORGANIZATION = "organization";
	protected static final String LABEL_LATITUDE = "latitude";
	protected static final String LABEL_LONGITUDE = "longitude";
	protected static final String LABEL_ISP_NAME = "ispName";
	
	public JsonElement serialize(OutputRecord record, Type type, JsonSerializationContext context) {
		if (null == record) { return JsonNull.INSTANCE; }
		
		JsonObject json = new JsonObject(); //super.serialize(record, type, context).getAsJsonObject();
		json.add(LABEL_WHEN_REQUEST_PROCESSED, serializeDateTime(record.getWhenRequestProcessed()));
		json.addProperty(LABEL_CLIENT_REQUEST_URI, record.getClientRequestedUri());
		json.addProperty(LABEL_HTTP_REFERER, record.getHttpReferer());
		json.addProperty(LABEL_IP_ADDRESS, record.getIpAddress());
		if (null == record.getGeoLocation()) {
			json.add(LABEL_ORGANIZATION, JsonNull.INSTANCE);
			json.add(LABEL_LATITUDE, JsonNull.INSTANCE);
			json.add(LABEL_LONGITUDE, JsonNull.INSTANCE);
			json.add(LABEL_ISP_NAME, JsonNull.INSTANCE);
		}
		else {
			json.addProperty(LABEL_ORGANIZATION, record.getGeoLocation().getOrganization());
			json.addProperty(LABEL_LATITUDE, record.getGeoLocation().getLatitude());
			json.addProperty(LABEL_LONGITUDE, record.getGeoLocation().getLongitude());
			json.addProperty(LABEL_ISP_NAME, record.getGeoLocation().getNameOfIsp());
		}
		return json;
	}
	
	private static final Gson GSON = new GsonBuilder()
											.serializeNulls()
											.disableHtmlEscaping()
											.registerTypeAdapter(OutputRecord.class, new OutputRecordJsonFormatter())
											.create();
	
	public String format(OutputRecord record) {
		return GSON.toJson(record);
	}

}
