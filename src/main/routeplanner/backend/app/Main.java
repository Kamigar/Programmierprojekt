package routeplanner.backend.app;

import java.util.HashMap;
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

import routeplanner.backend.app.App.FatalFailure;
import routeplanner.backend.app.App.Code;
import routeplanner.backend.app.App.Mode;
import routeplanner.backend.app.App.Parameters;

/*
 * Program flow and user interaction
 */
public class Main {
	
	/*
	 * Exception class for bad parameter
	 */
	private static class BadParameterException extends Exception {
		
		private static final long serialVersionUID = -6297306542282662293L;

		public BadParameterException(String detail) { super(detail); }
	}


	// Read command line parameters
	private static Parameters readParameters(String[] args) throws BadParameterException, IOException {
		
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
				
			case "--server":
			case "-srv":
				
				p.mode = Mode.SRV;
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
	
	/*
	 * Shutdown hook - Runs after program termination
	 *   Close I/O streams and stop server
	 */
	public static class ShutdownHook extends Thread {
		
		@Override
		public void run() {
			
			try {

				if (server != null) {
					
					if (logger != null)
						logger.info("Stopping server");
					
					server.stop(1);
				}	
				
				if (param != null) {
					
					if (param.structureIn != null && param.structureIn != param.requestIn)
						param.structureIn.close();
					
					if (param.requestIn != null)
						param.requestIn.close();
					
					if (param.requestOut != null && param.requestOut != param.logOut)
						param.requestOut.close();

					// Note: 'param.logOut' will be closed through logger
				}

				if (logger != null) {
					
					logger.info("Execution finished");
					logger.close();
				}

			} catch (IOException ex) {
				
				System.out.println("I/O exception while shutting down application");
			}
		}
		
		public Parameters param;
		public Logger logger;
		public Server server;
	}
	
	// Main function
	public static void main(String[] args) {
		
		try {
			
			ShutdownHook hook = new ShutdownHook();
			Runtime.getRuntime().addShutdownHook(hook);

			Parameters param = null;
			Logger logger = null;
			App app = new App();	

			try {
			
				// Read command line parameters
				param = readParameters(args);
				hook.param = param;
				
			} catch (BadParameterException ex) {
				
				System.out.println("Bad parameter provided");
				System.out.println(ex.getMessage());
				
				throw new FatalFailure(Code.BAD_PARAMETER, "Bad parameter provided");
			}
			
			logger = new Logger(param.logLevel, param.logOut);
			hook.logger = logger;


			// Read graph
			app.readGraph(param, logger);


			// Prepare data for calculation
			
			logger.info(System.lineSeparator() + "Prepare data for calculation");
			
			long startTime = System.nanoTime();
			
			switch (param.mode) {
			
			case OTO:
			case OTA:
			case OTM:
				
				app.prepareDijkstra();
				break;
				
			case NNI:
			case NNF:
				
				app.prepareNextNode();
				break;
				
			case SRV:
				
				app.prepare();
				break;
			}
			
			Runtime.getRuntime().gc();

			long endTime = System.nanoTime();

			logger.info("Initialization finished in " + (double)(endTime - startTime) / 1000000000 + " seconds");


			// Run calculation(s)

			switch (param.mode) {
			
			case OTO:
				
				app.runMultipleDijkstra(param, logger);
				break;
				
			case OTA:
			case OTM:
				
				app.runSingleDijkstra(param, logger);
				break;
				
			case NNI:
			case NNF:
				
				app.runNextNode(param, logger);
				break;
				
			case SRV:
				
				Server server = new Server();
				
				HashMap<String, byte[]> html = new HashMap<String, byte[]>();

				InputStream s = Main.class.getResourceAsStream("/html/index.html");
				byte[] data = s.readAllBytes();
				s.close();

				html.put("/", data);
				html.put("/index.html", data);

				server.start(80, 10, html, app, logger);
				hook.server = server;
				
				System.out.println(System.lineSeparator() + "Server started... Press [enter] to shut down");
				System.in.read();
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

		}

		System.exit(Code.SUCCESS.value());
	}
}
