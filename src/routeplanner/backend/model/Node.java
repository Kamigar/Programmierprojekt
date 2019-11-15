package routeplanner.backend.model;

public class Node {

	public Node(int id, double latitude, double longitude) {

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


	private int _id;

	private double _latitude;
	private double _longitude;
}
