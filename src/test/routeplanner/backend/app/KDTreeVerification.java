package routeplanner.backend.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import routeplanner.backend.app.Logger.Level;
import routeplanner.backend.app.Main.FatalFailure;
import routeplanner.backend.model.Node;

public class KDTreeVerification {
	
	@BeforeAll
	public static void initialize() throws IOException, FatalFailure {
		
		Main.Parameters param = new Main.Parameters();

		param.isTolerant = false;
		param.structureIn = new BufferedReader(new InputStreamReader(KDTreeVerification.class.getResourceAsStream("/toy.fmi"), "UTF-8"));
		
		_logger = new Logger(Level.WARNING, new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8")));
		
		_nodes = ReadGraphApp.run(param, _logger);
		
		_nextNode = new NextNode();
		_nextNode.prepare(_nodes);
	}

	@Test
	public void testRandomNodes() {
		
		double[] bounds = _nextNode.minimumBoundingBox();
		
		Random rand = new Random();
		
		for (int i = 0; i < 100; i++) {
			
			double x = bounds[0] + (bounds[2] - bounds[0]) * rand.nextDouble();
			double y = bounds[1] + (bounds[3] - bounds[1]) * rand.nextDouble();
			
			double df = _nextNode.findNext(x, y);
			Node[] rf = _nextNode.getResult(_nodes);
			
			double di = _nextNode.findNextIterative(x, y);
			Node[] ri = _nextNode.getResult(_nodes);
			
			assertEquals(df, di);

			assertEquals(rf.length, ri.length);
			
			for (int j = 0; j < rf.length; j++)
				assertEquals(rf[j].id(), ri[j].id());
		}
	}
	

	private static NextNode _nextNode;
	
	private static Node[] _nodes;
	
	private static Logger _logger;
}
