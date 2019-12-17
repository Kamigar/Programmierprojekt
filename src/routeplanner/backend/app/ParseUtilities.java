package routeplanner.backend.app;

import java.text.DecimalFormat;

public class ParseUtilities {
	
	// Note: Defined in standard but not accessible
	private static final int maximumFractionDigits = 340;
	
	public static String doubleToString(double value) {
		
		DecimalFormat f = new DecimalFormat("0");
		f.setMaximumFractionDigits(maximumFractionDigits);
			
		return f.format(value);
	}
}
