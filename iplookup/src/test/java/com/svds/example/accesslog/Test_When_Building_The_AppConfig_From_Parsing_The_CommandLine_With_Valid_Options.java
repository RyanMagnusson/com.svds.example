package com.svds.example.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.svds.example.accesslog.AppConfig.Argument;

public class Test_When_Building_The_AppConfig_From_Parsing_The_CommandLine_With_Valid_Options {

	private static final String PATH_TO_DATABASE = "/Users/junit/data/geolocations.sqlite";
	private static final String PATH_TO_INPUT_FILE = "/tmp/access.log";
	private static final String PATH_TO_OUTPUT_FILE = "access_log.out";
	
	private static final String[] COMMAND_LINE = {"--output",PATH_TO_OUTPUT_FILE,"--database",PATH_TO_DATABASE,"--input",PATH_TO_INPUT_FILE, "--maxmind", "--pipe"};
	private AppConfig config;
	
	@Before
	public void setUp() throws Exception {
		config = AppConfig.fromArguments(COMMAND_LINE);
	}

	@Test
	public void Then_It_Should_Return_The_Correct_Path_For_The_Database_File() {
		assertNotNull(config.getDatabaseFile());
		assertEquals(PATH_TO_DATABASE, config.getDatabaseFile());
	}
	
	@Test
	public void Then_It_Should_Return_The_Correct_Path_For_The_Input_File() {
		assertNotNull(config.getInputFile());
		assertEquals(PATH_TO_INPUT_FILE, config.getInputFile());
	}
	
	@Test
	public void Then_It_Should_Return_The_Correct_Path_For_The_Output_File() {
		assertNotNull(config.getOutputFile());
		assertEquals(PATH_TO_OUTPUT_FILE, config.getOutputFile());
	}
	
	@Test
	public void Then_It_Should_Return_To_Use_The_MaxMind_Local_Database() {
		assertTrue(config.useMaxMind());
	}

	@Test
	public void Then_It_Should_Return_To_Use_The_PipeDelimited_Format() {
		assertTrue(config.getFormatChoice() == AppConfig.OutputFormatChoice.PIPE_DELIMITED);
	}
	
}
