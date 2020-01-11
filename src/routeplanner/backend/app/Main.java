package routeplanner.backend.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	private static enum Code {

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

	private static class FatalFailure extends Exception {
		
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
	
	private static enum Mode {
		OTO,
		OTA,
		OTM
	}

	/*
	 * Parameters for the program execution
	 */
	private static class Parameters {
		
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

				// Note: Assuming 'etc/help.txt' is accessible
				FileInputStream reader = new FileInputStream("etc/help.txt");
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

			Dijkstra dijkstra = new Dijkstra();

			dijkstra.prepare(nodes);
			

			logger.info("Run garbage collection");

			Runtime.getRuntime().gc();
			

			logger.info(System.lineSeparator() + "Initialization finished");
			

			if (param.start != -1) {

				// Calculate distances from one starting point
				if (param.start < 0 || param.start >= nodes.length) {
					
					logger.error("nodeID of starting point out of range");
					
					throw new FatalFailure(Code.BAD_PARAMETER, "nodeID out of range");
				}
				

				logger.info(System.lineSeparator() + "Start calculation");

				startTime = System.nanoTime();

				// Calculate distances
				dijkstra.calculate(param.start);

				endTime = System.nanoTime();	


				logger.info("Path calculated in " + (double)(endTime - startTime) / 1000000000 + " seconds" + System.lineSeparator()
						+ System.lineSeparator() + "Distances: [trgID] [distance]");
				
				dijkstra.getResult(nodes);

				if (param.mode == Mode.OTA) {

					// Output result
					for (Node node : nodes) {
						
						String line = String.valueOf(node.id()) + ' ' + ParseUtilities.intToString(node.distance());
						
						param.requestOut.write(line);
						param.requestOut.newLine();

						logger.info(line);
					}	

				} else {
					
					// Read multiple requests
					logger.info(System.lineSeparator()
						+ "Input format: [trgID] e.g. 18445" + System.lineSeparator()
						+ "  use multiple lines for multiple requests" + System.lineSeparator()
						+ "  end input with <EOF> (CTRL+D)" + System.lineSeparator());

					FileScanner.StringPosition pos = new FileScanner.StringPosition();
					while (true) {

						int trgId;
						try {
							
							// Read target request
							trgId = FileScanner.readTarget(param.requestIn, pos, nodes.length, logger, param.isTolerant);

						} catch (FileScanner.BadRequestException ex) {
							
							logger.error("Bad request provided");
							logger.info(ex.getMessage());
							
							throw new FatalFailure(Code.BAD_REQUEST, "Bad request provided");
						}

						// Note: No 'while (trgId != -1) {...}' loop used
						//   to prevent code duplication of 'FileScanner.readRequest(...)'
						if (trgId == -1)
							break;
						
						// Output result

						Node dst = nodes[trgId];

						String distance = ParseUtilities.intToString(dst.distance());

						param.requestOut.write(distance);
						param.requestOut.newLine();

						logger.info("Distance: " + distance + System.lineSeparator());
					}
				}
			
			} else {
				
				// Read multiple requests
				logger.info(System.lineSeparator()
					+ "Input format: [srcID] [trgID] e.g. 18445 12343" + System.lineSeparator()
					+ "  use multiple lines for multiple requests" + System.lineSeparator()
					+ "  end input with <EOF> (CTRL+D)" + System.lineSeparator());

				FileScanner.StringPosition pos = new FileScanner.StringPosition();
				int lastRequest = -1;
				while (true) {

					int[] request;
					try {
						
						// Read path request
						request = FileScanner.readRequest(param.requestIn, pos, nodes.length, logger, param.isTolerant);
						
					} catch (FileScanner.BadRequestException ex) {
						
						logger.error("Bad request provided");
						logger.info(ex.getMessage());
					
						throw new FatalFailure(Code.BAD_REQUEST, "Bad request provided");
					}	
					
					// Note: No 'while (request != null) {...}' loop used
					//   to prevent code duplication of 'FileScanner.readRequest(...)'
					if (request == null)
						break;

					if (request[0] != lastRequest) {
						
						lastRequest = request[0];
						
						// Reset nodes for new calculation
						Node.reset(nodes);


						logger.info("Start calculation");

						startTime = System.nanoTime();
						
						// Calculate distances
						dijkstra.calculate(lastRequest);
						
						endTime = System.nanoTime();	


						logger.info("Path calculated in " + (double)(endTime - startTime) / 1000000000 + " seconds" + System.lineSeparator());	
						
						dijkstra.getResult(nodes);
					}
					
					// Output result

					Node dst = nodes[request[1]];

					String distance = ParseUtilities.intToString(dst.distance());

					param.requestOut.write(distance);
					param.requestOut.newLine();

					logger.info("Distance: " + distance + System.lineSeparator());
				}	
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
