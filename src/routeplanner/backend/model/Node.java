package routeplanner.backend.model;

/*
 * Represents a node of the graph
 */
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
		
		reset();
	}
	
	// Reset the working values of the Dijkstra algorithm
	public void reset() {
		
		_distance = Integer.MAX_VALUE;
	}
	
	// Reset an array of nodes
	public static void reset(Node[] nodes) {
		
		for (Node node : nodes)
			node.reset();
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
	
	public int distance() {
		return _distance;
	}
	
	public void setDistance(int distance) {
		_distance = distance;
	}
	
	
	// ID
	private int _id;
	
	// Latitude
	private double _latitude;
	// Longitude
	private double _longitude;
	
	// Edges from this to another node
	private Edge[] _edges;
	
	// Shortest distance from start point
	private int _distance;
}
