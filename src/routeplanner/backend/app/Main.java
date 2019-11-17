package routeplanner.backend.app;

import java.io.FileNotFoundException;
import java.io.IOException;

import routeplanner.backend.model.*;

public class Main {

	public static void main(String[] args) {

		try {

			System.out.println("Reading file");
			
			long startTime = System.nanoTime();

			Node[] nodes = FileScanner.read("data/toy.fmi");
			//Node[] nodes = FileScanner.read("data/bw.fmi");
			//Node[] nodes = FileScanner.read("data/stgtregbz.fmi");
			
			long endTime = System.nanoTime();
			
			System.out.println("" + nodes.length + " nodes read in "
					+ (double)(endTime - startTime) / 1000000000 + " seconds");
			
			startTime = System.nanoTime();
			
			Dijkstra.DijkstraStructure struct = Dijkstra.calculate(nodes, 3);
			
			endTime = System.nanoTime();
			
			System.out.println("Path calculated in "
					+ (double)(endTime - startTime) / 1000000000 + " seconds");

			int x = 34;

		} catch (FileNotFoundException e) {
			
			System.out.println("File not found");

		} catch (IOException e) {
			
			System.out.println("I/O exception");
		}
	}
}
