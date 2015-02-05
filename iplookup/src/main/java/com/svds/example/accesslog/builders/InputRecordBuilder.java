package com.svds.example.accesslog.builders;

import java.net.InetAddress;
import java.net.URI;
import java.text.MessageFormat;

import org.joda.time.DateTime;

import com.svds.example.accesslog.InputRecord;

public class InputRecordBuilder {
	private InputRecord record;
	
	public InputRecordBuilder() {
		this.record = new InputRecord();
	}
	
	/**
	 * In case it is necessary, dereferences the current {@code InputRecord}
	 * to a new fresh one.
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder reset() { record = new InputRecord(); return this ;}
	
	/**
	 * Sets the remote IP address 
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder fromAddress(String address) { this.record.setRemoteHost(address); return this ;}
	
	/**
	 * Sets the remote IP address 
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder fromAddress(InetAddress address) { 
		if (null == address) {
			this.record.setRemoteHost(null);
		}
		else {
			this.record.setRemoteHost(address.getHostAddress());
		}
		return this;
	}

	/**
	 * Sets the HTTP version recorded in the request 
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder httpVersion(String version) { 
		this.record.setHttpVersion(version);
		return this;
	}
	
	/**
	 * Sets the HTTP version recorded in the request 
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder httpVersion(double version) { 
		this.record.setHttpVersion(MessageFormat.format("HTTP/{0,number,0.0}",version));
		return this;
	}
	
	/**
	 * Sets the HTTP method recorded in the request 
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder httpMethodUsed(String method) { 
		this.record.setHttpMethod(method);
		return this;
	}
	
	/**
	 * Sets the HTTP status returned to the caller
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder httpStatusReturned(Integer status) { 
		this.record.setHttpStatus(status);
		return this;
	}
	
	/**
	 * Sets the length in bytes of the response returned to the caller
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder sizeOfTheResponse(Integer length) { 
		this.record.setResponseSize(length);
		return this;
	}
	
	/**
	 * Sets the date & time the request was processed by the server
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder whenProcessed(DateTime timestamp) { 
		this.record.setWhenRequestProcessed(timestamp);
		return this;
	}
	
	/**
	 * Sets the URI that the client requested
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder uriRequested(String uri) { 
		this.record.setClientRequestUri(uri);
		return this;
	}
	
	/**
	 * Sets the URI that the client requested
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder uriRequested(URI uri) { 
		return uriRequested(null == uri ? null : uri.toString());
	}
	
	/**
	 * Sets the URI of the web page that the client came from
	 * <p>
	 * This method was intentionally spelled using the traditional spelling 
	 * used in the CGI variables.
	 * </p>
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder referedFrom(URI uri) { // yes it is spelled wrong, just using the same spelling as used with CGI 
		return referedFrom(null == uri ? null : uri.toString());
	}
	
	/**
	 * Sets the URI of the web page that the client came from
	 * <p>
	 * This method was intentionally spelled using the traditional spelling 
	 * used in the CGI variables.
	 * </p>
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder referedFrom(String uri) { 
		String text = null == uri ? null : uri.toString();
		this.record.setHttpReferer(text);
		return this;
	}
	
	/**
	 * Sets the HTTP user-agent 
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder browserUsed(String agent) { 
		return withUserAgent(agent);
	}
	
	/**
	 * Sets the HTTP user-agent 
	 * <p>
	 * 
	 * </p>
	 * 
	 * @return the current InputRecordBuilder
	 */
	public InputRecordBuilder withUserAgent(String agent) { 
		this.record.setUserAgent(agent);
		return this;
	}
	
	public InputRecord create() {
		return this.record.copy();
	}
	
}
