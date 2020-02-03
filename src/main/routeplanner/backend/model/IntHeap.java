package routeplanner.backend.model;

/*
 * Static helper class to use an integer array as priority queue
 */
public class IntHeap {
	
	// Create an integer binary heap with the specified capacity
	public static int[] create(int capacity) {
		
		// Note: Java language guarantees initialization with 0
		return new int[capacity * 2 + 1];
	}
	
	// Insert ID and decrease key
	public static void insert(int[] heap, int id, int key) {
		
		decreaseKey(heap, size(heap), id, key);
		
		changeSize(heap, 1);
	}
	
	// Decrease key recursively
	private static void decreaseKey(int[] heap, int index, int id, int key) {
		
		int parentIndex = (index - 1) / 2;
		
		if (index == 0 || key >= key(heap, parentIndex)) {
			// Entry is root or parent key is not smaller
			set(heap, index, id, key);

		} else {
			// Swap with parent and decrease key on new position
			set(heap, index, id(heap, parentIndex), key(heap, parentIndex));
			
			decreaseKey(heap, parentIndex, id, key);		
		}
	}
	
	// Increase key recursively
	private static void increaseKey(int[] heap, int index, int id, int key) {
		
		int left = index * 2 + 1, right = index * 2 + 2;
		
		if (left >= size(heap)) {
			// Entry is leaf (no successor)
			set(heap, index, id, key);

		} else {

			if (right >= size(heap)) {
				// Single child

				if (key <= key(heap, left)) {
					// Left is not smaller, so correct position found
					set(heap, index, id, key);

				} else {
					// Swap with left
					// Note: left must be leaf
					set(heap, index, id(heap, left), key(heap, left));
					set(heap, left, id, key);	
				}

			} else {

				int min = key(heap, left) > key(heap, right) ? right : left;
				
				if (key <= key(heap, min)) {
					// Smaller successor is not smaller, so correct position found
					set(heap, index, id, key);

				} else {
					// Swap with successor and increase key on new position
					set(heap, index, id(heap, min), key(heap, min));
					
					increaseKey(heap, min, id, key);			
				}
			}
		}
	}
	
	// Get and remove first ID (minimal key)
	public static int poll(int[] heap) {
		
		int first = peek(heap);
		
		changeSize(heap, -1);
		
		if (size(heap) != 0)
			increaseKey(heap, 0, id(heap, size(heap)), key(heap, size(heap)));
		
		return first;
	}
	
	// Get first ID (minimal key)
	public static int peek(int[] heap) {
		return id(heap, 0);
	}

	// Remove all entries from the queue
	public static void clear(int[] heap) {
		setSize(heap, 0);
	}
	
	// Return maximum capacity
	public static int capacity(int[] heap) {
		return (heap.length - 1) / 2;
	}

	// Check if the queue is empty
	public static boolean isEmpty(int[] heap) {
		return size(heap) == 0;
	}

	// Return current number of entries
	public static int size(int[] heap) {
		return heap[0];
	}
	
	// Set current number of entries
	private static void setSize(int[] heap, int count) {
		heap[0] = count;
	}
	
	// Increase number of entries by 'count'
	private static void changeSize(int[] heap, int count) {
		heap[0] += count;
	}
	
	// Get key on given index
	private static int key(int[] heap, int index) {
		return heap[index * 2 + 1];
	}
	
	// Get ID on given index
	private static int id(int[] heap, int index) {
		return heap[index * 2 + 2];
	}
	
	// Set ID and key on given index
	private static void set(int[] heap, int index, int id, int key) {
		heap[index * 2 + 1] = key;
		heap[index * 2 + 2] = id;
	}
}
