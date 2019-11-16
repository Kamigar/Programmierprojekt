package routeplanner.backend.model;

public class DijkstraNode {
	
	public DijkstraNode(Node node) {

		_node = node;

		_previous = null;
		_distance = Double.POSITIVE_INFINITY;
	}
	
	public Node node() {
		return _node;
	}
	
	public DijkstraNode previous() {
		return _previous;
	}
	
	public void setPrevious(DijkstraNode previous) {
		_previous = previous;
	}
	
	public double distance() {
		return _distance;
	}
	
	public void setDistance(double distance) {
		_distance = distance;
	}


	private Node _node;
	
	private DijkstraNode _previous;
	
	private double _distance;
}
