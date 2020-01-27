package routeplanner.backend.app;

import java.io.IOException;

import routeplanner.backend.model.Node;

/*
 * Program flow of next node calculation
 */
public class NextNodeApp {
	
	// Prepare data for calculation
	public void prepare(Node[] nodes) {
	}

	// Run next node calculation
	public void run(Main.Parameters param, Node[] nodes, Logger logger) throws IOException, Main.FatalFailure {
		
		logger.info(System.lineSeparator()
				+ "Input format: [srcID] e.g. 18445" + System.lineSeparator()
				+ "  use multiple lines for multiple requests" + System.lineSeparator()
				+ "  end input with <EOF> (CTRL+D)" + System.lineSeparator());

		FileScanner.StringPosition pos = new FileScanner.StringPosition();
		while (true) {

			int srcId;
			try {
				
				// Read source request
				srcId = FileScanner.readId(param.requestIn, pos, nodes.length, logger, param.isTolerant);
				
			} catch (FileScanner.BadRequestException ex) {
				
				logger.error("Bad request provided");
				logger.info(ex.getMessage());
			
				throw new Main.FatalFailure(Main.Code.BAD_REQUEST, "Bad request provided");
			}	
			
			// Note: No 'while (srcId != -1) {...}' loop used
			//   to prevent code duplication of 'FileScanner.readId(...)'
			if (srcId == -1)
				break;	
			

			long startTime = System.nanoTime();

			// Search next node
			Node next = NextNode.calculateIterative(nodes, nodes[srcId]);
			
			long endTime = System.nanoTime();
			
			logger.info("Next node found in " + (double)(endTime - startTime) / 1000000000 + " seconds" + System.lineSeparator());
			

			// Output result
			
			param.requestOut.write("" + next.id());
			param.requestOut.newLine();
			
			logger.info("Next node: " + next.id() + System.lineSeparator());
		}
	}
}
