package routeplanner.backend.app;

import java.io.IOException;

import routeplanner.backend.model.*;

public class Dijkstra {
	
	public static Node[] calculate(Node[] nodes, Node start, Logger logger) throws IOException {
		
		logger.info(System.lineSeparator() + "Prepare data for calculation");

		Queue nodeList = new Queue(nodes.length);

		int finishedIndex = 0;
		Node[] finishedNodes = new Node[nodes.length];
		
		start.setDistance(0);
		start.setEntry(nodeList.insert(start, 0));

		logger.info("Calculate distances");
		
		while (!nodeList.isEmpty()) {
			
			Node current = nodeList.poll();
			current.setEntry(null);

			finishedNodes[finishedIndex] = current;
			finishedIndex++;
			
			for (Edge edge : current.edges()) {
				
				Node neighbour = edge.trg();
				
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
		
		return finishedNodes;
	}
}
