package routeplanner.backend.app;

import java.io.IOException;
import java.util.Vector;

import routeplanner.backend.model.*;

public class Dijkstra {
	
	public static class DijkstraStructure {
		public DijkstraNode[] nodes;
		public Vector<DijkstraNode> ordered;
	}
	
	public static DijkstraStructure calculate(DijkstraNode[] nodes, DijkstraNode start, Logger logger) throws IOException {
		
		logger.info(System.lineSeparator() + "Prepare data for calculation");

		Queue<DijkstraNode> nodeList = new Queue<DijkstraNode>(nodes.length);

		Vector<DijkstraNode> finishedNodes = new Vector<DijkstraNode>(nodes.length);
		
		start.setDistance(0);
		start.setEntry(nodeList.insert(start, 0));

		logger.info("Calculate distances");
		
		while (!nodeList.isEmpty()) {
			
			DijkstraNode current = nodeList.poll();
			current.setEntry(null);

			finishedNodes.add(current);
			
			for (DijkstraEdge edge : current.edges()) {
				
				DijkstraNode neighbour = edge.trg();
				
				double newDistance = current.distance() + edge.cost();
				double oldDistance = neighbour.distance();
				if (oldDistance > newDistance) {

					neighbour.setDistance(newDistance);
					neighbour.setPrevious(current);
					
					if (neighbour.entry() == null) {
						
						neighbour.setEntry(nodeList.insert(neighbour, neighbour.distance()));

					} else {
						
						nodeList.decreaseKey(neighbour.entry(), neighbour.distance());
					}
				}
			}
		}
		
		logger.info("Calculation finished");
		
		DijkstraStructure r = new DijkstraStructure();
		r.nodes = nodes;
		r.ordered = finishedNodes;
		return r;
	}
}
