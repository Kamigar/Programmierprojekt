package routeplanner.backend.model.fmi;

public class Edge {

	public Edge(int srcId, int trgId, double cost) {

		_srcId = srcId;
		_trgId = trgId;
		_cost = cost;
	}

	public int srcId() {
		return _srcId;
	}

	public int trgId() {
		return _trgId;
	}

	public double cost() {
		return _cost;
	}


	private int _srcId;
	private int _trgId;

	private double _cost;
}
