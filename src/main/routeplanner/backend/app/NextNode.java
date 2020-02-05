package routeplanner.backend.app;

import routeplanner.backend.model.IntStack;
import routeplanner.backend.model.Node;

/*
 * Implementation of next node algorithms
 */
public class NextNode {
	
	/*
	 * Node of k-d tree
	 */
	private static class TreeNode {
		
		public double value;

		public TreeNode right;
		public TreeNode left;
	}
	
	// Get the calculated nearest nodes
	public Node[] getResult(Node[] nodes) {
		
		Node[] r = new Node[IntStack.size(_stack)];
		for (int i = 0; i < r.length; i++) {
			
			r[i] = nodes[IntStack.get(_stack, i)];
		}
		return r;
	}
	
	// Return the minimum bounding box of the graph
	public double[] minimumBoundingBox() {

		return _bounds;
	}
	
	// Find next nodes to point (longitude, latitude) iteratively
	public double findNextIterative(double longitude, double latitude) {
		
		IntStack.clear(_stack);
		
		double minDistance = Double.POSITIVE_INFINITY;
		
		for (int i = 0; i < _data.length / 2; i++) {
			
			double d = distance(longitude, latitude, getProperty(0, _data, i), getProperty(1, _data, i));
			
			if (d <= minDistance) {
				
				if (d != minDistance) {

					minDistance = d;
					IntStack.clear(_stack);
				}
				IntStack.push(_stack, i);
			}
		}
		return minDistance;
	}

	// Find next nodes to point (longitude, latitude) in k-d tree
	public double findNext(double longitude, double latitude) {
		
		IntStack.clear(_stack);

		return findNext(_stack, 0, _data, _root, longitude, latitude);
	}
	
	// Recursively search next node in (sub)tree
	private static double findNext(int[] results, int property, double[] data, TreeNode tree, double current, double other) {
		
		if (tree.left == null) {
			// Is leaf node
			int index = (int)tree.value;
			IntStack.push(results, index);
			return distance(current, other, getProperty(property, data, index), getProperty(1 - property, data, index));
		}
		
		if (tree.right == null) {
			// Search in left subtree
			return findNext(results, 1 - property, data, tree.left, other, current);
		}
		
		// Decide whether to search first in left or right subtree
		TreeNode nextChild, otherChild;
		double threshold = tree.value - current;
		if (threshold < 0) {
			
			threshold = -threshold;
			
			nextChild = tree.right;
			otherChild = tree.left;

		} else {
			
			nextChild = tree.left;
			otherChild = tree.right;
		}


		int stackSize = IntStack.size(results);

		double distance = findNext(results, 1 - property, data, nextChild, other, current);
		
		if (distance >= threshold) {

			// Next node possibly in other subtree

			int newSize = IntStack.size(results);

			double e = findNext(results, 1 - property, data, otherChild, other, current);
			
			if (e <= distance) {
				
				if (e != distance) {

					// Remove old results from stack
					IntStack.remove(results, IntStack.size(results) - newSize, newSize - stackSize);
					distance = e;
				}

			} else {

				// Remove last results from stack
				IntStack.remove(results, IntStack.size(results) - newSize);
			}
		}
		return distance;
	}

	// Create k-d tree and initialize calculation data
	public void prepare(Node[] nodes) {
		
		// Copy nodes to new structure and find minimum bounding box

		_bounds = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		_data = new double[nodes.length * 2];
		int[] indices = new int[nodes.length];
		
		for (int i = 0; i < nodes.length; i++) {
			
		  indices[i] = i;

			_data[i * 2] = nodes[i].longitude();
			_data[i * 2 + 1] = nodes[i].latitude();
			
			if (nodes[i].longitude() < _bounds[0])
				_bounds[0] = nodes[i].longitude();
			if (nodes[i].longitude() > _bounds[2])
				_bounds[2] = nodes[i].longitude();
			if (nodes[i].latitude() < _bounds[1])
				_bounds[1] = nodes[i].latitude();
			if (nodes[i].latitude() > _bounds[3])
				_bounds[3] = nodes[i].latitude();
		}

		// Create k-d tree
		_root = createTree(0, _data, indices, 0, nodes.length - 1);
		
		// Create result stack
		_stack = IntStack.create(nodes.length);

		// Recreate node structure so the index of a node matches its ID
		for (int i = 0; i < nodes.length; i++) {
		  _data[i * 2] = nodes[i].longitude();
		  _data[i * 2 + 1] = nodes[i].latitude();
		}
	}
	
	// Create k-d (sub)tree recursively
	private static TreeNode createTree(int property, double[] nodes, int[] indices, int left, int right) {
		
		if (left >= right) {
			
			if (left != right)
				return null;

			// Create leaf
			TreeNode t = new TreeNode();
			t.value = indices[left];
			return t;	
		}
		
		// Expected position of pivot element
		int pos = (right - left) / 2;

		TreeNode t = new TreeNode();
		
		// Find median (element on position 'pos')
		t.value = getPosition(property, nodes, indices, left, right, pos);
		
		// Create subtrees
		t.left = createTree(1 - property, nodes, indices, left, left + pos);
		t.right = createTree(1 - property, nodes, indices, left + pos + 1, right);
		
		return t;
	}
	
	// Find 'pos'-th smallest element (position 'pos' in sorted array)
	private static double getPosition(int property, double[] nodes, int[] indices, int left, int right, int pos) {
		
		// Partition elements into two parts
		int pivot = partition(property, nodes, indices, left, right);
		int pivotPos = pivot - left;
		
		// Continue search in right or left part
		if (pivotPos > pos)
			return getPosition(property, nodes, indices, left, pivot - 1, pos);
		if (pivotPos < pos)
			return getPosition(property, nodes, indices, pivot + 1, right, pos - pivotPos - 1);
			
		// Return if pivot is on position 'pos'
		return getProperty(property, nodes, pivot);
	}
	
	// Partition elements into two parts (values less or equal than pivot left, greater or equal pivot right)
	private static int partition(int property, double[] nodes, int[] indices, int left, int right) {
		
		double pivot = getProperty(property, nodes, right);
		
		int i = left;
		for (int j = left; j < right; j++) {
			
			if (getProperty(property, nodes, j) < pivot) {

				swap(nodes, indices, i, j);
				i++;
			}
		}
		swap(nodes, indices, i, right);
		return i;
	}


	// Calculate Euclidean distance between two points
	private static double distance(double ax, double ay, double bx, double by) {
		
		double dx = ax - bx;
		double dy = ay - by;
		
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	// Swap i and j in node and index array
	private static void swap(double[] nodes, int[] indices, int i, int j) {
		
	  double longitude = nodes[i * 2];
	  nodes[i * 2] = nodes[j * 2];
	  nodes[j * 2] = longitude;
	  double latitude = nodes[i * 2 + 1];
	  nodes[i * 2 + 1] = nodes[j * 2 + 1];
	  nodes[j * 2 + 1] = latitude;
	  int index = indices[i];
	  indices[i] = indices[j];
	  indices[j] = index;
	}
	
	// Get property of node on given index
	private static double getProperty(int property, double[] data, int index) {
		
		return data[index * 2 + property];
	}
	
	
	// Root of the k-d tree
	private TreeNode _root;
	
	// Structure to store nodes with their longitude & latitude
	private double[] _data;
	
	// Stack to store the calculated results
	private int[] _stack;
	
	// Minimum bounding box
	private double[] _bounds;
}
