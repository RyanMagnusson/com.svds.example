package com.svds.example.accesslog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.svds.example.accesslog.Main.CommandLineOptions.Argument;
import com.svds.example.geolocation.ipinfo.IpInfoGeoLocationService;

public class Main {

	private static final Logger logger = LogManager.getLogger();
	private LogReader reader;
	private LogWriter writer;
	private GeoLocationService geoLocator;
	
	private static final String DEFAULT_OUTPUT_FILENAME = "access_log.out";
	private static final String DEFAULT_INPUT_FILENAME = "access.log";
	
	
	public Main() {}
	
	static class GuiceModule extends AbstractModule {
		
		
		public GuiceModule() {}
		
		public GuiceModule(String inFile, String outFile) {
			this.outputFileName = outFile;
			this.inputFileName = inFile;
		}
		
		private Writer setupOutputWriter() {
			File fOut = new File(outputFileName);
			try {
				return new BufferedWriter(new FileWriter(fOut));
			}
			catch (IOException e) {
				throw new ContextedRuntimeException("An IOException was thrown while setting up the output file.\n" + ExceptionUtils.getMessage(e),e)
								.addContextValue("file", outputFileName);
			}
		}
		
		private Reader setupInputReader() {
			File f = new File(inputFileName);
			try {
				return new BufferedReader(new FileReader(f));
			}
			catch (IOException e) {
				throw new ContextedRuntimeException("An IOException was thrown while setting up the input file.\n" + ExceptionUtils.getMessage(e),e)
								.addContextValue("file", inputFileName);
			}
		}
		
		@Override
		protected void configure() {
			bind(Reader.class).annotatedWith(Names.named("inFile")).toInstance(setupInputReader());
			bind(Writer.class).annotatedWith(Names.named("outFile")).toInstance(setupOutputWriter());
			
			bind(new TypeLiteral<JsonSerializer<GeoLocation>>() {}).to(GeoLocation.GsonAdapter.class).in(Scopes.SINGLETON);
			bind(new TypeLiteral<JsonSerializer<OutputRecord>>() {}).to(OutputRecord.GsonAdapter.class).in(Scopes.SINGLETON);
			bind(new TypeLiteral<JsonSerializer<InputRecord>>() {}).to(InputRecord.GsonAdapter.class).in(Scopes.SINGLETON);
			//bind(new TypeLiteral<Formatter<OutputRecord>>() {}).to(OutputRecordJsonFormatter.class).in(Scopes.SINGLETON);
			bind(new TypeLiteral<Formatter<OutputRecord>>() {}).to(OutputRecordPipeDelimitedFormatter.class).in(Scopes.SINGLETON);
			bind(Parser.class).to(InputRecordParser.class);
			bind(LogReader.class).to(AccessLogReader.class);
			bind(LogWriter.class).to(AccessLogWriter.class);
			bind(GeoLocationService.class).to(IpInfoGeoLocationService.class);
			bind(Main.class).asEagerSingleton();
		}
	}
	
	static class CommandLineOptions {
		
		private static final Options OPTIONS = initOptions();
		
		public enum Argument {
			  DATABASE_FILE("database")
			, INPUT_FILE("input")
			, OUTPUT_FILE("output")
			, USE_MAXMIND("maxmind")
			, USE_IPINFO("ipinfo")
			;
			
			
			private String ref;
			private Option option;
			private Argument(String name) {
				this.ref = name;
			}
			
			public static final Argument fromString(String text) {
				if (StringUtils.isBlank(text)) { return null; }
				
				final String trimmed = text.trim();
				for (Argument arg : values()) {
					if (arg.ref.equalsIgnoreCase(trimmed)
							|| arg.ref.equalsIgnoreCase(arg.name())) {
						return arg;
					}
				}
				return null;
			}
		}
		
		private static final Options initOptions() {
			  Options opts = new Options();
			  opts.addOption(OptionBuilder.isRequired(false).hasArg().withDescription("The path or name of the file to use for storing GeoLocation data").create(Argument.DATABASE_FILE.ref));
			  opts.addOption(OptionBuilder.isRequired(false).hasArg().withDescription("The path or name of the file to read").create(Argument.INPUT_FILE.ref));
			  opts.addOption(OptionBuilder.isRequired(false).hasArg().withDescription("The path or name of the file to write to").create(Argument.OUTPUT_FILE.ref));
			  opts.addOption(OptionBuilder.isRequired(false).hasArg(false).withDescription("Tells iplookup to use the local MaxMind database. This option is mutually exclusive with the --use-ipinfo option and the program will abort if both are set").create(Argument.USE_MAXMIND.ref));
			  opts.addOption(OptionBuilder.isRequired(false).hasArg(false).withDescription("Tells iplookup to use the ipinfo REST service. This option is mutually exclusive with the --use-maxmind option and the program will abort if both are set").create(Argument.USE_IPINFO.ref));
			  return opts;
		}
		
