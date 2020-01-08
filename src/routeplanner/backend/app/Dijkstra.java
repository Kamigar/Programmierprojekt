package routeplanner.backend.app;

import java.io.IOException;

import routeplanner.backend.model.*;

/*
 * Implementation of the Dijkstra algorithm
 */
public class Dijkstra {
	
	// Calculate shortest paths from start to all other nodes
	public static Node[] calculate(Node[] nodes, Node start, Logger logger) throws IOException {
		
		logger.info(System.lineSeparator() + "Prepare data for calculation");

		Queue nodeList = new Queue();

		int finishedIndex = 0;
		Node[] finishedNodes = new Node[nodes.length];
		
		// Initialize start node with distance 0
		start.setDistance(0);
		start.setEntry(nodeList.insert(start, 0));

		logger.info("Calculate distances");
		
		while (!nodeList.isEmpty()) {
			
			// Remove next node from queue
			Node current = nodeList.poll().node();
			current.setEntry(null);

			finishedNodes[finishedIndex] = current;
			finishedIndex++;
			
			for (Edge edge : current.edges()) {
				
				// Check if there is an unknown (shorter) path
				Node neighbour = edge.trg();
				
				int newDistance = current.distance() + edge.cost();
				int oldDistance = neighbour.distance();
				if (oldDistance > newDistance) {

					neighbour.setDistance(newDistance);
					neighbour.setPrevious(current);
					
					if (neighbour.entry() == null) {
						// Add node to queue
						neighbour.setEntry(nodeList.insert(neighbour, neighbour.distance()));

					} else {
						// Update (decrease) distance
						nodeList.decreaseKey(neighbour.entry(), neighbour.distance());
					}
				}
			}
		}
		
		logger.info("Calculation finished");
		
		return finishedNodes;
	}
}
