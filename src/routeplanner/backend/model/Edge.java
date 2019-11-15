package routeplanner.backend.model;

public class Edge {
	
	public Edge(Node src, Node trg, double cost) {
		
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
	
	public double cost() {
		return _cost;
	}
	

	private Node _src;
	private Node _trg;
	
	private double _cost;
}
