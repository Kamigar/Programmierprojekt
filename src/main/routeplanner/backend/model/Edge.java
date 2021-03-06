package routeplanner.backend.model;

/*
 * Represents an edge of the graph
 */
public class Edge {
	
	public Edge(Node src, Node trg, int cost) {
		
		_src = src;
		_trg = trg;
		_cost = cost;
	}
	
	public Node src() {
		return _src;
	}
	
	public Node trg() {
		return _trg;
	}
	
	public int cost() {
		return _cost;
	}
	

	// Source node
	private Node _src;
	// Target node
	private Node _trg;
	
	// Cost (distance) from source to target
	private int _cost;
}
