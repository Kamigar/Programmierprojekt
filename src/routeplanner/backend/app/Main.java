package routeplanner.backend.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import routeplanner.backend.app.FileScanner;
import routeplanner.backend.model.*;

/*
 * Program flow and user interaction
 */
public class Main {
	
	/*
	 * Return codes of program
	 */
	static enum Code {

		SUCCESS(0),
		
		UNHANDLED_EXCEPTION(-99),
		IO_EXCEPTION(-1),
		
		BAD_PARAMETER(-2),
		BAD_HEADER(-3),
		BAD_NODE(-4),
		BAD_EDGE(-5),
		BAD_REQUEST(-6);
		
		private Code(int value) { _value = value; }
		
		public int value() { return _value; }
		
		private int _value;
	}	

	// Exception classes

	static class FatalFailure extends Exception {
		
		private static final long serialVersionUID = 988623077434694182L;

		public FatalFailure(Code code, String message) {
			super(message);
			_code = code;
		}
		
		public Code code() {
			return _code;
		}
		
		private Code _code;
	}
	
	private static class BadParameterException extends Exception {
		
		private static final long serialVersionUID = -6297306542282662293L;

		public BadParameterException(String detail) { super(detail); }
	}
	
	/*
	 * Calculation modes of the program
	 */
	static enum Mode {
		OTO, // one-to-one
		OTA, // one-to-all
		OTM, // one-to-many
		NNI, // next-node-iterative
		NNF, // next-node-fast
	}

	/*
	 * Parameters for the program execution
	 */
	static class Parameters {
		
		public BufferedReader structureIn = null;
		public BufferedReader requestIn = null;
		public BufferedWriter requestOut = null;
		public BufferedWriter logOut = null;
		public Mode mode = Mode.OTO;
		public int start = -1;
		public Logger.Level logLevel = null;
		public boolean isTolerant = false;
	}
	
	// Read command line parameters
	private static Parameters readParameters(String[] args)
			throws BadParameterException, IOException {
		
		Parameters p = new Parameters();
		
		String structureIn = null, requestIn = null;
		String requestOut = null, logOut = null;
		
		for (int i = 0; i < args.length; i++) {
			
			switch (args[i]) {
			
			case "--input-file":
			case "-i":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No input file provided");

				structureIn = args[i];
				break;
				
			case "--output-file":
			case "-o" :
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No output file provided");
				
				requestOut = args[i];
				break;
				
			case "--request-file":
			case "-r":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No request file provided");
				
				requestIn = args[i];
				break;
				
			case "--log-file":
			case "-l":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No log file provided");
				
				logOut = args[i];
				break;
				
			case "--one-to-all":
			case "-ota":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No start point for one-to-all provided");
				
				try {
					p.start = Integer.parseUnsignedInt(args[i]);
				} catch (NumberFormatException ex) {
					throw new BadParameterException("Bad start point for one-to-all provided");
				}
				p.mode = Mode.OTA;
				break;
				
			case "--one-to-many":
			case "-otm":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No start point for one-to-many provided");
				
				try {
					p.start = Integer.parseUnsignedInt(args[i]);
				} catch (NumberFormatException ex) {
					throw new BadParameterException("Bad start point for one-to-many provided");
				}
				p.mode = Mode.OTM;
				break;
				
			case "--next-node-iterative":
			case "-nni":
				
				p.mode = Mode.NNI;
				break;
				
			case "--next-node-fast":
			case "-nnf":
				
				p.mode = Mode.NNF;
				break;
				
			case "--tolerant":
			case "-t":
				
				p.isTolerant = true;
				break;

			case "--quiet":
			case "-q":
				
				p.logLevel = Logger.Level.ERROR;
				break;
				
			case "--verbose":
			case "-v":
				
				p.logLevel = Logger.Level.INFO;
				break;
				
			case "--warning":
			case "-w":
				
				p.logLevel = Logger.Level.WARNING;
				break;

			case "--help":
			case "-h":

				InputStream reader = Main.class.getResourceAsStream("/help.txt");
				byte[] data = reader.readAllBytes();
				
				System.out.print(new String(data, "UTF-8"));

				System.exit(Code.SUCCESS.value());

			default:
				
				throw new BadParameterException("Unknown parameter. Use -h for help");
			}
		}
		
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		BufferedWriter stdout = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
					
		try {
			
			if (structureIn == null)
				p.structureIn = stdin;
			else
				p.structureIn = new BufferedReader(new InputStreamReader(new FileInputStream(structureIn), "UTF-8"));
			
			if (requestIn == null)
				p.requestIn = stdin;
			else
				if (structureIn != null && Files.exists(Paths.get(requestIn))
					&& Files.isSameFile(Paths.get(requestIn), Paths.get(structureIn)))
					p.requestIn = p.structureIn;
				else
					p.requestIn = new BufferedReader(new InputStreamReader(new FileInputStream(requestIn), "UTF-8"));
			
			if (requestOut == null)
				p.requestOut = stdout;
			else
				p.requestOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(requestOut), "UTF-8"));
			
