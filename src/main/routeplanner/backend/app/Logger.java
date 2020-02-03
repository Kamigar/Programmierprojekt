package routeplanner.backend.app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/*
 * Logger class
 */
public class Logger {
	
	/*
	 * Log levels
	 */
	public enum Level {
		
		INFO,
		WARNING,
		ERROR
	}
	
	// Default log level (not explicitly specified)
	public static Level defaultLogLevel = Level.WARNING;
	// Default log level, if output and log streams are the same
	public static Level defaultSameOutputLogLevel = Level.ERROR;
	
	
	public Logger() {
		this(defaultLogLevel);
	}
	
	// Initialize logger with the given log level
	public Logger(Level level) {
		this(level, new BufferedWriter(new OutputStreamWriter(System.out)));
	}
	
	// Initialize logger with the given writer
	public Logger(BufferedWriter writer) {
		this(defaultLogLevel, writer);
	}
	
	// Initialize logger with log level and writer
	public Logger(Level level, BufferedWriter writer) {

		_level = level;
		_writer = writer;
	}
	
	// Log message asynchronously (without flush)
	public void logLazy(Level level, String message) throws IOException {
		
		if (level.compareTo(_level) >= 0) {
			
			_writer.write(message);
			_writer.newLine();
		}
	}
	
	// Log message synchronously (with flush)
	public void log(Level level, String message) throws IOException {
		
		if (level.compareTo(_level) >= 0) {
			
			logLazy(level, message);
			flush();
		}
	}
	
	// Flush the writer
	public void flush() throws IOException {
		
		_writer.flush();
	}
	
	// Close the underlying stream
	public void close() throws IOException {
		
		_writer.close();
	}
	
	// Log message with level INFO
	public void info(String message) throws IOException {
		log(Level.INFO, message);
	}
	
	// Log message with level WARNING
	public void warning(String message) throws IOException {
		log(Level.WARNING, message);
	}
	
	// Log message with level ERROR
	public void error(String message) throws IOException {
		log(Level.ERROR, message);
	}
	

	// Log level (messages with lower level are not printed
	private Level _level;
	
	// Buffered writer to write the messages
	private BufferedWriter _writer;
}
