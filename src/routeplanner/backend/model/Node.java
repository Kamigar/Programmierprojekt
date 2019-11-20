package routeplanner.backend.model;

public class Node {
	
	public Node(int id, double latitude,
			double longitude) {
		
		this(id, latitude, longitude, null);
	}
	
	public Node(int id, double latitude,
			double longitude, Edge[] edges) {

		_id = id;
		_latitude = latitude;
		_longitude = longitude;
		_edges = edges;
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
	
	public Edge[] edges() {
		return _edges;
	}
	
	public void setEdges(Edge[] edges) {
		_edges = edges;
	}

	
	private int _id;
	
	private double _latitude;
	private double _longitude;
	
	private Edge[] _edges;
}