			if (logOut == null)
				p.logOut = stdout;
			else
				if (requestOut != null && Files.exists(Paths.get(logOut))
					&& Files.isSameFile(Paths.get(logOut), Paths.get(requestOut)))
					p.logOut = p.requestOut;
				else
					p.logOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logOut), "UTF-8"));	
		
		} catch (FileNotFoundException ex) {
			
			throw new BadParameterException("File not found");
			
		} catch (InvalidPathException ex) {
			
			throw new BadParameterException("Invalid path provided");
			
		} catch (SecurityException ex) {
			
			throw new BadParameterException("Security exception while opening file");
		}
		

		if (p.logLevel == null) {
			
			if (p.requestOut == p.logOut)
				p.logLevel = Logger.defaultSameOutputLogLevel;
			else
				p.logLevel = Logger.defaultLogLevel;
		}

		return p;
	}
	
	// Run garbage collection
	private static void finishInitialization(Logger logger) throws IOException {
		
		logger.info("Run garbage collection");

		Runtime.getRuntime().gc();
	}

	// Main function
	public static void main(String[] args) {

		Parameters param = null;
		Logger logger = null;
		try {
			
			try {
			
				// Read command line parameters
				param = readParameters(args);
				
			} catch (BadParameterException ex) {
				
				System.out.println("Bad parameter provided");
				System.out.println(ex.getMessage());
				
				throw new FatalFailure(Code.BAD_PARAMETER, "Bad parameter provided");
			}
			
			logger = new Logger(param.logLevel, param.logOut);
			
			Node[] nodes = null;
			long startTime, endTime;

			logger.info(System.lineSeparator() + "Reading graph" + System.lineSeparator());

			try {

				startTime = System.nanoTime();

				// Read graph description file
				nodes = FileScanner.readStructure(param.structureIn, logger, param.isTolerant);
				
				endTime = System.nanoTime();	

			} catch (FileScanner.BadHeaderException ex) {
				
				logger.error("Bad header provided");
				logger.info(ex.getMessage());
				
				throw new FatalFailure(Code.BAD_HEADER, "Bad header provided");

			} catch (FileScanner.BadNodeException ex) {
				
				logger.error("Bad node provided");
				logger.info(ex.getMessage());
				
				throw new FatalFailure(Code.BAD_NODE, "Bad node provided");

			} catch (FileScanner.BadEdgeException ex) {
				
				logger.error("Bad edge provided");
				logger.info(ex.getMessage());
				
				throw new FatalFailure(Code.BAD_EDGE, "Bad edge provided");
			}

			logger.info(System.lineSeparator() + nodes.length + " nodes read in "
					+ (double)(endTime - startTime) / 1000000000 + " seconds");
			

			logger.info(System.lineSeparator() + "Prepare data for calculation");

			switch (param.mode) {
			
			case OTO:
			case OTA:
			case OTM:
				
				DijkstraApp dijkstra = new DijkstraApp();

				startTime = System.nanoTime();
				
				dijkstra.prepare(nodes);
				finishInitialization(logger);

				endTime = System.nanoTime();
				
				logger.info("Initialization finished in " + (double)(endTime - startTime) / 1000000000 + " seconds");

				// Run Dijkstra calculation
				dijkstra.run(param, nodes, logger);
				break;
				
			case NNI:
			case NNF:
				
				NextNodeApp nextNode = new NextNodeApp();
				
				startTime = System.nanoTime();
				
				nextNode.prepare(nodes);
				finishInitialization(logger);

				endTime = System.nanoTime();

				logger.info("Initialization finished in " + (double)(endTime - startTime) / 1000000000 + " seconds");

				// Run next node calculation
				nextNode.run(param, nodes, logger);
				break;
			}

		} catch (FatalFailure failure) {
			
			System.out.println(System.lineSeparator() + "Fatal failure in program execution");
			System.out.println("  " + failure.getMessage());
			
			System.exit(failure.code().value());

		} catch (IOException ex) {
			
			System.out.println(System.lineSeparator() + "I/O exception");
			System.out.println("  " + ex.getMessage());

			System.exit(Code.IO_EXCEPTION.value());

		} catch (Exception ex) {
			
			System.out.println(System.lineSeparator() + "Unhandled exception occured");
			System.out.println(ex.getStackTrace());

			System.exit(Code.UNHANDLED_EXCEPTION.value());

		} finally {
			
			try {
			
				if (logger != null)
					logger.flush();

				if (param != null) {

					if (param.structureIn != null)
						param.structureIn.close();
					if (param.requestIn != null)
						param.requestIn.close();
					if (param.requestOut != null)
						param.requestOut.close();
					if (param.logOut != null)
						param.logOut.close();
				}
				
			} catch (IOException ex) {
				
				System.out.println("Error while closing files");
				System.out.println("  " + ex.getMessage());
				System.exit(Code.IO_EXCEPTION.value());
			}
		}

		System.exit(Code.SUCCESS.value());
	}
}
