package routeplanner.backend.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
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
  
  private static final int DEFAULT_TEST_COUNT = 100;
  private static final String DEFAULT_TEST_MAP = "/toy.fmi";
  private static final int MAX_RESULTS = 100;
	
	@BeforeAll
	public static void initialize() throws IOException, FatalFailure {
	  
		Parameters param = new Parameters();

		param.isTolerant = false;
		
		param.structureIn = new BufferedReader(new InputStreamReader(System.getProperty("map") == null
		    ? KDTreeVerification.class.getResourceAsStream(DEFAULT_TEST_MAP)
		    : new FileInputStream(System.getProperty("map"))));
		
		_logger = new Logger(Logger.Level.INFO, new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8")));
		
		App app = new App();
		_nodes = app.readGraph(param, _logger);
		
		_nextNode = new NextNode();
		_nextNode.prepare(_nodes);
		
		_nnfResults = new Node[MAX_RESULTS];
		_nniResults = new Node[MAX_RESULTS];
	}

	@Test
	public void testRandomNodes() throws IOException {
	  
	  // Number of random nodes to check
	  int cnt = System.getProperty("cnt") == null ? DEFAULT_TEST_COUNT : Integer.parseUnsignedInt(System.getProperty("cnt"));
		
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

			int nnfcnt = _nextNode.getResult(_nnfResults, _nodes);


			// Calculate iteratively
			
			startTime = System.nanoTime();

			double di = _nextNode.findNextIterative(x, y);
			
			endTime = System.nanoTime();
			
			nniTimes[i] = endTime - startTime;
			
			int nnicnt = _nextNode.getResult(_nniResults, _nodes);
			
			
			// Assert results
			
			assertEquals(di, df);

			assertEquals(nnicnt, nnfcnt);
			
			if (nnicnt > MAX_RESULTS) {
			  
			  _logger.warning("Too much nodes with the same distance");

			} else {
			  
			  for (int j = 0; j < nnicnt; j++)
			    assertEquals(_nniResults[j], _nnfResults[j]);
			}
		}
		

		// Output time results

		double nnfSum = 0, nniSum = 0;

		StringBuilder nnfMsg = new StringBuilder(System.lineSeparator() + "Calculation times NNF:");
		StringBuilder nniMsg = new StringBuilder(System.lineSeparator() + "Calculation times NNI: ");

		for (int i = 0; i < cnt; i++) {
		  
		  double nnfTime = (double)nnfTimes[i] / 1000000;
		  double nniTime = (double)nniTimes[i] / 1000000;

		  nnfSum += nnfTime;
		  nniSum += nniTime;
		  
		  nnfMsg.append("  " + nnfTime + " ms");
		  nniMsg.append("  " + nniTime + " ms");
		}
		
		_logger.info(nnfMsg.toString());
		_logger.info("Average: " + nnfSum / cnt + " ms");
		
		_logger.info(nniMsg.toString());
		_logger.info("Average: " + nniSum / cnt + " ms");
	}
	

	private static NextNode _nextNode;
	
	private static Node[] _nodes;

	private static Node[] _nnfResults;
	private static Node[] _nniResults;

	private static Logger _logger;
}
