package com.svds.example.accesslog;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.svds.example.accesslog.builders.InputRecordBuilder;
import com.svds.example.common.Copyable;
import com.svds.example.common.Equivalency;

/**
 * @author Ryan Magnusson
 */
public class InputRecord implements Serializable, Copyable<InputRecord>, Cloneable, Comparable<InputRecord> {

	private static final long serialVersionUID = -788150284958178978L;

	/**
	 * The remote host (i.e. the client IP)
	 */
	private String remoteHost;
	
	/**
	 * Identity of the user determined by identd (not usually used since not reliable)
	 */
	private String identdUserId;
	
	/**
	 * User name determined by HTTP authentication 
	 */
	private String httpAuthUserName;
	
	/**
	 * Time the server finished processing the request.
	 */
	private DateTime whenRequestProcessed;
	
	/**
	 * Request line from the client. ("GET / HTTP/1.0")
	 */
	private String httpMethod;
	private String clientRequestUri;
	private String httpVersion;
	
	/**
	 * Status code sent from the server to the client (200, 404 etc.)
	 */
	private Integer httpStatus;
	
	/**
	 * Size of the response to the client (in bytes) 
	 */
	private Integer responseSize;
	
	/**
	 * Referer is the page that linked to this URL.
	 */
	private String httpReferer;
	
	/**
	 * User-agent is the browser identification string
	 */
	private String userAgent;

	public String getRemoteHost() {
		return remoteHost;
	}


//	 * <p>
//	 * Since an empty string ("") or whitespace ("     ") really
//	 * refers to same IP address as a {@code null} reference does,
//	 * this first inspects the text provided and treats these the same as a {@code null}.
//	 * <br />
//	 * Likewise, an IP address of "127.0.0.1" is the same as " 127.0.0.1    " 
//	 * so the provided text is also trimmed before being assigned. 
//	 * </p>
	/**
	 * Sets the remote IP address of the client.
	 * @param remoteHost the IP address of the client
	 */
	public void setRemoteHost(String address) {
		this.remoteHost = address; //StringUtils.isBlank(remoteHost) ? null : remoteHost.trim();
	}

	public String getIdentdUserId() {
		return identdUserId;
	}

	public void setIdentdUserId(String identdUserId) {
		this.identdUserId = identdUserId;
	}

	public String getHttpAuthUserName() {
		return httpAuthUserName;
	}

	public void setHttpAuthUserName(String httpAuthUserName) {
		this.httpAuthUserName = httpAuthUserName;
	}

	public DateTime getWhenRequestProcessed() {
		return whenRequestProcessed;
	}

	public void setWhenRequestProcessed(DateTime timestamp) {
		this.whenRequestProcessed = timestamp;
	}

	public String getClientRequestUri() {
		return clientRequestUri;
	}

	public void setClientRequestUri(String request) {
		this.clientRequestUri = request;
	}

	public String getHttpMethod() {
		return httpMethod;
	}
	
	public void setHttpMethod(String method) {
		this.httpMethod = method;
	}
	
	public String getHttpVersion() {
		return httpVersion;
	}
	
	public void setHttpVersion(String version) {
		this.httpVersion = version;
	}
	
	public Integer getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(Integer status) {
		this.httpStatus = status;
	}

	public Integer getResponseSize() {
		return responseSize;
	}

	public void setResponseSize(Integer responseSize) {
		this.responseSize = responseSize;
	}

	public String getHttpReferer() {
		return httpReferer;
	}

	public void setHttpReferer(String httpReferer) {
		this.httpReferer = httpReferer;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	/**
	 * An instance of Gson's JsonSerializer interface that 
	 * will format an AccessLogParsedRecord into a standardized
	 * format.
	 * 
	 * @author rmagnus
	 */
	public static class GsonAdapter implements JsonSerializer<InputRecord> {

		protected static final String LABEL_REMOTE_HOST = "remoteHost";
		protected static final String LABEL_IDENTD_USERID = "identdUserId";
		protected static final String LABEL_HTTP_AUTH_USERNAME = "httpAuthUserName";
		protected static final String LABEL_WHEN_PROCESS_COMPLETED = "whenRequestProcessed";
		protected static final String LABEL_CLIENT_REQUEST_URI = "requestedUri";
		protected static final String LABEL_HTTP_METHOD = "httpMethod";
		protected static final String LABEL_HTTP_VERSION = "httpVersion";
		protected static final String LABEL_HTTP_STATUS = "httpStatus";
		protected static final String LABEL_RESPONSE_SIZE = "responseSize";
		protected static final String LABEL_HTTP_REFERER = "httpReferer";
		protected static final String LABEL_USER_AGENT = "userAgent";
		
		protected static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
		
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
		public JsonElement serialize(InputRecord record, Type type, JsonSerializationContext context) {
			if (null == record) { return JsonNull.INSTANCE; }
			
			JsonObject json = new JsonObject();
			json.addProperty(LABEL_REMOTE_HOST,record.getRemoteHost());
			json.addProperty(LABEL_IDENTD_USERID,record.getIdentdUserId());
			json.addProperty(LABEL_HTTP_AUTH_USERNAME,record.getHttpAuthUserName());
			
			json.add(LABEL_WHEN_PROCESS_COMPLETED, serializeDateTime(record.whenRequestProcessed));
			json.addProperty(LABEL_CLIENT_REQUEST_URI,record.getClientRequestUri());
			json.addProperty(LABEL_HTTP_METHOD,record.getHttpMethod());
			json.addProperty(LABEL_HTTP_VERSION, record.getHttpVersion());
			json.addProperty(LABEL_HTTP_STATUS,record.getHttpStatus());
			
			json.addProperty(LABEL_RESPONSE_SIZE,record.getResponseSize());
			json.addProperty(LABEL_HTTP_REFERER,record.getHttpReferer());
			json.addProperty(LABEL_USER_AGENT,record.getUserAgent());
			return json;
		}
	}

	@Override
	public String toString() {
		return GsonHelper.getGson().toJson(this);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11,13)
						.append(getRemoteHost())
						.append(getWhenRequestProcessed())
						.append(getClientRequestUri())
						.append(getHttpMethod())
						.append(getHttpStatus())
						.append(getHttpReferer())
						.append(getUserAgent())
						.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) { return false; }
		if (this == obj) { return true;  }
		
