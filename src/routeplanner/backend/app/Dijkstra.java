package routeplanner.backend.app;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import routeplanner.backend.model.*;

public class Dijkstra {
	
	private static final int s_defaultQueueCapacity = 256;
	
	public static class DijkstraStructure {
		public DijkstraNode[] nodes;
		public LinkedList<DijkstraNode> ordered;
	}
	
	public static class DistanceComparator implements Comparator<DijkstraNode> {
		
		@Override
		public int compare(DijkstraNode first, DijkstraNode second) {

			double diff = first.distance() - second.distance();

			if (diff > 0) return 1;
			if (diff < 0) return -1;
			return 0;
		}
	}
	
	public static DijkstraStructure calculate(Node[] structure, int index) {
		
		System.out.println("Create Dijkstra nodes");
		
		DijkstraNode[] nodes = new DijkstraNode[structure.length];
		
		PriorityQueue<DijkstraNode> nodeList = new PriorityQueue<DijkstraNode>(
				s_defaultQueueCapacity, new DistanceComparator());

		LinkedList<DijkstraNode> finishedNodes = new LinkedList<DijkstraNode>();
		
		for (int i = 0; i < structure.length; i++) {
			nodes[i] = new DijkstraNode(structure[i]);
		}
		
		nodes[index].setDistance(0);
		nodeList.add(nodes[index]);

		System.out.println("Calculate distances");
		
		while (!nodeList.isEmpty()) {
			
			DijkstraNode current = nodeList.poll();

			finishedNodes.addLast(current);
			
			for (Edge edge : current.node().edges()) {
				
				DijkstraNode neighbour = nodes[edge.trg().id()];
				
				double newDistance = current.distance() + edge.cost();
				double oldDistance = neighbour.distance();
				if (oldDistance > newDistance) {
					
					nodeList.remove(neighbour);

					neighbour.setDistance(newDistance);
					neighbour.setPrevious(current);
					
					nodeList.add(neighbour);
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
