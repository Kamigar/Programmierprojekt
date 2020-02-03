package routeplanner.backend.model;

/*
 * Static helper class to use an integer array as stack
 */
public class IntStack {
	
	// Creates an integer stack with the specified capacity
	public static int[] create(int capacity) {

		// Note: Java language guarantees initialization with 0
		return new int[capacity + 1];
	}
	
	// Push a value on the stack
	public static void push(int[] stack, int value) {
		
		changeSize(stack, 1);

		stack[size(stack)] = value;
	}
	
	// Pop a value from the stack
	public static int pop(int[] stack) {
		
		int r = stack[size(stack)];

		changeSize(stack, -1);
		
		return r;
	}
	
	// Get the value on the specified position
	public static int get(int[] stack, int index) {
		
		return stack[index + 1];
	}
	
	// Remove the last 'count' elements from the stack
	public static void remove(int[] stack, int count) {
		
		changeSize(stack, -count);
	}
	
	// Remove 'count' elements from the end of the stack
	//   but leave the last 'offset' elements in there
	public static void remove(int[] stack, int offset, int count) {
		
		int tailOffset = size(stack) - offset + 1;
		int frontOffset = tailOffset - count;
		
		for (int i = 0; i < offset; i++)
			stack[frontOffset + i] = stack[tailOffset + i];
		
		remove(stack, count);
	}
	
	// Remove all elements on the stack
	public static void clear(int[] stack) {
		setSize(stack, 0);
	}

	// The capacity of the stack
	public static int capacity(int[] stack) {
		return stack.length;
	}

	// The current number of elements on the stack
	public static int size(int[] stack) {
		return stack[0];
	}
	
	// Set current number of elements
	private static void setSize(int[] stack, int size) {
		stack[0] = size;
	}
	
	// Increase number of elements by 'count'
	private static void changeSize(int[] stack, int count) {
		stack[0] += count;
	}
}
