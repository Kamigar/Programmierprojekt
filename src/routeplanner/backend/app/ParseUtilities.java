package routeplanner.backend.app;

/*
 * Helper functions for parsing
 */
public class ParseUtilities {
	
	// Convert integer to string (assuming maximal integer value means infinity)
	public static String intToString(int value) {
		
		if (value == Integer.MAX_VALUE)
			return "-1";
		
		return "" + value;
	}
}
