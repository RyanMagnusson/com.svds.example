package com.svds.example.accesslog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.inject.Inject;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonSerializer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.svds.example.geolocation.GeoLocationDAO;
import com.svds.example.geolocation.ipinfo.IpInfoGeoLocationService;
import com.svds.example.geolocation.maxmind.MaxMindGeoLocator;
import com.svds.example.geolocation.sqlite.SqliteGeoLocationDAO;

public class Main {

	private static final Logger logger = LogManager.getLogger();
	private static final String DEFAULT_OUTPUT_FILENAME = "access_log.out";
	private static final String DEFAULT_INPUT_FILENAME = "access.log";
	
	private LogWriter writer;
	private LogReader reader;
	private GeoLocationService geoLocationService;
	
	public Main() {}
	
	static class GuiceModule extends AbstractModule {
		private AppConfig config;
		
		public GuiceModule() {}
		
		public GuiceModule(AppConfig config) {
			this.config = config;
		}
		
		private Writer setupOutputWriter() {
			 
			File fOut = new File(config.getOutputFile());
			try {
				return new BufferedWriter(new FileWriter(fOut));
			}
			catch (IOException e) {
				throw new ContextedRuntimeException("An IOException was thrown while setting up the output file.\n" + ExceptionUtils.getMessage(e),e)
								.addContextValue("file", config.getOutputFile());
			}
		}
		
		private Reader setupInputReader() {
			File f = new File(config.getInputFile());
			try {
				return new BufferedReader(new FileReader(f));
			}
			catch (IOException e) {
				throw new ContextedRuntimeException("An IOException was thrown while setting up the input file.\n" + ExceptionUtils.getMessage(e),e)
								.addContextValue("file", config.getInputFile());
			}
		}
		
		@Override
		protected void configure() {
			bind(Reader.class).annotatedWith(Names.named("inFile")).toInstance(setupInputReader());
			bind(Writer.class).annotatedWith(Names.named("outFile")).toInstance(setupOutputWriter());
			
			bind(new TypeLiteral<JsonSerializer<GeoLocation>>() {}).to(GeoLocation.GsonAdapter.class).in(Scopes.SINGLETON);
			bind(new TypeLiteral<JsonSerializer<OutputRecord>>() {}).to(OutputRecord.GsonAdapter.class).in(Scopes.SINGLETON);
			bind(new TypeLiteral<JsonSerializer<InputRecord>>() {}).to(InputRecord.GsonAdapter.class).in(Scopes.SINGLETON);
			
			if (AppConfig.OutputFormatChoice.PIPE_DELIMITED == config.getFormatChoice()) {
				bind(new TypeLiteral<Formatter<OutputRecord>>() {}).to(OutputRecordPipeDelimitedFormatter.class).in(Scopes.SINGLETON);
			}
			else {
				bind(new TypeLiteral<Formatter<OutputRecord>>() {}).to(OutputRecordJsonFormatter.class).in(Scopes.SINGLETON);
			}
			
			bind(Parser.class).to(InputRecordParser.class);
			bind(LogReader.class).to(AccessLogReader.class);
			bind(LogWriter.class).to(AccessLogWriter.class);
			
			if (config.useMaxMind()) {
				bind(GeoLocationService.class).to(MaxMindGeoLocator.class);	
			}
			else {
				bind(GeoLocationDAO.class).to(SqliteGeoLocationDAO.class).in(Scopes.SINGLETON);
				bind(GeoLocationService.class).to(IpInfoGeoLocationService.class);
			}
			
			bind(Main.class).asEagerSingleton();
		}
	}
	
	
	
	
	
	public Main shutdown() {
		if (null != writer) { try { writer.close(); } catch (Exception e) { /* eat it */ } }
		return this;
	}
	
	@Inject
	Main(LogReader reader, LogWriter writer, GeoLocationService service) {
		this.reader = reader;
		this.writer = writer;
		this.geoLocationService = service;
	}
	
	public static void main(String[] args) throws Exception {
		AppConfig config = AppConfig.fromArguments(args);
		Injector guice = Guice.createInjector(new GuiceModule(config));
		 
		Main m = guice.getInstance(Main.class);
		try {
			m.execute();
		}
		catch (Exception e) {
			logger.catching(e);
			throw e;
		}
		finally {
			m.shutdown();
		}
	}
	
	public OutputRecord createFrom(InputRecord in) {
		if (null == in) { return null; }
		
		OutputRecord out = new OutputRecord();
		
		out.setClientRequestedUri(in.getClientRequestUri());
		out.setIpAddress(in.getRemoteHost());
		out.setHttpReferer(in.getHttpReferer());
		out.setWhenRequestProcessed(in.getWhenRequestProcessed());
		try {
			GeoLocation location = geoLocationService.find(in.getRemoteHost());
			out.setGeoLocation(location);
			if (null == location) {
				logger.info("No GeoLocation data was returned for IP address: " + in.getRemoteHost());
			}
		}
		catch (GeoLocationException e) {
			logger.error("Unable to retrieve the GeoLocation for IP address: " + in.getRemoteHost() + "\n" + ExceptionUtils.getMessage(e),e);
		}
		return out;
	}
	
	public long execute() throws IOException {
		long count = 0;
		
		InputRecord inputRec = null;
		while (null != (inputRec = reader.readLine())) {
			count++;
			writer.write(createFrom(inputRec));
		}
		return count;
	}
}
