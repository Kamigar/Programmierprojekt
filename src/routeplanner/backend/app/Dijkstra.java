package routeplanner.backend.app;

import java.util.Vector;

import routeplanner.backend.model.*;

public class Dijkstra {
	
	public static class DijkstraStructure {
		public DijkstraNode[] nodes;
		public Vector<DijkstraNode> ordered;
	}
	
	public static DijkstraStructure calculate(Node[] structure, int index) {
		
		System.out.println("Create Dijkstra nodes");
		
		DijkstraNode[] nodes = new DijkstraNode[structure.length];

		Vector<Queue.Entry<DijkstraNode>> entries =
				new Vector<Queue.Entry<DijkstraNode>>(structure.length);

		for (int i = 0; i < structure.length; i++)
			entries.add(null);
		
		Queue<DijkstraNode> nodeList = new Queue<DijkstraNode>(structure.length);

		Vector<DijkstraNode> finishedNodes = new Vector<DijkstraNode>(structure.length);
		
		for (int i = 0; i < structure.length; i++) {
			nodes[i] = new DijkstraNode(structure[i]);
		}
		
		nodes[index].setDistance(0);
		entries.set(index, nodeList.insert(nodes[index], 0));

		System.out.println("Calculate distances");
		
		while (!nodeList.isEmpty()) {
			
			DijkstraNode current = nodeList.poll();

			finishedNodes.add(current);
			
			for (Edge edge : current.node().edges()) {
				
				DijkstraNode neighbour = nodes[edge.trg().id()];
				
				double newDistance = current.distance() + edge.cost();
				double oldDistance = neighbour.distance();
				if (oldDistance > newDistance) {

					neighbour.setDistance(newDistance);
					neighbour.setPrevious(current);
					
					Queue.Entry<DijkstraNode> entry = entries.get(neighbour.node().id());

					if (entry == null) {
						
						entries.set(neighbour.node().id(), nodeList.insert(neighbour, neighbour.distance()));

					} else {
						
						nodeList.decreaseKey(entry, neighbour.distance());
					}
				}
			}
		}
		
		System.out.println("Calculation finished");
		
		DijkstraStructure r = new DijkstraStructure();
		r.nodes = nodes;
		r.ordered = finishedNodes;
		return r;
	}
}
