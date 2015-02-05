package com.svds.example.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.svds.example.accesslog.AppConfig.Argument;

public class Test_When_Parsing_The_CommandLine_With_Valid_Options {

	private static final String PATH_TO_DATABASE = "/Users/junit/data/geolocations.sqlite";
	private static final String PATH_TO_INPUT_FILE = "/tmp/access.log";
	private static final String PATH_TO_OUTPUT_FILE = "access_log.out";
	
	private static final String[] COMMAND_LINE = {"--output",PATH_TO_OUTPUT_FILE,"--database",PATH_TO_DATABASE,"--input",PATH_TO_INPUT_FILE, "--maxmind", "--pipe"};
	private Map<Argument, String> parsedResults;
	
	@Before
	public void setUp() throws Exception {
		parsedResults = AppConfig.parse(COMMAND_LINE);
	}

	@Test
	public void Then_It_Should_Return_The_Correct_Path_For_The_Database_File() {
		assertTrue(parsedResults.containsKey(Argument.DATABASE_FILE));
		assertEquals(PATH_TO_DATABASE, parsedResults.get(Argument.DATABASE_FILE));
	}
	
	@Test
	public void Then_It_Should_Return_The_Correct_Path_For_The_Input_File() {
		assertTrue(parsedResults.containsKey(Argument.INPUT_FILE));
		assertEquals(PATH_TO_INPUT_FILE, parsedResults.get(Argument.INPUT_FILE));
	}
	
	@Test
	public void Then_It_Should_Return_The_Correct_Path_For_The_Output_File() {
		assertTrue(parsedResults.containsKey(Argument.OUTPUT_FILE));
		assertEquals(PATH_TO_OUTPUT_FILE, parsedResults.get(Argument.OUTPUT_FILE));
	}
	
	@Test
	public void Then_It_Should_Return_To_Use_The_MaxMind_Local_Database() {
		assertTrue(parsedResults.containsKey(Argument.USE_MAXMIND));
		assertEquals("true", parsedResults.get(Argument.USE_MAXMIND));
	}

	@Test
	public void Then_It_Should_Return_To_Use_The_PipeDelimited_Format() {
		assertTrue(parsedResults.containsKey(Argument.USE_MAXMIND));
		assertEquals("true", parsedResults.get(Argument.USE_MAXMIND));
	}
	
}
