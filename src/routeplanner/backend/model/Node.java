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
		
		reset();
	}
	
	public void reset() {
		
		_previous = null;
		_distance = Double.POSITIVE_INFINITY;
		_entry = null;
	}
	
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
	
	public Node previous() {
		return _previous;
	}
	
	public void setPrevious(Node previous) {
		_previous = previous;
	}
	
	public double distance() {
		return _distance;
	}
	
	public void setDistance(double distance) {
		_distance = distance;
	}
	
	public Queue.Entry entry() {
		return _entry;
	}
	
	public void setEntry(Queue.Entry entry) {
		_entry = entry;
	}
	
	
	private int _id;
	
	private double _latitude;
	private double _longitude;
	
	private Edge[] _edges;
	
	private Node _previous;
	private double _distance;
	
	private Queue.Entry _entry;
}
