package routeplanner.backend.model;

import java.util.Vector;
import java.util.LinkedList;
import java.util.Iterator;

/*
 * Priority queue implementation as radix heap
 */
public class Queue {

	/*
	 * Entry of the queue
	 */
	public static class Entry {
		
		private Entry(Node node, int key) {
			_node = node;
			_key = key;
		}
		
		public int key() {
			return _key;
		}
		
		public Node node() {
			return _node;
		}

		private int _key;
		private Node _node;
	}
	
	// Initialize queue for maximal possible key values
	public Queue() {
		
		this(Integer.MAX_VALUE - 1);
	}
	
	// Initialize queue with maximal key value
	public Queue(int keyBound) {
		
		int bNum = (int)(Math.log(keyBound + 1) / Math.log(2)) + 1;
		
		_buckets = new Vector<LinkedList<Entry>>(bNum);
		for (int i = 0; i < bNum; i++)
			_buckets.add(new LinkedList<Entry>());
		
		_emptyList = new LinkedList<Entry>();
		_lastRemoved = 0;
		_size = 0;
	}
	
	// Return current number of entries
	public int size() {
		return _size;
	}
	
	// Check if the queue is empty
	public boolean isEmpty() {
		return _size == 0;
	}
	
	// Insert node with given key
	public Entry insert(Node node, int key) {
		
		Entry t = new Entry(node, key);
		insert(t);

		_size++;
		return t;
	}
	
	private void insert(Entry entry) {
		
		_buckets.get(bucketIndex(entry._key)).add(entry);
	}
	
	// Get and remove first entry (minimal key)
	public Entry poll() {
		
		// Find first non-empty bucket
		int bIndex = 0;
		for (; _buckets.get(bIndex).isEmpty(); bIndex++);
		
		if (bIndex == 0) {
			// First bucket is not empty, so no redistribution of entries needed
			_size--;
			return _buckets.firstElement().removeFirst();
		}

		LinkedList<Entry> list = _buckets.get(bIndex);
		
		// Find minimal entry in bucket
		Iterator<Entry> it = list.iterator();
		int min = it.next()._key;
		while (it.hasNext()) {
			int t = it.next()._key;
			if (t < min)
				min = t;
		}
		
		_lastRemoved = min;
		
		_buckets.set(bIndex, _emptyList);

		// Redistribute other entries in bucket
		it = list.iterator();
		Entry entry = it.next();
		for (; entry._key != min; entry = it.next()) {

			it.remove();
			insert(entry);
		}
		
		it.remove();
		Entry root = entry;
		
		while (it.hasNext()) {
			entry = it.next();
			it.remove();
			insert(entry);
		}
		
		_emptyList = list;
		_size--;
		return root;
	}
	
	// Decrease key of a given entry
	public void decreaseKey(Entry entry, int key) {
		
		int oldIndex = bucketIndex(entry._key);
		int newIndex = bucketIndex(key);
		
		entry._key = key;
		
		if (oldIndex != newIndex) {

			_buckets.get(oldIndex).remove(entry);
			insert(entry);
		}
	}
	
	// Calculate bucket index for given key
	private int bucketIndex(int key) {

		return highestBit(_lastRemoved ^ key);
	}
	
	// Find index of highest set bit in value
	private static int highestBit(int value) {
		
		return 32 - Integer.numberOfLeadingZeros(value);
	}


	// Array of buckets
	private Vector<LinkedList<Entry>> _buckets;
	// Key of the last removed entry
	private int _lastRemoved;
	
	// Number of entries in the queue
	private int _size;

	// Just an empty list to prevent concurrent list
	//   modifications while iterating (cf. function 'poll()')
	private LinkedList<Entry> _emptyList;
}
