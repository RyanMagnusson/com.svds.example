package com.svds.example.accesslog;

public interface Parser {

	/**
	 * Parses a string of text into an InputRecord
	 * 
	 * @param line the text to parse, can be {@code null}
	 * @return an InputRecord if the row can be parsed, null if it is just empty whitespace or {@code null}.
	 */
	InputRecord parse(String line);
	
}
