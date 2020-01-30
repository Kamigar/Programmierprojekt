package routeplanner.backend.app;

import java.io.IOException;

import routeplanner.backend.model.Node;

/*
 * Program flow of Dijkstra algorithm
 */
public class DijkstraApp {
	
	// Prepare data for calculation
	public void prepare(Node[] nodes) {
		
			_dijkstra = new Dijkstra();

			_dijkstra.prepare(nodes);
	}

	// Run Dijkstra calculation
	public void run(Main.Parameters param, Node[] nodes, Logger logger) throws IOException, Main.FatalFailure {
		
		long startTime, endTime;
		
		if (param.start != -1) {

			// Calculate distances from one starting point
			if (param.start < 0 || param.start >= nodes.length) {
				
				logger.error("nodeID of starting point out of range");
				
				throw new Main.FatalFailure(Main.Code.BAD_PARAMETER, "nodeID out of range");
			}
			

			logger.info(System.lineSeparator() + "Start calculation");

			startTime = System.nanoTime();

			// Calculate distances
			_dijkstra.calculate(param.start);

			endTime = System.nanoTime();	


			logger.info("Path calculated in " + (double)(endTime - startTime) / 1000000000 + " seconds" + System.lineSeparator()
					+ System.lineSeparator() + "Distances: [trgID] [distance]");
			
			_dijkstra.getResult(nodes);

			if (param.mode == Main.Mode.OTA) {

				// Output result
				for (Node node : nodes) {
					
					String line = "" + node.id() + " " + node.distance();
					
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
						trgId = FileScanner.readId(param.requestIn, pos, nodes.length, logger, param.isTolerant);

					} catch (FileScanner.BadRequestException ex) {
						
						logger.error("Bad request provided");
						logger.info(ex.getMessage());
						
						throw new Main.FatalFailure(Main.Code.BAD_REQUEST, "Bad request provided");
					}

					// Note: No 'while (trgId != -1) {...}' loop used
					//   to prevent code duplication of 'FileScanner.readId(...)'
					if (trgId == -1)
						break;
					
					// Output result

					Node dst = nodes[trgId];

					param.requestOut.write("" + dst.distance());
					param.requestOut.newLine();

					logger.info("Distance: " + dst.distance() + System.lineSeparator());
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
				
					throw new Main.FatalFailure(Main.Code.BAD_REQUEST, "Bad request provided");
				}	
				
				// Note: No 'while (request != null) {...}' loop used
				//   to prevent code duplication of 'FileScanner.readRequest(...)'
				if (request == null)
					break;

				if (request[0] != lastRequest) {
					
					lastRequest = request[0];
					
					_dijkstra.reset();

					logger.info("Start calculation");

					startTime = System.nanoTime();
					
					// Calculate distances
					_dijkstra.calculate(lastRequest);
					
					endTime = System.nanoTime();	


					logger.info("Path calculated in " + (double)(endTime - startTime) / 1000000000 + " seconds" + System.lineSeparator());	
					
					_dijkstra.getResult(nodes);
				}
				
				// Output result

				Node dst = nodes[request[1]];

				param.requestOut.write("" + dst.distance());
				param.requestOut.newLine();

				logger.info("Distance: " + dst.distance() + System.lineSeparator());
			}	
		}	
	}
	

	// Dijkstra algorithm implementation
	private Dijkstra _dijkstra;
}
