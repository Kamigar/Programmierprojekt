package routeplanner.backend.app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Logger {
	
	public enum Level {
		
		INFO,
		WARNING,
		ERROR
	}
	
	public static Level defaultLogLevel = Level.WARNING;
	public static Level defaultSameOutputLogLevel = Level.ERROR;
	
	
	public Logger() {
		this(defaultLogLevel);
	}
	
	public Logger(Level level) {
		this(level, new BufferedWriter(new OutputStreamWriter(System.out)));
	}
	
	public Logger(BufferedWriter writer) {
		this(defaultLogLevel, writer);
	}
	
	public Logger(Level level, BufferedWriter writer) {

		_level = level;
		_writer = writer;
	}
	
	public void logLazy(Level level, String message) throws IOException {
		
		if (level.compareTo(_level) >= 0) {
			
			_writer.write(message);
			_writer.newLine();
		}
	}
	
	public void log(Level level, String message) throws IOException {
		
		if (level.compareTo(_level) >= 0) {
			
			logLazy(level, message);
			flush();
		}
	}
	
	public void flush() throws IOException {
		
		_writer.flush();
	}
	
	public void info(String message) throws IOException {
		log(Level.INFO, message);
	}
	
	public void warning(String message) throws IOException {
		log(Level.WARNING, message);
	}
	
	public void error(String message) throws IOException {
		log(Level.ERROR, message);
	}
	

	private Level _level;
	
	private BufferedWriter _writer;
}
