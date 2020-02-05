package routeplanner.backend.app;

import routeplanner.backend.model.*;

/*
 * Implementation of the Dijkstra algorithm
 */
public class Dijkstra {
	
	// Calculate shortest paths from start to all other nodes
	public void calculate(int start) {
		
		// Initialize start node with distance 0
		setDistance(_data[start], 0);
		IntHeap.insert(_queue, start, 0);
		
		while (!IntHeap.isEmpty(_queue)) {
			
			// Remove next node from queue
			int currentId = IntHeap.poll(_queue);
			int[] current = _data[currentId];
			int currentDistance = distance(current);

			// Skip node if already finished
			if (currentDistance < 0)
				continue;

			// Mark node finished
			setDistance(current, -currentDistance);


			for (int i = 0; i < edgeCount(current); i++) {
				
				// Check if there is an unknown (shorter) path
				int neighbor = edgeTarget(current, i);
				int newDistance = edgeCost(current, i) + currentDistance;
				
				if (newDistance < distance(_data[neighbor])) {
					
					setPrevious(_data[neighbor], newDistance, currentId);
					
					// Add node with new distance to queue
					IntHeap.insert(_queue, neighbor, newDistance);
				}
			}
		}
	}

	// Get the calculated distances
	public void getResult(Node[] nodes) {
		
		for (int i = 0; i < _data.length; i++) {
			
			int d = -distance(_data[i]);
			int p = previous(_data[i]);
			
			if (d < 0)
				nodes[i].setDistance(-1);
			else
				nodes[i].setDistance(d);
			
			if (p == -1)
				nodes[i].setPrevious(null);
			else
				nodes[i].setPrevious(nodes[p]);
		}
	}
	
	// Prepare data for calculation
	public void prepare(Node[] nodes) {
		
		_queue = IntHeap.create(nodes.length);

		_data = new int[nodes.length][];
		
		for (int i = 0; i < nodes.length; i++) {
			
			Edge[] edges = nodes[i].edges();
			
			int[] n = new int[edges.length * 2 + 2];
			
			for (int j = 0; j < edges.length; j++) {
				
				n[j * 2 + 2] = edges[j].cost();
				n[j * 2 + 3] = edges[j].trg().id();
			}
			
			_data[i] = n;
		}
		reset();
	}
	
	// Reset distances and path for new calculation
	public void reset() {
		
		for (int i = 0; i < _data.length; i++)
			setPrevious(_data[i], Integer.MAX_VALUE, -1);
	}
	
	private static int edgeCount(int[] node) {
		return (node.length - 2) / 2;
	}
	
	private static int edgeTarget(int[] source, int index) {
		return source[index * 2 + 3];
	}
	
	private static int edgeCost(int[] source, int index) {
		return source[index * 2 + 2];
	}
	
	private static int distance(int[] node) {
		return node[0];
	}
	
	private static void setDistance(int[] node, int distance) {
		node[0] = distance;
	}

	private static int previous(int[] node) {
		return node[1];
	}
	
	private static void setPrevious(int[] node, int distance, int previous) {
		node[0] = distance;
		node[1] = previous;
	}
	

	// Data for calculation
	private int[][] _data;
	
	// Priority queue for non-finished nodes
	int[] _queue;
}
