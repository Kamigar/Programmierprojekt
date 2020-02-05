package routeplanner.backend.app;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
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
					
					server.stop(serverShutdownDelay);
				}	
				
				if (param != null) {
					
					if (param.structureIn != null && param.structureIn != stdinReader && param.structureIn != param.requestIn)
						param.structureIn.close();
					
					if (param.requestIn != null && param.requestIn != stdinReader)
						param.requestIn.close();
					
					if (param.requestOut != null && param.requestOut != stdoutWriter && param.requestOut != param.logOut)
						param.requestOut.close();

					// Note: 'param.logOut' will be closed through logger
				}

				if (logger != null) {
					
					logger.info("Execution finished");

					if (param.logOut != stdoutWriter)
						logger.close();
					else
						logger.flush();
				}

			} catch (IOException ex) {
				
				System.out.println("I/O exception while shutting down application");
			}
		}
		
		public Parameters param;
		public Logger logger;
		public Server server;
	}
	

	// Some compile-time parameters
	static final int serverPortNumber = 80;
	static final int serverBacklogSize = 10;
	static final int serverShutdownDelay = 1;
	static final String serverRootRedirection = "/index.html";
	static final String serverRouteplannerPath = "/routeplanner";

	static final String htmlDirPath = "/html";
	static final String helpFilePath = "/help.txt";

	// Default stream writers (System.in/System.out)
	private static final Charset utf8 = Charset.forName("utf-8");
	private static final BufferedReader stdinReader = new BufferedReader(new InputStreamReader(System.in, utf8));
	private static final BufferedWriter stdoutWriter = new BufferedWriter(new OutputStreamWriter(System.out, utf8));
	

	// Read command line parameters
	private static Parameters readParameters(String[] args) throws BadParameterException, IOException {
		
		Parameters p = new Parameters();
		
		String structureIn = null, requestIn = null;
		String requestOut = null, logOut = null;
		
		// Parse command line parameters
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
				
			case "--one-to-one":
			case "-oto":
				
				p.mode = Mode.OTO;
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
				
			case "--port":
			case "-p":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No port number provided");
				
				try {
					p.port = Integer.parseUnsignedInt(args[i]);
				} catch (NumberFormatException ex) {
					throw new BadParameterException("Bad port number provided");
				}
				break;
				
			case "--html-directory":
			case "-d":
				
				i++;
				if (args.length == i)
					throw new BadParameterException("No HTML directory provided");
				
				String dir = new File(args[i]).toURI().toString();

				// Remove trailing '/' from directory path
				if (dir.endsWith("/"))
					dir = dir.substring(0, dir.length() - 1);

				p.htmlDirectory = new URL(dir);
				break;
				
			case "--print-location":
			case "-pl":
				
				p.printLocation = true;
				break;
				
			case "--print-distance":
			case "-pd":
				
				p.printDistance = true;
				break;
				
			case "--print-path":
			case "-pp":
				
				p.printPath = true;
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

				InputStream reader = Main.class.getResourceAsStream(helpFilePath);
				byte[] data = reader.readAllBytes();
				
				System.out.print(new String(data, utf8));

				System.exit(Code.SUCCESS.value());

			default:

				throw new BadParameterException("Unknown parameter. Use -h for help");
			}
		}
		
		// Throw exception if no operation/mode (e.g. '--oto') was specified
		if (p.mode == Mode.NONE) {
			throw new BadParameterException("No operation specified. Use -h for help");
		}
		

		// Create I/O reader/writer

		try {
			
			if (structureIn == null)
				p.structureIn = stdinReader;
			else
				p.structureIn = new BufferedReader(new InputStreamReader(new FileInputStream(structureIn), utf8));
			
			if (requestIn == null)
				p.requestIn = stdinReader;
			else
				if (structureIn != null && Files.exists(Paths.get(requestIn))
					&& Files.isSameFile(Paths.get(requestIn), Paths.get(structureIn)))
					p.requestIn = p.structureIn;
				else
					p.requestIn = new BufferedReader(new InputStreamReader(new FileInputStream(requestIn), utf8));
			
			if (requestOut == null)
				p.requestOut = stdoutWriter;
			else
				p.requestOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(requestOut), utf8));
			
			if (logOut == null)
				p.logOut = stdoutWriter;
			else
				if (requestOut != null && Files.exists(Paths.get(logOut))
					&& Files.isSameFile(Paths.get(logOut), Paths.get(requestOut)))
					p.logOut = p.requestOut;
				else
					p.logOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logOut), utf8));	
		
		} catch (FileNotFoundException ex) {
			
			throw new BadParameterException("File not found");
			
		} catch (InvalidPathException ex) {
			
			throw new BadParameterException("Invalid path provided");
			
		} catch (SecurityException ex) {
			
			throw new BadParameterException("Security exception while opening file");
		}
		

		// Set log level

		if (p.logLevel == null) {
			
			if (p.requestOut == p.logOut)
				p.logLevel = Logger.defaultSameOutputLogLevel;
			else
				p.logLevel = Logger.defaultLogLevel;
		}
		
		if (p.logLevel == Logger.Level.INFO && p.logOut == stdoutWriter)
			p.logLevel = Logger.Level.INSTRUCTION;

		return p;
	}
	
	// Return HashMap (filename -> data) of files in directory 'url'
	private static HashMap<String, byte[]> getHtmlData(URL url, Logger logger) throws IOException, FatalFailure {
		
		HashMap<String, byte[]> res = new HashMap<String, byte[]>();
		
		if (url.getProtocol().contentEquals("jar")) {
			
			// Skip 'file:' at beginning and split as 'path'!'resourceFolder'
			String path = url.getPath().substring(5, url.getPath().indexOf('!'));
			String resourceFolder = url.getPath().substring(url.getPath().indexOf('!') + 2, url.getPath().length());
			
			JarFile jar = new JarFile(path);
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				
				String file = entries.nextElement().getName();

				// Ignore files outside 'resourceFolder' and directories
				if (file.startsWith(resourceFolder) && file.lastIndexOf("/") < file.length() - 1) {
					
					InputStream s = Main.class.getResourceAsStream("/" + file);
					res.put(file.substring(resourceFolder.length(), file.length()), s.readAllBytes());
					s.close();
				}
			}
			jar.close();

		} else if (url.getProtocol().contentEquals("file")) {
			
			LinkedList<String> files = new LinkedList<String>();

			// Find all files recursively
			Files.find(Paths.get(url.getPath()), 128,
				(path, filter) -> filter.isRegularFile())
					.forEach(x -> files.add(x.toString()));

			for (String file : files) {
				
				FileInputStream s = new FileInputStream(file);
				res.put(file.substring(url.getPath().length(), file.length()), s.readAllBytes());
				s.close();
			}

		} else {
			
			logger.error("Resource folder uses unsupported protocol '" + url.getProtocol() + "'");
			throw new FatalFailure(Code.IO_EXCEPTION, "Resources with unsupported protocol");
		}
		return res;
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
			
			if (param.structureIn != stdinReader && param.structureIn != param.requestIn) {
				// Close structure input stream
				param.structureIn.close();
				param.structureIn = null;
			}


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
				
			case NONE:
				// Should never happen
				throw new FatalFailure(Code.BAD_PARAMETER, "No mode provided");
			}
			
			Runtime.getRuntime().gc();

			long endTime = System.nanoTime();

			logger.info("Initialization finished in " + (double)(endTime - startTime) / 1000000000 + " seconds" + System.lineSeparator());


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

				// Increase log level to prevent logging of instructions for every request
				if (logger.level() == Logger.Level.INSTRUCTION)
					logger.setLevel(Logger.Level.INFO);

				// Close request streams
				if (param.requestIn != stdinReader) {
					param.requestIn.close();
					param.requestIn = null;
				}
				if (param.requestOut != stdoutWriter && param.requestOut != param.logOut) {
					param.requestOut.close();
					param.requestOut = null;
				}


				Server server = new Server();

				// Read HTML files
				HashMap<String, byte[]> html = getHtmlData(param.htmlDirectory, logger);

				HashMap<String, String> redirections = new HashMap<String, String>();
				redirections.put("/", serverRootRedirection);
				
				// Start server
				server.start(param.port, serverBacklogSize, serverRouteplannerPath, html, redirections, app, logger);
				hook.server = server;
				
				System.out.println(System.lineSeparator() + "Server started... Press [enter] to shut down");
				System.in.read();
				break;
				
			case NONE:
				// Should never happen
				throw new FatalFailure(Code.BAD_PARAMETER, "No mode provided");
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
			ex.printStackTrace(System.out);

			System.exit(Code.UNHANDLED_EXCEPTION.value());
		}

		System.exit(Code.SUCCESS.value());
	}
}
