package com.svds.example.accesslog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppConfig {

	private static final String DEFAULT_OUTPUT_FILENAME = "access_log.out";
	private static final String DEFAULT_INPUT_FILENAME = "access.log";
	
	
	public AppConfig() {}
	
	public enum OutputFormatChoice {
		JSON, PIPE_DELIMITED;
	}
	
	public enum GeoLocationServiceChoice {
		MAXMIND, IPINFO;
	}
	
	private String databaseFile;
	private String inputFile;
	private String outputFile;
	private GeoLocationServiceChoice serviceChoice;
	private OutputFormatChoice formatChoice;
	
	public String getDatabaseFile() {
		return databaseFile;
	}

	public void setDatabaseFile(String databaseFile) {
		this.databaseFile = databaseFile;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public OutputFormatChoice getFormatChoice() {
		return formatChoice;
	}

	public void setFormatChoice(OutputFormatChoice formatChoice) {
		this.formatChoice = formatChoice;
	}

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
	
	public static AppConfig fromArguments(String[] args) {
		Map<Argument,String> options = null;
		try {
			options = parse(args);
		} catch (ParseException e) {
			throw new ContextedRuntimeException("Unable to parse the command-line options",e)
							.addContextValue("commandline", StringUtils.join(args,' '));
		}
		
		AppConfig.Builder builder = new AppConfig.Builder();
		final String dbFileName = options.get(Argument.DATABASE_FILE);
		builder.usingDatabase(dbFileName);
		
		final String inFileName = options.get(Argument.INPUT_FILE);
		if (StringUtils.isBlank(inFileName)) {
			builder.readingFrom(DEFAULT_INPUT_FILENAME);
		}
		else {
			builder.readingFrom(inFileName);
		}
		
		final String outFileName = options.get(Argument.OUTPUT_FILE);
		if (StringUtils.isBlank(outFileName)) {
			builder.writingTo(DEFAULT_OUTPUT_FILENAME);
		}
		else {
			builder.writingTo(outFileName);
		}
		
		if (options.containsKey(Argument.USE_MAXMIND)) {
			if (options.containsKey(Argument.USE_IPINFO)) {
				throw new ContextedRuntimeException("The ipinfo service and MaxMind local database cannot be used at the same time. Please choose just one.");
			}
			builder.useMaxMind();
		}
		else {
			builder.useIpInfo(); // defaults to this
		}
		
		if (options.containsKey(Argument.FORMAT_PIPES)) {
			if (options.containsKey(Argument.FORMAT_JSON)) {
				throw new ContextedRuntimeException("The JSON format and pipe-delimited format were both specified as the format to write the file out as. Both cannot be used at the same time, so please just choose one.");
			}
			builder.formatAsPipeDelimited();
		}
		else {
			builder.formatAsJSON(); // defaults to this
		}
		
		return builder.create();
	}
	
	
	
	public static class Builder {
		private final AppConfig config;
		
		public Builder() {
			this.config = new AppConfig();
		}
		
		public Builder useMaxMind() {
			config.serviceChoice = GeoLocationServiceChoice.MAXMIND;
			return this;
		}
		
		public Builder useIpInfo() {
			config.serviceChoice = GeoLocationServiceChoice.IPINFO;
			return this;
		}
		
		public Builder usingDatabase(String path) {
			config.databaseFile = StringUtils.isBlank(path) ? null : path;
			return this;
		}
		
		public Builder readingFrom(String path) {
			String fPath = path;
			if (StringUtils.isBlank(fPath)) {
				config.inputFile = null;
			}
			else {
				File f = new File(fPath);
				if (f.exists() && f.isDirectory()) {
					if (!path.endsWith(File.pathSeparator)) {
						fPath += File.pathSeparator;
					}
					fPath += DEFAULT_INPUT_FILENAME;
				}
				config.inputFile = fPath;
			}
			return this;
		}
		
		public Builder writingTo(String path) {
			String fPath = path;
			if (StringUtils.isBlank(fPath)) {
				config.outputFile = null;
			}
			else {
				File f = new File(fPath);
				if (f.exists() && f.isDirectory()) {
					if (!path.endsWith(File.pathSeparator)) {
						fPath += File.pathSeparator;
					}
					fPath += DEFAULT_OUTPUT_FILENAME;
				}
				config.outputFile = fPath;
			}
			return this;
		}
		
		public Builder formatAsJSON() {
			config.formatChoice = OutputFormatChoice.JSON;
			return this;
		}
		
		public Builder formatAsPipeDelimited() {
			config.formatChoice = OutputFormatChoice.PIPE_DELIMITED;
			return this;
		}
		
		public AppConfig create() {
			return config.copy();
		}
	}
	
	public enum Argument {
		  DATABASE_FILE("database")
		, INPUT_FILE("input")
		, OUTPUT_FILE("output")
		, USE_MAXMIND("maxmind")
		, USE_IPINFO("ipinfo")
		, FORMAT_JSON("json")
		, FORMAT_PIPES("pipe")
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
	
	public AppConfig copy() {
		AppConfig config = new AppConfig();
		copyTo(config);
		return config;
	}
	
	public void copyTo(AppConfig target) {
		if (null == target) { return; }
		if (this == target) { return; }
		
		target.databaseFile = databaseFile;
		target.formatChoice = formatChoice;
		target.inputFile = inputFile;
		target.outputFile = outputFile;
		target.serviceChoice = serviceChoice;
	}
	
	private static final Options OPTIONS = initOptions();
	private static final Options initOptions() {
		  Options opts = new Options();
		  opts.addOption(OptionBuilder.isRequired(false).hasArg().withDescription("The path or name of the file to use for storing GeoLocation data").create(Argument.DATABASE_FILE.ref));
		  opts.addOption(OptionBuilder.isRequired(false).hasArg().withDescription("The path or name of the file to read").create(Argument.INPUT_FILE.ref));
		  opts.addOption(OptionBuilder.isRequired(false).hasArg().withDescription("The path or name of the file to write to").create(Argument.OUTPUT_FILE.ref));
		  opts.addOption(OptionBuilder.isRequired(false).hasArg(false).withDescription("Tells iplookup to use the local MaxMind database. This option is mutually exclusive with the --use-ipinfo option and the program will abort if both are set").create(Argument.USE_MAXMIND.ref));
		  opts.addOption(OptionBuilder.isRequired(false).hasArg(false).withDescription("Tells iplookup to use the ipinfo REST service. This option is mutually exclusive with the --use-maxmind option and the program will abort if both are set").create(Argument.USE_IPINFO.ref));
		  opts.addOption(OptionBuilder.isRequired(false).hasArg(false).withDescription("Tells iplookup to write the output file in JSON format. This option is mutually exclusive with the --pipe option and the program will abort if both are set").create(Argument.FORMAT_JSON.ref));
		  opts.addOption(OptionBuilder.isRequired(false).hasArg(false).withDescription("Tells iplookup to write the output file using pipe '|' delimited format. This option is mutually exclusive with the --json option and the program will abort if both are set").create(Argument.FORMAT_PIPES.ref));
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
