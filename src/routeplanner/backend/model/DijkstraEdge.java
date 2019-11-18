package routeplanner.backend.model;

public class DijkstraEdge {
	
	public DijkstraEdge(Edge edge, DijkstraNode src,
			DijkstraNode trg) {
		
		_src = src;
		_trg = trg;
		_cost = edge.cost();
	}
	
	public DijkstraNode src() {
		return _src;
	}
	
	public DijkstraNode trg() {
		return _trg;
	}
	
	public double cost() {
		return _cost;
	}


	private DijkstraNode _src;
	private DijkstraNode _trg;
	
	private double _cost;
}
