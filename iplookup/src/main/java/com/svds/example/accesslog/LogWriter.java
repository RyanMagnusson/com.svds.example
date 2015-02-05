package com.svds.example.accesslog;

import java.io.Closeable;
import java.io.Flushable;

/**
 * @author rmagnus
 */
public interface LogWriter extends Flushable, Closeable {

	/**
	 * Writes out an OutputRecord 
	 * <p>
	 * It is up to the implementing class to determine how it will handle {@code null} references.
	 * <br />
	 * NOTE: this method only returns true to make it easier for testing. 
	 * </p>
	 * 
	 * @param record the record to write, can be {@code null}.
	 * @return {@code true} if the write was successful, {@code false} if it was not.
	 */
	boolean write(OutputRecord record);
	
}
