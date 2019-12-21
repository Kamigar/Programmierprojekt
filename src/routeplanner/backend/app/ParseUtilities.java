package routeplanner.backend.app;

import java.text.DecimalFormat;

/*
 * Helper functions for parsing
 */
public class ParseUtilities {
	
	// Note: Defined in standard but not accessible
	private static final int maximumFractionDigits = 340;
	
	// Converts a double to a String (without '.0' if integer value)
	public static String doubleToString(double value) {
		
		DecimalFormat f = new DecimalFormat("0");
		f.setMaximumFractionDigits(maximumFractionDigits);
			
		return f.format(value);
	}
}
