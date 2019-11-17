package routeplanner.backend.model;

import java.util.Vector;

public class Queue<T> {
	
	public static class Entry<T> {
		
		private Entry()
		{}
		
		public int index() {
			return _index;
		}
		
		public double key() {
			return _key;
		}
		
		public T value() {
			return _value;
		}
		
		private int _index;
		private double _key;
		private T _value;
	}
	
	
	public Queue(int size) {
		
		_entries = new Vector<Entry<T>>(size);

		for (int i = 0; i < size; i++)
			_entries.add(new Entry<T>());
		
		_size = 0;
	}
	
	public Entry<T> insert(T value, double key) {
		
		Entry<T> t = insert(value);
		
		decreaseKey(t, key);
		
		return t;
	}

	public Entry<T> insert(T value) {

		Entry<T> t = _entries.get(_size);

		t._index = _size;
		t._key = Double.POSITIVE_INFINITY;
		t._value = value;
		
		_size++;

		return t;
	}
	
	public Entry<T> decreaseKey(Entry<T> entry, double key) {
		
		return decreaseKey(entry, entry._index, key);
	}

	private Entry<T> decreaseKey(Entry<T> entry, int index, double key) {
		
		if (index == 0)
			return setEntry(entry, index, key);
		
		Entry<T> parent = _entries.get((index - 1) / 2);
		
		if (key >= parent._key)
			return setEntry(entry, index, key);

		setEntry(parent, index);
		
		return decreaseKey(entry, (index - 1) / 2, key);
	}
	
	public Entry<T> increaseKey(Entry<T> entry, double key) {
		
		return increaseKey(entry, entry._index, key);
	}

	private Entry<T> increaseKey(Entry<T> entry, int index, double key) {
		
		if (index * 2 + 1 >= _size)
			return setEntry(entry, index, key);
		
		Entry<T> left = _entries.get(index * 2 + 1);
		
		if (index * 2 + 2 >= _size) {
			// Single child
			if (key <= left._key)
				return setEntry(entry, index, key);
			
			// Note: 'left' must be leaf of tree
			setEntry(left, index);
			
			return setEntry(entry, index * 2 + 1, key);
		}
		
		Entry<T> min = _entries.get(index * 2 + 2);
		if (min._key > left._key)
			min = left;
		
		if (key <= min._key)
			return setEntry(entry, index, key);
		
		int newIndex = min._index;

		setEntry(min, index);
		
		return increaseKey(entry, newIndex, key);
	}
	
	public Entry<T> peek() {
		
		return _entries.get(0);
	}
	
	public T poll() {
		
		Entry<T> first = peek();
		
		Entry<T> last = setEntry(_entries.get(_size - 1), 0);
		
		setEntry(first, _size - 1);
		
		_size--;
		
		increaseKey(last, last._key);
		
		return first._value;
	}
	
	private Entry<T> setEntry(Entry<T> entry, int index) {
		
		entry._index = index;
		
		_entries.set(index, entry);
		
		return entry;
	}

	private Entry<T> setEntry(Entry<T> entry, int index, double key) {
		
		entry._key = key;
		
		return setEntry(entry, index);
	}
	
	public boolean isEmpty() {
		return _size == 0;
	}
	
	public int size() {
		return _size;
	}
	

	private Vector<Entry<T>> _entries;
	
	private int _size;
}