		if (!(obj instanceof InputRecord)) { return false; }
		
		InputRecord other = (InputRecord)obj;
		return Equivalency.ignoringCase().areNotEqual(getRemoteHost(), other.getRemoteHost())
					&& Equivalency.forDateTime().areNotEqual(getWhenRequestProcessed(), other.getWhenRequestProcessed())
					&& Equivalency.ignoringCase().areNotEqual(getClientRequestUri(), other.getClientRequestUri())
					&& Equivalency.ignoringCase().areNotEqual(getHttpMethod(), other.getHttpMethod())
					&& Equivalency.forInts().areNotEqual(getHttpStatus(), other.getHttpStatus())
					&& Equivalency.ignoringCase().areNotEqual(getHttpReferer(), other.getHttpReferer())
					&& Equivalency.ignoringCase().areNotEqual(getUserAgent(), other.getUserAgent())
					;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		InputRecord rec = (InputRecord)super.clone(); // per the Cloneable API, you should always invoke #clone()
		
		copyTo(rec); // but that doesn't mean we cannot override everything ;)
		return rec;
	}

	public InputRecord copy() {
		InputRecord rec = new InputRecord(); // let's create a brand new object...
		copyTo(rec);
		return rec;
	}

	@Override
	public void copyTo(InputRecord target) {
		if (null == target) { return; }
		
		target.clientRequestUri = getClientRequestUri();
		target.httpAuthUserName = getHttpAuthUserName();
		target.httpMethod = getHttpMethod();
		target.httpReferer = getHttpReferer();
		target.httpStatus = getHttpStatus();
		target.httpVersion = getHttpVersion();
		target.identdUserId = getIdentdUserId();
		target.remoteHost = getRemoteHost();
		target.responseSize = getResponseSize();
		target.userAgent = getUserAgent();
		target.whenRequestProcessed = getWhenRequestProcessed();
	}

	public static InputRecordBuilder fromAddress(String address) {
		return new InputRecordBuilder().fromAddress(address);
	}
	
	public static InputRecordBuilder whenProcessed(DateTime timestamp) {
		return new InputRecordBuilder().whenProcessed(timestamp);
	}
	
	public static InputRecordBuilder uriRequested(String uri) {
		return new InputRecordBuilder().fromAddress(uri);
	}
	
	public static InputRecordBuilder referedFrom(String uri) {
		return new InputRecordBuilder().referedFrom(uri);
	}
	
	static class IgnoringWhitespaceAndNullSafeOrdering implements Comparator<String> {

		@Override
		public int compare(String s1, String s2) {
			if (s1 == s2) { return 0; }
			
			if (StringUtils.isBlank(s1)) {
				return StringUtils.isBlank(s2) ? 0 : +1;
			}
			
			if (StringUtils.isBlank(s2)) { return -1; }
			
			return s1.trim().compareToIgnoreCase(s2.trim());
		}
	}
	
	/**
	 * Implements the default natural sort over an InputRecord
	 * <p>
	 * This method is used by several of the java.util.Collection classes,
	 * and has a relationship with the {@link #equals(Object)} and {@link #hashCode()}
	 * methods to define uniqueness.
	 * <br />
	 * The natural sort is defined in order of the following:
	 * <ol>
	 * <li>When the request was processed</li>
	 * <li>The URI of the page requested</li>
	 * <li>The IP address of the client</li>
	 * <li>The user-agent used</li>
	 * <li>The URI of the refering page</li>
	 * <li>The HTTP method</li>
	 * <li>The HTTP status</li>
	 * </ol>
	 * <br />
	 * 
	 * Any {@code null} references will be sorted last.
	 * Any text fields that are empty or contain only whitespace will be treated as {@code null} references.
	 * </p>
	 */
	@Override
	public int compareTo(InputRecord other) {
		if (null == other) { return -1; }
		if (this == other) { return  0; }
		
		return ComparisonChain.start()   
							  .compare(getWhenRequestProcessed(), other.getWhenRequestProcessed(), Ordering.natural().nullsLast())
							  .compare(getClientRequestUri(), other.getClientRequestUri(), new IgnoringWhitespaceAndNullSafeOrdering())
							  .compare(getRemoteHost(), other.getRemoteHost(), new IgnoringWhitespaceAndNullSafeOrdering())
							  .compare(getUserAgent(), other.getUserAgent(), new IgnoringWhitespaceAndNullSafeOrdering())
							  .compare(getHttpReferer(), other.getHttpReferer(), new IgnoringWhitespaceAndNullSafeOrdering())
							  .compare(getHttpMethod(), other.getHttpMethod(), new IgnoringWhitespaceAndNullSafeOrdering())
							  .compare(getHttpStatus(), other.getHttpStatus(), Ordering.natural().nullsLast())
							  .result();
	}
}
