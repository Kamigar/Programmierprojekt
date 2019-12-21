package routeplanner.backend.model;

/*
 * Represents an edge in the graph description file
 */
public class FmiEdge {

	public FmiEdge(int srcId, int trgId, double cost) {

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


	// ID of the source node
	private int _srcId;
	// ID of the target node
	private int _trgId;

	// Cost (distance) from source to target
	private double _cost;
}
