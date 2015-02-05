package com.svds.example.accesslog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccessLogWriter implements LogWriter {

	private Logger logger = LogManager.getLogger();
	
	@Inject
	private Formatter<OutputRecord> formatter;
	
	private BufferedWriter writer;
	public AccessLogWriter() {}

	@Inject
	private AccessLogWriter(@Named("outFile") Writer writer) {
		if (null != writer) {
			this.writer = (writer instanceof BufferedWriter ? (BufferedWriter)writer : new BufferedWriter(writer));
		}
	}
	
	public void setWriter(Writer writer) {
		if (null == writer) {
			this.writer = null;
			return;
		}
		this.writer = (writer instanceof BufferedWriter ? (BufferedWriter)writer : new BufferedWriter(writer));
	}
	
	public void setFormatter(Formatter<OutputRecord> formatter) {
		this.formatter = formatter;
	}
	
	public static AccessLogWriter using(Writer writer) {
		AccessLogWriter logWriter = new AccessLogWriter();
		if (null != writer) {
			logWriter.writer = (writer instanceof BufferedWriter ? (BufferedWriter)writer : new BufferedWriter(writer));
		}
		return logWriter;
	}
	
	public static AccessLogWriter writing(String path) throws IOException {
		AccessLogWriter logWriter = new AccessLogWriter();
		if (StringUtils.isBlank(path)) {
			throw new FileNotFoundException("Unable to locate a file because a null reference or empty string was provided as the path");
		}
		File f = new File(path.trim());
		if (f.canWrite()) {
			logWriter.writer = new BufferedWriter(new FileWriter(f,true));
			return logWriter;
		}
		else {
			logWriter.writer = new BufferedWriter(new FileWriter(path.trim()));
		}
		return logWriter;
	}
	
	@Override
	public void flush() {
		if (null != writer) {
			try {
				writer.flush();
			}
			catch (IOException e) {
				/* expected as a possibility but just ignore */
			}
		}
	}
	
	@Override
	public void close() {
		if (null != writer) {
			flush();
			try {
				writer.close();
			}
			catch (IOException e) {
				/* expected as a possibility but just ignore */
			}
		}
	}

	@Override
	public boolean write(OutputRecord record) {
		if (null == record) { return false; }
		
		try {
			writer.write(formatter.format(record));
			writer.write('\n');
		} 
		catch (IOException e) {
			logger.catching(Level.ERROR, e);
			logger.error("IOException thrown while trying to write out the OutputRecord: " + record);
			return false;
		}
		return true;
	}
}
