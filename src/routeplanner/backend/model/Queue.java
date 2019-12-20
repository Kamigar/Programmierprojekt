package routeplanner.backend.model;

public class Queue {
	
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
		
		private int _index;
		private double _key;
		private Node _value;
	}
	
	
	public Queue(int size) {
		
		_entries = new Entry[size];

		for (int i = 0; i < size; i++)
			_entries[i] = new Entry();
		
		_size = 0;
	}
	
	public Entry insert(Node value, double key) {
		
		Entry t = insert(value);
		
		decreaseKey(t, key);
		
		return t;
	}

	public Entry insert(Node value) {

		Entry t = _entries[_size];

		t._index = _size;
		t._key = Double.POSITIVE_INFINITY;
		t._value = value;
		
		_size++;

		return t;
	}
	
	public Entry decreaseKey(Entry entry, double key) {
		
		return decreaseKey(entry, entry._index, key);
	}

	private Entry decreaseKey(Entry entry, int index, double key) {
		
		if (index == 0)
			return setEntry(entry, index, key);
		
		Entry parent = _entries[(index - 1) / 2];
		
		if (key >= parent._key)
			return setEntry(entry, index, key);

		setEntry(parent, index);
		
		return decreaseKey(entry, (index - 1) / 2, key);
	}
	
	public Entry increaseKey(Entry entry, double key) {
		
		return increaseKey(entry, entry._index, key);
	}

	private Entry increaseKey(Entry entry, int index, double key) {
		
		if (index * 2 + 1 >= _size)
			return setEntry(entry, index, key);
		
		Entry left = _entries[index * 2 + 1];
		
		if (index * 2 + 2 >= _size) {
			// Single child
			if (key <= left._key)
				return setEntry(entry, index, key);
			
			// Note: 'left' must be leaf of tree
			setEntry(left, index);
			
			return setEntry(entry, index * 2 + 1, key);
		}
		
		Entry min = _entries[index * 2 + 2];
		if (min._key > left._key)
			min = left;
		
		if (key <= min._key)
			return setEntry(entry, index, key);
		
		int newIndex = min._index;

		setEntry(min, index);
		
		return increaseKey(entry, newIndex, key);
	}
	
	public Entry peek() {
		
		return _entries[0];
	}
	
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
	
	public boolean isEmpty() {
		return _size == 0;
	}
	
	public int size() {
		return _size;
	}
	

	private Entry[] _entries;
	
	private int _size;
}
