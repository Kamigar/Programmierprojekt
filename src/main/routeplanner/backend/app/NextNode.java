package routeplanner.backend.app;

import routeplanner.backend.model.IntStack;
import routeplanner.backend.model.Node;

/*
 * Implementation of next node algorithms
 */
public class NextNode {
	
	// Get the calculated nearest nodes (in preallocated array)
  public int getResult(Node[] result, Node[] nodes) {
    
    int stackSize = IntStack.size(_stack);
    int end = Math.min(result.length, stackSize);
    for (int i = 0; i < end; i++) {
      
      result[i] = nodes[IntStack.get(_stack, i)];
    }
    return stackSize;
  }

	// Get the calculated nearest nodes (allocate new array)
	public Node[] getResult(Node[] nodes) {
		
		Node[] result = new Node[IntStack.size(_stack)];
		getResult(result, nodes);
		return result;
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

		return findNext(_stack, 0, _data, _tree, 0, longitude, latitude);
	}
	
	// Recursively search next node in (sub)tree
	private static double findNext(int[] results, int property, double[] data, double[] tree, int treeIndex, double current, double other) {
	  
	  if (treeIndex * 2 + 1 >= tree.length) {
			// Is leaf node
	    int index = (int)tree[treeIndex];
	    IntStack.push(results, index);
	    return distance(current, other, getProperty(property, data, index), getProperty(1 - property, data, index));
	  }

		// Decide whether to search first in left or right subtree
	  int nextChild, otherChild;
	  double threshold = tree[treeIndex] - current;
	  if (threshold < 0) {
	    
	    threshold = -threshold;
	    nextChild = treeIndex * 2 + 2;
	    otherChild = treeIndex * 2 + 1;

	  } else {
	    
	    nextChild = treeIndex * 2 + 1;
	    otherChild = treeIndex * 2 + 2;
	  }
	  
	  int stackSize = IntStack.size(results);
	  
	  double distance = findNext(results, 1 - property, data, tree, nextChild, other, current);
	  
	  if (distance >= threshold) {
	    
			// Next node possibly in other subtree

	    int newSize = IntStack.size(results);

	    double e = findNext(results, 1 - property, data, tree, otherChild, other, current);
	    
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
		_tree = new double[nodes.length * 2 - 1];
		createTree(_tree, 0, 0, _data, indices, 0, nodes.length - 1);
		
		// Create result stack
		_stack = IntStack.create(nodes.length);

		// Recreate node structure so the index of a node matches its ID
		for (int i = 0; i < nodes.length; i++) {
		  _data[i * 2] = nodes[i].longitude();
		  _data[i * 2 + 1] = nodes[i].latitude();
		}
	}
	
	// Create k-d (sub)tree recursively
	private static void createTree(double[] tree, int treeIndex, int property, double[] nodes, int[] indices, int left, int right) {
	  
	  if (left >= right) {
	    
	    if (left != right)
	      return;
	    
	    // Create leaf
	    tree[treeIndex] = indices[left];
	    return;
	  }
	  
	  // Calculate pivot position
	  int size = right - left + 1;
	  int full = maxFullTree(size);
	  int pos = full / 2 + Math.min(size - full, full / 2) - 1;
	  
		// Find median (element on position 'pos')
	  tree[treeIndex] = getPosition(property, nodes, indices, left, right, pos);
	  
		// Create subtrees
	  createTree(tree, treeIndex * 2 + 1, 1 - property, nodes, indices, left, left + pos);
	  createTree(tree, treeIndex * 2 + 2, 1 - property, nodes, indices, left + pos + 1, right);
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

	// Calculate 2^floor(log2(value)) without rounding errors
	private static int maxFullTree(int value) {
	  
	  int r = 1;

	  while (r <= value)
	    r *= 2;

	  return r / 2;
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
	

	// k-d tree data structure
	public double[] _tree;
	
	// Structure to store nodes with their longitude & latitude
	private double[] _data;
	
	// Stack to store the calculated results
	private int[] _stack;
	
	// Minimum bounding box
	private double[] _bounds;
}
