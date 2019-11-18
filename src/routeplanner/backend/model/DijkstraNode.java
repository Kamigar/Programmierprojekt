package routeplanner.backend.model;

public class DijkstraNode {
	
	private DijkstraNode(Node node) {
		
		this(node, null);
	}

	public DijkstraNode(Node node, DijkstraEdge[] edges) {

		_node = node;
		_edges = edges;
		_previous = null;
		_distance = Double.POSITIVE_INFINITY;
		_entry = null;
	}
	
	public Node node() {
		return _node;
	}

	public DijkstraEdge[] edges() {
		return _edges;
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
	
	public Queue.Entry<DijkstraNode> entry() {
		return _entry;
	}
	
	public void setEntry(Queue.Entry<DijkstraNode> entry) {
		_entry = entry;
	}
	
	
	public static DijkstraNode[] createTree(Node[] nodes) {
		
		DijkstraNode[] result = new DijkstraNode[nodes.length];
		
		for (int i = 0; i < nodes.length; i++) {
			
			result[i] = new DijkstraNode(nodes[i]);
		}
		
		for (int i = 0; i < nodes.length; i++) {
			
			Edge edges[] = nodes[i].edges();
			DijkstraEdge newEdges[] = new DijkstraEdge[edges.length];
			for (int j = 0; j < edges.length; j++) {
				
				Edge edge = edges[j];
				DijkstraNode src = result[i];
				DijkstraNode trg = result[edge.trg().id()];
				
				newEdges[j] = new DijkstraEdge(edge, src, trg);
			}

			result[i]._edges = newEdges;
		}
		
		return result;
	}


	private Node _node;

	private DijkstraEdge[] _edges;
	
	private DijkstraNode _previous;
	private double _distance;
	
	private Queue.Entry<DijkstraNode> _entry;
}
