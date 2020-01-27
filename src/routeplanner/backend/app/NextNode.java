package routeplanner.backend.app;

import routeplanner.backend.model.Node;

/*
 * Implementation of next node algorithms
 */
public class NextNode {
	
	// Calculate next node iteratively
	public static Node calculateIterative(Node[] nodes, Node start) {
		
		Node min = null;
		double minDistance = Double.POSITIVE_INFINITY;

		for (Node node : nodes) {
			
			double d = distance(node, start);

			if (d < minDistance && node != start) {

				min = node;
				minDistance = d;
			}
		}
		return min;
	}
	
	// Calculate Euclidean distance between two nodes
	private static double distance(Node a, Node b) {
		
		double dx = a.longitude() - b.longitude();
		double dy = a.latitude() - b.latitude();
		
		return Math.sqrt(dx * dx + dy * dy);
	}
}
