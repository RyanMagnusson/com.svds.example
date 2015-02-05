package com.svds.example.accesslog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class AccessLogReader implements LogReader {

	@Inject
	private Parser parser;
	private BufferedReader reader;
	public AccessLogReader() {}
	
	@Inject
	private AccessLogReader(@Named("inFile") Reader reader) {
		if (null != reader) {
			this.reader = (reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader));
		}
	}

	public void setParser(Parser parser) { this.parser = parser; }
	//public Parser getParser() { return parser; }
	
	public void setReader(Reader reader) {
		if (null == reader) {
			this.reader = null;
			return;
		}
		this.reader = (reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader));
	}
	//public Reader getReader() { return reader; }
	
	public static AccessLogReader using(Reader reader) {
		AccessLogReader logReader = new AccessLogReader();
		if (null == reader) {
			throw new IllegalArgumentException("A valid reader must be provided. Null references are not allowed.");
		}
		logReader.reader = (reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader));
		return logReader;
	}
	
	public static AccessLogReader reading(String path) throws FileNotFoundException {
		AccessLogReader logReader = new AccessLogReader();
		if (StringUtils.isBlank(path)) {
			throw new FileNotFoundException("Unable to locate a file because a null reference or empty string was provided as the path");
		}
		File f = new File(path.trim());
		if (f.canRead()) {
			logReader.reader = new BufferedReader(new FileReader(f));
			return logReader;
		}
		else {
			// try using the classpath
			ClassLoader loader = AccessLogReader.class.getClassLoader();
			URL url = loader.getResource(path);
			if (null != url) {
				try {
					logReader.reader = new BufferedReader(new InputStreamReader(url.openStream()));
					return logReader;
				}
				catch (IOException e) {
					throw new FileNotFoundException("IOException trying to open the file at URL: " + url.toString() + ". " + ExceptionUtils.getMessage(e));
				}
			}
			else {
				throw new FileNotFoundException("Unable to find the access_log file after searching if the argument provided was an explicit path to the file or a name of a file on the classpath: " + path);
			}
		}
	}
	
	@Override
	public void close() {
		if (null != reader) {
			try {
				reader.close();
			}
			catch (IOException e) {
				/* expected as a possibility but just ignore */
			}
		}
	}

	@Override
	public InputRecord readLine() throws IOException {
		if (null == reader) { return null; }
		
		final String line = reader.readLine();
		if (StringUtils.isBlank(line)) { return null; }
		
		return parser.parse(line);
	}
}
