package routeplanner.backend.app;

import routeplanner.backend.model.*;

/*
 * Implementation of the Dijkstra algorithm
 */
public class Dijkstra {
	
	// Calculate shortest paths from start to all other nodes
	public void calculate(int start) {
		
		// Initialize start node with distance 0
		setDistance(start, 0);
		_queue.insert(start, 0);
		
		while (!_queue.isEmpty()) {
			
			// Remove next node from queue
			int[] current = _data[_queue.poll()];
			int currentDistance = current[0];

			if (currentDistance < 0)
				continue;

			current[0] = -currentDistance;

			for (int i = 0; i < edgeCount(current); i++) {
				
				// Check if there is an unknown (shorter) path
				int neighbour = edgeTarget(current, i);
				int newDistance = edgeCost(current, i) + currentDistance;
				
				if (newDistance < distance(neighbour)) {
					
					setDistance(neighbour, newDistance);
					
					// Add node with new distance to queue
					_queue.insert(neighbour, newDistance);
				}
			}
		}
	}

	// Get the calculated distances
	public void getResult(Node[] nodes) {
		
		for (int i = 0; i < _data.length; i++) {
			
			int d = -distance(i);
			if (d < 0)
				d = -1;
			
			nodes[i].setDistance(d);
		}
	}
	
	// Prepare data for calculation
	public void prepare(Node[] nodes) {
		
		_queue = new BinaryHeap(nodes.length);

		_data = new int[nodes.length][];
		
		for (int i = 0; i < nodes.length; i++) {
			
			Edge[] edges = nodes[i].edges();
			
			int[] n = new int[edges.length * 2 + 1];
			
			for (int j = 0; j < edges.length; j++) {
				
				n[j * 2 + 1] = edges[j].cost();
				n[j * 2 + 2] = edges[j].trg().id();
			}
			
			_data[i] = n;
		}
		reset();
	}
	
	// Reset distances for new calculation
	public void reset() {
		
		for (int i = 0; i < _data.length; i++)
			setDistance(i, Integer.MAX_VALUE);
	}
	
	private int edgeCount(int[] node) {
		return (node.length - 1) / 2;
	}
	
	private int edgeTarget(int[] source, int index) {
		return source[index * 2 + 2];
	}
	
	private int edgeCost(int[] source, int index) {
		return source[index * 2 + 1];
	}
	
	private int distance(int index) {
		return _data[index][0];
	}

	private void setDistance(int index, int value) {
		_data[index][0] = value;
	}
	

	// Data for calculation
	private int[][] _data = null;
	
	// Priority queue for non-finished nodes
	BinaryHeap _queue = null;
}
