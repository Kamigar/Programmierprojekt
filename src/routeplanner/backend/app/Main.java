package routeplanner.backend.app;

import java.io.FileNotFoundException;
import java.io.IOException;

import routeplanner.backend.model.*;

public class Main {

	public static void main(String[] args) {

		try {

			//Node[] nodes = FileScanner.read("data/toy.fmi");
			Node[] nodes = FileScanner.read("data/bw.fmi");
			//Node[] nodes = FileScanner.read("data/stgtregbz.fmi");
			
			System.out.println("" + nodes.length + " nodes read");
			
			Dijkstra.DijkstraStructure struct = Dijkstra.calculate(nodes, 3);
			
			
			int x = 34;

		} catch (FileNotFoundException e) {
			
			System.out.println("File not found");

		} catch (IOException e) {
			
			System.out.println("I/O exception");
		}
	}
}
