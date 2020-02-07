package routeplanner.backend.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import routeplanner.backend.app.App.FatalFailure;
import routeplanner.backend.app.App.Parameters;
import routeplanner.backend.model.Node;

public class KDTreeVerification {
	
	@BeforeAll
	public static void initialize() throws IOException, FatalFailure {
		
		Parameters param = new Parameters();

		param.isTolerant = false;
		param.structureIn = new BufferedReader(new InputStreamReader(KDTreeVerification.class.getResourceAsStream("/toy.fmi"), "UTF-8"));
		
		_logger = new Logger(Logger.Level.INFO, new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8")));
		
		App app = new App();
		_nodes = app.readGraph(param, _logger);
		
		_nextNode = new NextNode();
		_nextNode.prepare(_nodes);
	}

	@Test
	public void testRandomNodes() throws IOException {
	  
	  // Number of random nodes to check
	  int cnt = 100;
		
		double[] bounds = _nextNode.minimumBoundingBox();
		
		Random rand = new Random();
		
		long[] nnfTimes = new long[cnt], nniTimes = new long[cnt];
		
		for (int i = 0; i < cnt; i++) {
		  
		  // Get random test location
			double x = bounds[0] + (bounds[2] - bounds[0]) * rand.nextDouble();
			double y = bounds[1] + (bounds[3] - bounds[1]) * rand.nextDouble();
			

			// Calculate with k-d tree

			long startTime = System.nanoTime();

			double df = _nextNode.findNext(x, y);

			long endTime = System.nanoTime();
			
			nnfTimes[i] = endTime - startTime;

			Node[] rf = _nextNode.getResult(_nodes);
			

			// Calculate iteratively
			
			startTime = System.nanoTime();

			double di = _nextNode.findNextIterative(x, y);
			
			endTime = System.nanoTime();
			
			nniTimes[i] = endTime - startTime;
			
			Node[] ri = _nextNode.getResult(_nodes);
			
			
			// Assert results
			
			assertEquals(df, di);

			assertEquals(rf.length, ri.length);
			
			for (int j = 0; j < rf.length; j++)
				assertEquals(rf[j].id(), ri[j].id());
		}
		

		// Output time results

		double nnfAvg = 0, nniAvg = 0;

		StringBuilder nnfMsg = new StringBuilder(System.lineSeparator() + "Calculation times NNF:");
		StringBuilder nniMsg = new StringBuilder(System.lineSeparator() + "Calculation times NNI: ");

		for (int i = 0; i < cnt; i++) {
		  
		  nnfAvg += (double)nnfTimes[i] / (cnt * 1000000);
		  nniAvg += (double)nniTimes[i] / (cnt * 1000000);
		  
		  nnfMsg.append("  " + (double)nnfTimes[i] / 1000000 + " ms");
		  nniMsg.append("  " + (double)nniTimes[i] / 1000000 + " ms");
		}
		
		_logger.info(nnfMsg.toString());
		_logger.info("Average: " + nnfAvg + " ms");
		
		_logger.info(nniMsg.toString());
		_logger.info("Average: " + nniAvg + " ms");
	}
	

	private static NextNode _nextNode;
	
	private static Node[] _nodes;
	
	private static Logger _logger;
}
