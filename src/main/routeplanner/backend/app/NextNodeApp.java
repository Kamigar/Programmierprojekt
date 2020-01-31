package routeplanner.backend.app;

import java.io.IOException;

import routeplanner.backend.model.Node;

/*
 * Program flow of next node calculation
 */
public class NextNodeApp {
	
	// Prepare data for calculation
	public void prepare(Node[] nodes) {
		
		_nextNode = new NextNode();
		
		_nextNode.prepare(nodes);
	}
	
	// Run next node calculation
	public void run(Main.Parameters param, Node[] nodes, Logger logger) throws IOException, Main.FatalFailure {
		
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
			
				throw new Main.FatalFailure(Main.Code.BAD_REQUEST, "Bad request provided");
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
				distance = Double.NaN; startTime = 0; endTime = 0;
				break;
			}
			
			logger.info("Nearest node found in " + (double)(endTime - startTime) / 1000000 + " ms" + System.lineSeparator());
			
			Node[] result = _nextNode.getResult(nodes);

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
	
	
	// Next node algorithm implementation
	private NextNode _nextNode;
}
