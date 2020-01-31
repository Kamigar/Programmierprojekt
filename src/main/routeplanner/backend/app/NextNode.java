package routeplanner.backend.app;

import java.util.Arrays;
import java.util.LinkedList;

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
		
		LinkedList<Node> result = new LinkedList<Node>();
		for (int i = 0; i < IntStack.size(_stack); i++) {
			
			double[] n = _data[IntStack.get(_stack, i)];
			
			for (int j = 2; j < n.length; j++)
				result.add(nodes[(int)n[j]]);
		}
		return result.toArray(new Node[0]);
	}
	
	// Return the minimum bounding box of the graph
	public double[] minimumBoundingBox() {

		return _bounds;
	}
	
	// Find next nodes to point (longitude, latitude) iteratively
	public double findNextIterative(double longitude, double latitude) {
		
		IntStack.clear(_stack);
		
		double minDistance = Double.POSITIVE_INFINITY;
		
		for (int i = 0; i < _data.length; i++) {
			
			double d = distance(longitude, latitude, _data[i][0], _data[i][1]);
			
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
	private static double findNext(int[] results, int property, double[][] data, TreeNode tree, double current, double other) {
		
		if (tree.left == null) {
			// Is leaf node
			int index = (int)tree.value;
			IntStack.push(results, index);
			return distance(current, other, data[index][property], data[index][1 - property]);
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
		
		Node[] sorted = Arrays.copyOf(nodes, nodes.length);
		
		// Sort nodes by position
		Arrays.sort(sorted, (x, y) -> {
			
			if (x.longitude() == y.longitude()) {
				if (x.latitude() == y.latitude())
					return 0;

				return x.latitude() < y.latitude() ? -1 : 1;
			}
			return x.longitude() < y.longitude() ? -1 : 1;
		});
		
		_bounds = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		_data = new double[sorted.length][];
		int dataSize = 0;
		
		// Find and merge duplicates into a structure with unique positions
		for (int i = 0; i < sorted.length;) {
			
			double x = sorted[i].longitude(), y = sorted[i].latitude();
			
			int j = i + 1;
			while (j < sorted.length && sorted[j].longitude() == x && sorted[j].latitude() == y)
				j++;
			
			double[] location = new double[j - i + 2];
			location[0] = x;
			location[1] = y;

			for (int k = i; k < j; k++)
				location[k - i + 2] = sorted[k].id();

			if (x < _bounds[0])
				_bounds[0] = x;
			if (y < _bounds[1])
				_bounds[1] = y;
			if (x > _bounds[2])
				_bounds[2] = x;
			if (y > _bounds[3])
				_bounds[3] = y;

			_data[dataSize] = location;
			dataSize++;
			
			i = j;
		}
		
		// Shrink data to relevant size
		_data = Arrays.copyOf(_data, dataSize);
		

		// Create k-d tree
		_root = createTree(0, _data, 0, _data.length - 1);

		// Create result stack
		_stack = IntStack.create(dataSize);
	}
	
	// Create k-d (sub)tree recursively
	private static TreeNode createTree(int property, double[][] nodes, int left, int right) {
		
		if (left >= right) {
			
			if (left != right)
				return null;

			// Create leaf
			TreeNode t = new TreeNode();
			t.value = left;
			return t;	
		}
		
		// Expected position of pivot element
		int pos = (right - left) / 2;

		TreeNode t = new TreeNode();
		
		// Find median (element on position 'pos')
		t.value = getPosition(property, nodes, left, right, pos);
		
		// Create subtrees
		t.left = createTree(1 - property, nodes, left, left + pos);
		t.right = createTree(1 - property, nodes, left + pos + 1, right);
		
		return t;
	}
	
	// Find 'pos'-th smallest element (position 'pos' in sorted array)
	private static double getPosition(int property, double[][] nodes, int left, int right, int pos) {
		
		// Partition elements into two parts (use value in the middle as pivot)
		int pivot = partition(property, nodes, left, right, left + pos);
		int pivotPos = pivot - left;
		
		// Continue search in right or left part
		if (pivotPos > pos)
			return getPosition(property, nodes, left, pivot - 1, pos);
		if (pivotPos < pos)
			return getPosition(property, nodes, pivot + 1, right, pos - pivotPos - 1);
			
		// Return if pivot is on position 'pos'
		return nodes[pivot][property];
	}
	
	// Partition elements into two parts (values less or equal than pivot left, greater or equal pivot right)
	private static int partition(int property, double[][] nodes, int left, int right, int pivotIndex) {
		
		double pivot = nodes[pivotIndex][property];
		swap(nodes, pivotIndex, right);
		
		int i = left;
		for (int j = left; j < right; j++) {
			
			if (nodes[j][property] < pivot) {

				swap(nodes, i, j);
				i++;
			}
		}
		swap(nodes, i, right);
		return i;
	}
	
	// Swap node i and j in array
	private static void swap(double[][] nodes, int i, int j) {
		
		double[] t = nodes[i];
		nodes[i] = nodes[j];
		nodes[j] = t;
	}

	// Calculate Euclidean distance between two points
	private static double distance(double ax, double ay, double bx, double by) {
		
		double dx = ax - bx;
		double dy = ay - by;
		
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	
	// Root of the k-d tree
	private TreeNode _root;
	
	// Structure to store nodes with unique position
	private double[][] _data;
	
	// Stack to store the calculated results
	private int[] _stack;
	
	// Minimum bounding box
	private double[] _bounds;
}
