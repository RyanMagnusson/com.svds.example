package com.svds.example.accesslog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author rmagnus
 */
public class InputRecordParser implements Parser {

	private Logger logger = LogManager.getLogger();
	
	
//	198.0.200.105 - - [14/Jan/2014:09:36:50 -0800] "GET /svds.com HTTP/1.1" 301 241 "http://www.svds.com/rockandroll/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36"
//	Remote host (ie the client IP)
//	Identity of the user determined by identd (not usually used since not reliable)
//	User name determined by HTTP authentication
//	Time the server finished processing the request.
//	Request line from the client. ("GET / HTTP/1.0")
//	Status code sent from the server to the client (200, 404 etc.)
//	Size of the response to the client (in bytes)
//	Referer is the page that linked to this URL.
//	User-agent is the browser identification string.
	
	static final String REGEX_TOKENS = "^(?<address>[0-9\\.]+)\\s+(?<user>.*?)\\s+\\[(?<when>.*?)\\]\\s+\"(?<request>.*?)\"\\s+(?<status>[\\d\\-]+)\\s+(?<length>[\\d\\-]+)\\s+\"(?<referer>.*?)\"\\s+\"(?<agent>.*?)\".*$";
	static final Pattern PATTERN_TOKENS = Pattern.compile(REGEX_TOKENS, Pattern.CASE_INSENSITIVE);
	
	static final String REGEX_CLIENT_REQUEST = "(?<method>GET|HEAD|DELETE|POST|PUT)\\s+?(?<uri>.*?)\\s+?(?<version>HTTP.*)$";
	static final Pattern PATTERN_CLIENT_REQUEST = Pattern.compile(REGEX_CLIENT_REQUEST, Pattern.CASE_INSENSITIVE);
	static final DateTimeFormatter dtFormatter = DateTimeFormat.forPattern("dd/MMM/yyyy:HH:mm:ss Z");
	
	@Override
	public InputRecord parse(String line) {
		if (StringUtils.isBlank(line)) { return null; }
		
		Matcher matcher = PATTERN_TOKENS.matcher(line);
		if (!matcher.find()) {
			logger.warn("found a mismatched line: " + line);
			return null;
		}
		
		InputRecord record = new InputRecord();
		record.setRemoteHost(matcher.group("address"));
		
		String timestamp = matcher.group("when");
		if (StringUtils.isNotBlank(timestamp)) {
			//14/Jan/2014:09:36:50 -0800
			DateTime dt = dtFormatter.parseDateTime(timestamp);
			record.setWhenRequestProcessed(dt);
		}
		
		String request = matcher.group("request");
		if (StringUtils.isNotBlank(request)) {
			
			Matcher reqMatch = PATTERN_CLIENT_REQUEST.matcher(request.trim());
			if (!reqMatch.find()) {
				logger.warn("The request token does not match the expected regex pattern: " + request);
			}
			else {
				logger.debug("The line is a match: {}", request);
				record.setHttpMethod(reqMatch.group("method"));
				record.setClientRequestUri(reqMatch.group("uri"));
				record.setHttpVersion(reqMatch.group("version"));
			}
		}
		
		String status = matcher.group("status");
		if (StringUtils.isNotBlank(status)) {
			if (!NumberUtils.isNumber(status)) {
				logger.warn("The parsed HTTP status is not numeric: " + status);
			}
			else {
				int value = Integer.parseInt(status);
				record.setHttpStatus(value);
			}
		}
		
		record.setResponseSize(parseInt(matcher,"length"));
		
//		String length = matcher.group("length");
//		if (StringUtils.isNotBlank(length)) {
//			if ("-".equals(length)) {
//				logger.debug("The parsed response length was recorded as a hyphen: " + line);
//			}
//			else if (!NumberUtils.isNumber(length)) {
//				logger.warn("The parsed response length is not numeric: " + length);
//			}
//			else {
//				int value = Integer.parseInt(length);
//				
//			}
//		}
		
		final String referer = matcher.group("referer");
		if (StringUtils.isNotBlank(referer)) {
			final String trimmed = referer.trim();
			if ("-".equalsIgnoreCase(trimmed)) {
				logger.debug("The parsed referer address was recorded as a hyphen. A null reference will be used instead.");
			}
			else {
				record.setHttpReferer(trimmed);
			}
		}
		
		//record.setUserAgent(matcher.group("agent"));
		record.setUserAgent(parseText(matcher,"agent"));
		return record;
	}
	
	String parseText(Matcher matcher, String key) {
		final String text = matcher.group(key);
		if (StringUtils.isBlank(text)) { return null; }
		
		final String trimmed = text.trim();
		if ("-".equalsIgnoreCase(trimmed)) {
			logger.debug("The parsed text value was recorded as a hyphen. A null reference will be used instead.");
			return null;
		}
		
		return trimmed;
	}
	
	Integer parseInt(Matcher matcher, String key) {
		final String text = matcher.group(key);
		if (StringUtils.isBlank(text)) { return null; }
		
		final String trimmed = text.trim();
		
		if ("-".equals(trimmed)) {
			logger.debug("The parsed value was recorded as a hyphen, returning a null reference.");
			return null;
		}
		
		if (!NumberUtils.isNumber(trimmed)) {
			logger.warn("The parsed value is not numeric: " + trimmed);
			return null;
		}
		
		return Integer.parseInt(trimmed);
	}
}
