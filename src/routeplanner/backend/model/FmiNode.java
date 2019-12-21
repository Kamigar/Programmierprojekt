package routeplanner.backend.model;

/*
 * Represents a node in the graph description file
 */
public class FmiNode {

	public FmiNode(int id, double latitude, double longitude) {

		_id = id;
		_latitude = latitude;
		_longitude = longitude;
	}

	public int id() {
		return _id;
	}

	public double latitude() {
		return _latitude;
	}

	public double longitude() {
		return _longitude;
	}


	// ID
	private int _id;

	// Latitude
	private double _latitude;
	// Longitude
	private double _longitude;
}
