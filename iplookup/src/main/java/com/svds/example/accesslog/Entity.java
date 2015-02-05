package com.svds.example.accesslog;

import java.io.Serializable;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;

/**
 * Base class for all entity types
 * 
 * @author rmagnus
 */
public abstract class Entity<T> implements Serializable {

	private static final long serialVersionUID = 6654387444925864648L;

	@Inject
	private volatile Gson gson;
	protected void setGson(Gson gson) { synchronized(this) { this.gson = gson; } }
	
	protected Gson getGson() {
		Gson g = gson;
		if (null == g) {
			synchronized(this) {
				g = gson;
				if (null == g) {
					gson = g = new GsonBuilder()
										.disableHtmlEscaping()
										.serializeNulls()
										.registerTypeAdapter(getClass(), getDefaultJsonSerializer())
										.create();
				}
			}
		}
		return g;
	}

	protected abstract JsonSerializer<T> getDefaultJsonSerializer();
	
}
