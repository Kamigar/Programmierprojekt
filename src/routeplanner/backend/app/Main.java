package routeplanner.backend.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import routeplanner.backend.app.FileScanner;
import routeplanner.backend.model.*;

public class Main {
	
	public enum Code {

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

	private static class FatalFailure extends Exception {
		
		/**
		 * 
		 */
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
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -6297306542282662293L;

		public BadParameterException(String detail) { super(detail); }
	}

	private static class Parameters {
		
		public BufferedReader structureIn = null;
		public BufferedReader requestIn = null;
		public BufferedWriter requestOut = null;
		public BufferedWriter logOut = null;
		public int start = -1;
		public Logger.Level logLevel = null;
		public boolean isTolerant = false;
	}
	

	private static Parameters readParameters(String[] args)
			throws BadParameterException, IOException {
		
		Parameters p = new Parameters();
		
		for (int i = 0; i < args.length; i++) {
			
			switch (args[i]) {
			
			case "--input-file":
			case "-i":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No input file provided");

				p.structureIn = new BufferedReader(new FileReader(args[i]));
				break;
				
			case "--output-file":
			case "-o" :
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No output file provided");
				
				p.requestOut = new BufferedWriter(new FileWriter(args[i]));
				break;
				
			case "--request-file":
			case "-r":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No request file provided");
				
				p.requestIn = new BufferedReader(new FileReader(args[i]));
				break;
				
			case "--log-file":
			case "-l":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No log file provided");
				
				p.logOut = new BufferedWriter(new FileWriter(args[i]));
				break;
				
			case "--one-to-all":
			case "-ota":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No start point for one-to-all provided");
				
				p.start = Integer.parseUnsignedInt(args[i]);
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

				FileInputStream reader = new FileInputStream("etc/help.txt");
				byte[] data = reader.readAllBytes();
				
				System.out.print(new String(data, "UTF-8"));

				System.exit(Code.SUCCESS.value());

			default:
				
				throw new BadParameterException("Unknown parameter. Use -h for help");
			}
		}
		
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter stdout = new BufferedWriter(new OutputStreamWriter(System.out));
		
		if (p.structureIn == null)
			p.structureIn = stdin;
		
		if (p.requestIn == null)
			p.requestIn = stdin;
		
		if (p.logOut == null)
			p.logOut = stdout;

		if (p.requestOut == null) {
			
			p.requestOut = stdout;
			
			if (p.logLevel == null)
				p.logLevel = Logger.defaultStdOutLogLevel;
		}
		
		if (p.logLevel == null) {
			
			p.logLevel = Logger.defaultLogLevel;
		}

		return p;
	}

	public static void main(String[] args) {

		Parameters param = null;
		Logger logger = null;
		try {
			
			try {
			
				param = readParameters(args);
				
			} catch (BadParameterException ex) {
				
				System.out.println("Bad parameter provided");
				System.out.println(ex.getMessage());
				
				throw new FatalFailure(Code.BAD_PARAMETER, "Bad parameter provided");
			}
			
			logger = new Logger(param.logLevel, param.logOut);
			
			Node[] nodes = null;
			DijkstraNode[] calcNodes = null;
			long startTime, endTime;

			logger.info("Reading graph");

			try {

				startTime = System.nanoTime();

				nodes = FileScanner.readStructure(param.structureIn, logger, param.isTolerant);

				calcNodes = DijkstraNode.createTree(nodes);
				
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
					+ (double)(endTime - startTime) / 1000000000 + " seconds" + System.lineSeparator());
			
			
			if (param.start != -1) {

				// Calculate one to all
				if (param.start < 0 || param.start >= calcNodes.length) {
					
					logger.error("nodeID of starting point out of range");
					
					throw new FatalFailure(Code.BAD_PARAMETER, "OTA nodeID out of range");
				}
				
				Dijkstra.DijkstraStructure struct = Dijkstra.calculate(
						calcNodes, calcNodes[param.start], logger);
				
				for (DijkstraNode node : struct.ordered) {
					
					param.requestOut.write(String.valueOf(node.node().id()));
					param.requestOut.write(' ');
					param.requestOut.write(String.valueOf(node.distance()));
					param.requestOut.newLine();
				}
			
			} else {
				
				// Read multiple requests
				logger.info("Input format: [start:id] [end:id] e.g. 18445 12343");
				logger.info("  use multiple lines for multiple requests");
				logger.info("  end input with <EOF> (CTRL+D)");	

				int lastRequest = -1;
				while (true) {

					int[] request;
					try {
						
						logger.info(""); // Print new line

						request = FileScanner.readRequest(param.requestIn, calcNodes.length, logger, param.isTolerant);
						
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
						
						DijkstraNode.reset(calcNodes);


						startTime = System.nanoTime();
						
						Dijkstra.calculate(
								calcNodes, calcNodes[lastRequest], logger);
						
						endTime = System.nanoTime();	


						logger.info(System.lineSeparator() + "Path calculated in "
								+ (double)(endTime - startTime) / 1000000000 + " seconds");	
					}
					

					DijkstraNode dst = calcNodes[request[1]];

					param.requestOut.write(String.valueOf(dst.distance()));
					param.requestOut.newLine();
					
					logger.info("Path:");
					logger.info(dst.toPath());
					logger.info("Distance:");
					logger.info(String.valueOf(dst.distance()));
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
