package routeplanner.backend.model;

public class DijkstraEdge {
	
	public DijkstraEdge(Edge edge, DijkstraNode trg) {
		
		_trg = trg;
		_cost = edge.cost();
	}
	
	public DijkstraNode trg() {
		return _trg;
	}
	
	public double cost() {
		return _cost;
	}


	private DijkstraNode _trg;
	
	private double _cost;
}
