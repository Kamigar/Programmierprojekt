package routeplanner.backend.app;

import java.io.IOException;

import routeplanner.backend.model.Node;

/*
 * Program flow of graph input processing
 */
public class ReadGraphApp {

	// Read graph
	public static Node[] run(Main.Parameters param, Logger logger) throws IOException, Main.FatalFailure {
		
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
			
			throw new Main.FatalFailure(Main.Code.BAD_HEADER, "Bad header provided");

		} catch (FileScanner.BadNodeException ex) {
			
			logger.error("Bad node provided");
			logger.info(ex.getMessage());
			
			throw new Main.FatalFailure(Main.Code.BAD_NODE, "Bad node provided");

		} catch (FileScanner.BadEdgeException ex) {
			
			logger.error("Bad edge provided");
			logger.info(ex.getMessage());
			
			throw new Main.FatalFailure(Main.Code.BAD_EDGE, "Bad edge provided");
		}

		logger.info(System.lineSeparator() + nodes.length + " nodes read in "
				+ (double)(endTime - startTime) / 1000000000 + " seconds");
		
		return nodes;
	}
}
