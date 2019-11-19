package routeplanner.backend.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import routeplanner.backend.model.*;

public class Main {
	
	private static class Parameters {
		
		public BufferedReader structureIn = null;
		public BufferedReader requestIn = null;
		public BufferedWriter requestOut = null;
		public BufferedWriter logOut = null;
		public int start = -1;
		public Logger.Level logLevel = null;
	}

	private static Parameters readParameters(String[] args) throws IOException {
		
		Parameters p = new Parameters();
		
		for (int i = 0; i < args.length; i++) {
			
			switch (args[i]) {
			
			case "--input-file":
			case "-i":
				
				i++;
				if (args.length == i)
					throw null;

				p.structureIn = new BufferedReader(new FileReader(args[i]));
				break;
				
			case "--output-file":
			case "-o" :
				
				i++;
				if (args.length == i)
					throw null;
				
				p.requestOut = new BufferedWriter(new FileWriter(args[i]));
				break;
				
			case "--request-file":
			case "-req":
				
				i++;
				if (args.length == i)
					throw null;
				
				p.requestIn = new BufferedReader(new FileReader(args[i]));
				break;
				
			case "--log-file":
			case "-l":
				
				i++;
				if (args.length == i)
					throw null;
				
				p.logOut = new BufferedWriter(new FileWriter(args[i]));
				break;
				
			case "--one-to-all":
			case "-ota":
				
				i++;
				if (args.length == i)
					throw null;
				
				p.start = Integer.parseUnsignedInt(args[i]);
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
				
			default:
				
				throw null;
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
				p.logLevel = Logger.Level.ERROR;
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
			
			param = readParameters(args);
			
			logger = new Logger(param.logLevel, param.logOut);
			

			logger.info("Reading file");


			long startTime = System.nanoTime();

			Node[] nodes = FileScanner.readStructure(param.structureIn, logger);

			DijkstraNode[] calcNodes = DijkstraNode.createTree(nodes);
			
			long endTime = System.nanoTime();
			

			logger.info("" + nodes.length + " nodes read in "
					+ (double)(endTime - startTime) / 1000000000 + " seconds");
			
			
			if (param.start != -1) {

				// Calculate one to all
				if (param.start < 0 || param.start >= calcNodes.length)
					throw null;
				
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
				int[] request;
				
				request = FileScanner.readRequest(param.requestIn, logger);

				while (request != null) {
					
					if (request[0] != lastRequest) {
						
						lastRequest = request[0];
						
						DijkstraNode.reset(calcNodes);

						if (request[0] < 0 || request[0] >= calcNodes.length)
							throw null;
						

						startTime = System.nanoTime();
						
						Dijkstra.calculate(
								calcNodes, calcNodes[request[0]], logger);
						
						endTime = System.nanoTime();	


						logger.info("Path calculated in "
								+ (double)(endTime - startTime) / 1000000000 + " seconds");	
					}
					
					if (request[1] < 0 || request[1] >= calcNodes.length)
						throw null;
					
					DijkstraNode dst = calcNodes[request[1]];

					param.requestOut.write(String.valueOf(dst.distance()));
					param.requestOut.newLine();
					
					logger.info("Path:");
					logger.info(dst.toPath());
					logger.info("Distance:");
					logger.info(String.valueOf(dst.distance()));
					
					request = FileScanner.readRequest(param.requestIn, logger);
				}	
			}

		} catch (IOException ex) {
			
			System.out.println("I/O exception");
			System.exit(-1);

		} catch (Exception ex) {
			
			System.out.println("General failure");
			System.exit(-2);

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
				System.exit(-3);
			}
		}
	}
}
