package com.svds.example.accesslog;

import org.apache.commons.lang3.exception.ContextedException;

/**
 * @author rmagnus
 */
public class GeoLocationException extends ContextedException {

	private static final long serialVersionUID = 3003176876626565356L;

	/**
	 * A default empty constructor, just like all good java beans should have. 
	 */
	public GeoLocationException() {}

	/**
	 * @param message
	 */
	public GeoLocationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public GeoLocationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public GeoLocationException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public GeoLocationException addContextValue(String label, Object value) {
		super.addContextValue(label, value);
		return this;
	}

	@Override
	public GeoLocationException setContextValue(String label, Object value) {
		super.setContextValue(label, value);
		return this;
	}

	

}
