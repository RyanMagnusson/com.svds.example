package com.svds.example.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.svds.example.accesslog.AppConfig.Argument;

public class Test_When_Parsing_Different_Options_From_The_CommandLine {

	private static final String PATH_TO_DATABASE = "/Users/junit/data/humpback_whale.watching";
	private static final String PATH_TO_INPUT_FILE = "~/access.log";
	private static final String PATH_TO_OUTPUT_FILE = "./access_log.log";
	
	private static final String[] COMMAND_LINE = {"--output",PATH_TO_OUTPUT_FILE,"--input",PATH_TO_INPUT_FILE, "--json"};
	private Map<Argument, String> parsedResults;
	
	@Before
	public void setUp() throws Exception {
		parsedResults = AppConfig.parse(COMMAND_LINE);
	}

	@Test
	public void Then_It_Should_Return_The_Correct_Path_For_The_Database_File() {
		assertFalse(parsedResults.containsKey(Argument.DATABASE_FILE));
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
		assertFalse(parsedResults.containsKey(Argument.USE_MAXMIND));
	}

	@Test
	public void Then_It_Should_Not_Return_To_Use_The_PipeDelimited_Format() {
		assertFalse(parsedResults.containsKey(Argument.FORMAT_PIPES));
	}
	
	@Test
	public void Then_It_Should_Return_To_Use_The_JSON_Format() {
		assertTrue(parsedResults.containsKey(Argument.FORMAT_JSON));
		assertEquals("true", parsedResults.get(Argument.FORMAT_JSON));
	}
}
