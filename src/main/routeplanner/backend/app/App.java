package routeplanner.backend.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;

import routeplanner.backend.model.Node;

/*
 * Implementation of the program functions
 */
public class App {
	
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

	/*
	 * Exception class for fatal (non-recoverable) failure
	 */
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

	/*
	 * Calculation modes of the program
	 */
	static enum Mode {
		NONE,
		
		OTO, // one-to-one
		OTA, // one-to-all
		OTM, // one-to-many
		NNI, // next-node-iterative
		NNF, // next-node-fast
		SRV, // server
	}
	
	/*
	 * Parameters for the program execution
	 */
	static class Parameters {
		
		public BufferedReader structureIn = null;
		public BufferedReader requestIn = null;
		public BufferedWriter requestOut = null;
		public BufferedWriter logOut = null;
		public Mode mode = Mode.NONE;
		public int start = -1;
		public int port = 80;
		public URL htmlDirectory = Parameters.class.getResource("/html");
		public Logger.Level logLevel = null;
		public boolean isTolerant = false;
	}
	

	// Prepare data for Dijkstra calculation
	public void prepareDijkstra() {
		
		_dijkstra = new Dijkstra();
		_dijkstra.prepare(_nodes);
	}
	
	// Prepare data for next node calculation
	public void prepareNextNode() {
		
		_nextNode = new NextNode();
		_nextNode.prepare(_nodes);
	}
	
	// Prepare data for calculation (Dijkstra & next node)
	public void prepare() {
		
		prepareDijkstra();
		prepareNextNode();
	}
	
	// Read graph from input stream
	public Node[] readGraph(Parameters param, Logger logger) throws IOException, FatalFailure {
		
		_nodes = null;
		long startTime, endTime;

		logger.info(System.lineSeparator() + "Reading graph" + System.lineSeparator());

		try {

			startTime = System.nanoTime();

			// Read graph description file
			_nodes = FileScanner.readStructure(param.structureIn, logger, param.isTolerant);
			
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

		logger.info(System.lineSeparator() + _nodes.length + " nodes read in "
				+ (double)(endTime - startTime) / 1000000000 + " seconds");
		
		return _nodes;	
	}
	
	// Calculate distances from one starting point
	public void runSingleDijkstra(Parameters param, Logger logger) throws IOException, FatalFailure {
		
		if (param.start < 0 || param.start >= _nodes.length) {
			
			logger.error("nodeID of starting point out of range");
			
			throw new FatalFailure(Code.BAD_PARAMETER, "nodeID out of range");
		}
		

		logger.info(System.lineSeparator() + "Start calculation");

		long startTime = System.nanoTime();

		// Calculate distances
		_dijkstra.calculate(param.start);

		long endTime = System.nanoTime();	


		logger.info("Path calculated in " + (double)(endTime - startTime) / 1000000000 + " seconds" + System.lineSeparator()
				+ System.lineSeparator() + "Distances: [trgID] [distance]");
		
		_dijkstra.getResult(_nodes);

		switch (param.mode) {
		
		case OTA:

			// Output result
			for (Node node : _nodes) {
				
				String line = "" + node.id() + " " + node.distance();
				
				param.requestOut.write(line);
				param.requestOut.newLine();

				logger.info(line);
			}		
			break;	

		case OTM:
			
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
					trgId = FileScanner.readId(param.requestIn, pos, _nodes.length, logger, param.isTolerant);

				} catch (FileScanner.BadRequestException ex) {
					
					logger.error("Bad request provided");
					logger.info(ex.getMessage());
					
					throw new FatalFailure(Code.BAD_REQUEST, "Bad request provided");
				}

				// Note: No 'while (trgId != -1) {...}' loop used
				//   to prevent code duplication of 'FileScanner.readId(...)'
				if (trgId == -1)
					break;
				
				// Output result

				Node dst = _nodes[trgId];

				param.requestOut.write("" + dst.distance());
				param.requestOut.newLine();

				logger.info("Distance: " + dst.distance() + System.lineSeparator());
			}	
			break;
			
		default:
			// Should never happen
			throw new FatalFailure(Code.BAD_PARAMETER, "Wrong mode for single Dijkstra calculation");
		}
	}
	
	// Calculate distances from multiple starting points
	public void runMultipleDijkstra(Parameters param, Logger logger) throws IOException, FatalFailure {

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
				request = FileScanner.readRequest(param.requestIn, pos, _nodes.length, logger, param.isTolerant);
				
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
				
				_dijkstra.reset();

				logger.info("Start calculation");

				long startTime = System.nanoTime();
				
				// Calculate distances
				_dijkstra.calculate(lastRequest);
				
				long endTime = System.nanoTime();	


				logger.info("Path calculated in " + (double)(endTime - startTime) / 1000000000 + " seconds" + System.lineSeparator());	
				
				_dijkstra.getResult(_nodes);
			}
			
			// Output result

			Node dst = _nodes[request[1]];

			param.requestOut.write("" + dst.distance());
			param.requestOut.newLine();

			logger.info("Distance: " + dst.distance() + System.lineSeparator());
		}		
	}
	
	// Calculate next node
	public void runNextNode(Parameters param, Logger logger) throws IOException, FatalFailure {
		
		logger.info(System.lineSeparator()
				+ "Input format: [latitude] [longitude] e.g. 49.2 9.8" + System.lineSeparator()
				+ "  use multiple lines for multiple requests" + System.lineSeparator()
				+ "  end input with <EOF> (CTRL+D)" + System.lineSeparator());

		FileScanner.StringPosition pos = new FileScanner.StringPosition();
		while (true) {

			double[] req;
			try {
				
				// Read request
				req = FileScanner.readCoordinates(param.requestIn, pos, logger, param.isTolerant);
				
			} catch (FileScanner.BadRequestException ex) {
				
				logger.error("Bad request provided");
				logger.info(ex.getMessage());
			
				throw new FatalFailure(Code.BAD_REQUEST, "Bad request provided");
			}	
			
			// Note: No 'while (req != null) {...}' loop used
			//   to prevent code duplication of 'FileScanner.readCoordinates(...)'
			if (req == null)
				break;
			
			
			double distance;
			long startTime, endTime;

			// Search for nearest node
			switch (param.mode) {
			
			case NNI:
				
				startTime = System.nanoTime();
				
				distance = _nextNode.findNextIterative(req[0], req[1]);
				
				endTime = System.nanoTime();
				break;

			case NNF:
				
				startTime = System.nanoTime();
				
				distance = _nextNode.findNext(req[0], req[1]);
				
				endTime = System.nanoTime();
				break;

			default:
				// Should never happen
				throw new FatalFailure(Code.BAD_PARAMETER, "Wrong mode for next node calculation");
			}
			
			logger.info("Nearest node found in " + (double)(endTime - startTime) / 1000000 + " ms" + System.lineSeparator());
			
			Node[] result = _nextNode.getResult(_nodes);

			// Output result
			
			param.requestOut.write("" + distance);
			for (Node n : result)
				param.requestOut.write(" " + n.id());
			param.requestOut.newLine();

			for (Node n : result)
				logger.info("Node: " + n.id() + " latitude: " + n.latitude() + " longitude: " + n.longitude());
			logger.info("Distance: " + distance + System.lineSeparator());
		}	
	}
	

	// Dijkstra implementation
	private Dijkstra _dijkstra;
	
	// Next node implementation
	private NextNode _nextNode;
	
	// Node list (graph)
	private Node[] _nodes;
}