package com.svds.example.accesslog;

import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTimeZone;

public class OutputRecordPipeDelimitedFormatter implements Formatter<OutputRecord> {

	private static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final char DELIMITER = '|';
	
	@SuppressWarnings("deprecation") // need to update to using Java 8
	public String format(OutputRecord record) {
		if (null == record) { return "null"; }
		
		String whenProcessed = "";
		String uri = "";
		String httpReferer = "";
		String ipAddress = "";
		String organization = "";
		String latitude = "";
		String longitude = "";
		String nameOfIsp = "";
		
		if (null != record.getWhenRequestProcessed()) {
			whenProcessed = record.getWhenRequestProcessed().withZone(DateTimeZone.UTC).toString(ISO8601_PATTERN);
		}
		uri = ObjectUtils.toString(record.getClientRequestedUri(), "");
		httpReferer = ObjectUtils.toString(record.getHttpReferer(), "");
		ipAddress = ObjectUtils.toString(record.getIpAddress(), "");
		if (null != record.getGeoLocation()) {
			organization = ObjectUtils.toString(record.getGeoLocation().getOrganization(), "");
			latitude = ObjectUtils.toString(record.getGeoLocation().getLatitude(), "");
			longitude = ObjectUtils.toString(record.getGeoLocation().getLongitude(), "");
			nameOfIsp = ObjectUtils.toString(record.getGeoLocation().getNameOfIsp(), "");
		}
		
		int length = 10 // for the pipes + a couple extra for breathing room :p 
				   + whenProcessed.length()
				   + uri.length()
				   + httpReferer.length()
				   + ipAddress.length()
				   + organization.length()
				   + latitude.length()
				   + longitude.length()
				   + nameOfIsp.length()
				   ;
		return new StringBuilder(length).append(whenProcessed)
				                        .append(DELIMITER)
				                        .append(uri)
				                        .append(DELIMITER)
				                        .append(httpReferer)
				                        .append(DELIMITER)
				                        .append(ipAddress)
				                        .append(DELIMITER)
				                        .append(organization)
				                        .append(DELIMITER)
				                        .append(latitude)
				                        .append(DELIMITER)
				                        .append(longitude)
				                        .append(DELIMITER)
				                        .append(nameOfIsp)
				                        .append(DELIMITER)
				                        .toString();
	}

}