		public static Map<Argument,String> parse(String[] args) throws ParseException {
			Map<Argument,String> map = new HashMap<Argument,String>();
			if (ArrayUtils.isEmpty(args)) {
				return map;
			}
			
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(OPTIONS, args);
			for (Option opt : cmd.getOptions()) {
				if (cmd.hasOption(opt.getOpt())) {
					Argument arg = Argument.fromString(opt.getOpt());
					if (!opt.hasArg()) {
						map.put(arg, "true");
					}
					else {
						map.put(arg, cmd.getOptionValue(opt.getOpt()));
					}
				}
			}
			return map;
		}
		
	}
	
	private enum GeoLocationServiceChoice {
		MAXMIND, IPINFO;
	}
	
	private String databaseFile;
	private String inputFile;
	private String outputFile;
	private GeoLocationServiceChoice serviceChoice;
	
	public boolean useMaxMind() {
		return serviceChoice == GeoLocationServiceChoice.MAXMIND;
	}
	
	public boolean useIpInfo() {
		switch(serviceChoice) {
			case MAXMIND: return false;
			default: return true;
		}
	}
	
	public void setServiceChoice(GeoLocationServiceChoice whichService) {
		this.serviceChoice = whichService;
	}
	
	public GeoLocationServiceChoice getServiceChoice() {
		return this.serviceChoice;
	}
	
	public Main bootstrap(String[] args) {
		Map<Argument,String> options = null;
		try {
			options = CommandLineOptions.parse(args);
		} catch (ParseException e) {
			throw new ContextedRuntimeException("Unable to parse the command-line options",e)
							.addContextValue("commandline", StringUtils.join(args,' '));
		}
		
		final String dbFileName = options.get(Argument.DATABASE_FILE);
		this.databaseFile = StringUtils.isBlank(dbFileName) ? null : dbFileName;
		
		final String inFileName = options.get(Argument.INPUT_FILE);
		this.inputFile = StringUtils.isBlank(inFileName) ? DEFAULT_INPUT_FILENAME : inFileName;
		File f = new File(inputFile);
		if (f.exists() && f.isDirectory()) {
			if (!this.inputFile.endsWith(File.pathSeparator)) {
				this.inputFile += File.pathSeparator;
			}
			this.inputFile += DEFAULT_INPUT_FILENAME;
		}
		
		final String outFileName = options.get(Argument.OUTPUT_FILE);
		this.outputFile = StringUtils.isBlank(outFileName) ? DEFAULT_OUTPUT_FILENAME : outFileName;
		f = new File(outputFile);
		if (f.exists() && f.isDirectory()) {
			if (!this.outputFile.endsWith(File.pathSeparator)) {
				this.outputFile += File.pathSeparator;
			}
			this.inputFile += DEFAULT_OUTPUT_FILENAME;
		}
		
		if (options.containsKey(Argument.USE_MAXMIND)) {
			if (options.containsKey(Argument.USE_IPINFO)) {
				throw new ContextedRuntimeException("The ipinfo service and MaxMind local database cannot be used at the same time. Please choose just one.");
			}
			this.serviceChoice = GeoLocationServiceChoice.MAXMIND;
		}
		else {
			this.serviceChoice = GeoLocationServiceChoice.IPINFO;
		}
		
		return this;
	}
	
	public Main shutdown() {
		
		if (null != writer) { try { writer.close(); } catch (Exception e) { /* eat it */ } }
		
		return this;
	}
	
	@Inject
	Main(LogReader reader, LogWriter writer, GeoLocationService service) {
		this.reader = reader;
		this.writer = writer;
		this.geoLocator = service;
	}
	
	public static void main(String[] args) throws Exception {
		Injector guice = Guice.createInjector(new GuiceModule("src/main/resources/access.log", "access_log.out"));
		 
		Main m = guice.getInstance(Main.class);
		m.bootstrap();
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
			GeoLocation location = geoLocator.find(in.getRemoteHost());
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
