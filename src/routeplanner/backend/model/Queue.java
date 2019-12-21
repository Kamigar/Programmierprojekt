package routeplanner.backend.model;

/*
 * Priority queue implementation as binary heap
 */
public class Queue {
	
	/*
	 * Entry of the queue
	 */
	public static class Entry {
		
		private Entry()
		{}
		
		public int index() {
			return _index;
		}
		
		public double key() {
			return _key;
		}
		
		public Node value() {
			return _value;
		}
		
		// Index in the queue
		private int _index;
		// Key (distance)
		private double _key;
		// Value (node)
		private Node _value;
	}
	
	
	// Initialize queue with a fixed maximal size
	public Queue(int size) {
		
		_entries = new Entry[size];

		for (int i = 0; i < size; i++)
			_entries[i] = new Entry();
		
		_size = 0;
	}
	
	// Insert node and decrease key
	public Entry insert(Node value, double key) {
		
		Entry t = insert(value);
		
		decreaseKey(t, key);
		
		return t;
	}

	// Insert a node in the queue
	public Entry insert(Node value) {

		Entry t = _entries[_size];

		t._index = _size;
		t._key = Double.POSITIVE_INFINITY;
		t._value = value;
		
		_size++;

		return t;
	}
	
	// Decrease key of a given entry
	public Entry decreaseKey(Entry entry, double key) {
		
		return decreaseKey(entry, entry._index, key);
	}

	// Decrease key recursively
	private Entry decreaseKey(Entry entry, int index, double key) {
		
		// Entry is root (minimal key)
		if (index == 0)
			return setEntry(entry, index, key);
		
		Entry parent = _entries[(index - 1) / 2];
		
		// Parent is bigger, so correct position found
		if (key >= parent._key)
			return setEntry(entry, index, key);

		// Swap with parent and decrease key on new position
		setEntry(parent, index);
		
		return decreaseKey(entry, (index - 1) / 2, key);
	}
	
	// Increase key of a given entry
	public Entry increaseKey(Entry entry, double key) {
		
		return increaseKey(entry, entry._index, key);
	}

	// Increase key recursively
	private Entry increaseKey(Entry entry, int index, double key) {
		
		// Entry is leaf (no successor)
		if (index * 2 + 1 >= _size)
			return setEntry(entry, index, key);
		
		Entry left = _entries[index * 2 + 1];
		
		if (index * 2 + 2 >= _size) {
			// Single child
			
			// Left is bigger, so correct position found
			if (key <= left._key)
				return setEntry(entry, index, key);
			
			// Swap with left
			// Note: 'left' must be leaf of tree
			setEntry(left, index);
			
			return setEntry(entry, index * 2 + 1, key);
		}
		
		Entry min = _entries[index * 2 + 2];
		if (min._key > left._key)
			min = left;
		
		// Smaller successor is bigger, so correct position found
		if (key <= min._key)
			return setEntry(entry, index, key);
		
		// Swap with successor and increase key on new position
		int newIndex = min._index;

		setEntry(min, index);
		
		return increaseKey(entry, newIndex, key);
	}
	
	// Get first entry (minimal key)
	public Entry peek() {
		
		return _entries[0];
	}
	
	// Get and remove first entry (minimal key)
	public Node poll() {
		
		Entry first = peek();
		
		Entry last = setEntry(_entries[_size - 1], 0);
		
		setEntry(first, _size - 1);
		
		_size--;
		
		increaseKey(last, last._key);
		
		return first._value;
	}
	
	private Entry setEntry(Entry entry, int index) {
		
		entry._index = index;
		
		_entries[index] = entry;
		
		return entry;
	}

	private Entry setEntry(Entry entry, int index, double key) {
		
		entry._key = key;
		
		return setEntry(entry, index);
	}
	
	// Check if the queue is empty
	public boolean isEmpty() {
		return _size == 0;
	}
	
	// Return current number of entries
	public int size() {
		return _size;
	}
	

	// Entries in the queue
	private Entry[] _entries;
	
	// Number of entries in the queue
	private int _size;
}
