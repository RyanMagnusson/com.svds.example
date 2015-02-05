package com.svds.example.accesslog;

import javax.inject.Inject;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.exception.DefaultExceptionContext;
import org.apache.commons.lang3.exception.ExceptionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.svds.example.accesslog.GeoLocation.GsonAdapter;

public final class GsonHelper {

	/*
	 * A special singleton instance of a Gson that will serialize 
	 * an AccessLogParsedRecord into a JSON format.
	 * <p>
	 * Implemented here as a static class so we still gain the advantage of
	 * having only a single instance created, but it is only done if necessary.
	 * </p>
	 */
	static final class Holder {
		
		static final Gson GSON = new GsonBuilder()
											.serializeNulls()
											.disableHtmlEscaping()
											.registerTypeAdapter(InputRecord.class, new InputRecord.GsonAdapter())
											.registerTypeAdapter(OutputRecord.class, new OutputRecord.GsonAdapter())
											.registerTypeAdapter(GeoLocation.class, new GeoLocation.GsonAdapter())
											.create(); 
	}

	GsonHelper() {}
	
	public static Gson getGson() { return Holder.GSON; }
}
