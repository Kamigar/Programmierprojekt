package routeplanner.backend.model;

/*
 * Priority queue implementation as binary heap
 */
public class BinaryHeap {
	
	// Initialize heap with a fixed maximal size
	public BinaryHeap(int size) {
		
		_data = new int[size * 2];
		_size = 0;
	}
	
	// Insert id and decrease key
	public void insert(int id, int key) {
		
		decreaseKey(_size, id, key);
		
		_size++;
	}
	
	// Decrease key recursively
	private void decreaseKey(int index, int id, int key) {
		
		int parentIndex = (index - 1) / 2;
		
		if (index == 0 || key >= key(parentIndex)) {
			// Entry is root or parent key is not smaller
			set(index, id, key);

		} else {
			// Swap with parent and decrease key on new position
			set(index, id(parentIndex), key(parentIndex));
			
			decreaseKey(parentIndex, id, key);		
		}
	}

	// Increase key recursively
	private void increaseKey(int index, int id, int key) {
		
		int left = index * 2 + 1, right = index * 2 + 2;
		
		if (left >= _size) {
			// Entry is leaf (no successor)
			set(index, id, key);

		} else {

			if (right >= _size) {
				// Single child

				if (key <= key(left)) {
					// Left is not smaller, so correct position found
					set(index, id, key);

				} else {
					// Swap with left
					// Note: left must be leaf
					set(index, id(left), key(left));
					set(left, id, key);	
				}

			} else {

				int min = key(left) > key(right) ? right : left;
				
				if (key <= key(min)) {
					// Smaller successor is not smaller, so correct position found
					set(index, id, key);

				} else {
					// Swap with successor and increase key on new position
					set(index, id(min), key(min));
					
					increaseKey(min, id, key);			
				}
			}
		}
	}
	
	// Get first id (minimal key)
	public int peek() {
		
		return id(0);
	}
	
	// Get and remove first id (minimal key)
	public int poll() {
		
		int first = peek();

		_size--;
		
		if (_size != 0)
			increaseKey(0, id(_size), key(_size));
		
		return first;
	}
	
	// Check if the queue is empty
	public boolean isEmpty() {
		return _size == 0;
	}
	
	// Return current number of entries
	public int size() {
		return _size;
	}
	
	private int key(int i) {
		
		return _data[i * 2];
	}
	
	private void setKey(int i, int v) {
		
		_data[i * 2] = v;
	}
	
	private int id(int i) {
		
		return _data[i * 2 + 1];
	}
	
	private void setId(int i, int v) {
		
		_data[i * 2 + 1] = v;
	}
	
	private void set(int index, int id, int key) {
		
		setKey(index, key);
		setId(index, id);
	}


	// Data to store in the heap
	private int[] _data;
	
	// Number of entries in the queue
	private int _size;
}
