package routeplanner.backend.app;

import java.io.FileNotFoundException;
import java.io.IOException;

import routeplanner.backend.model.*;

public class Main {

	public static void main(String[] args) {

		try {

			Node[] nodes = FileScanner.read("data/stgtregbz.fmi");
			
			System.out.println("" + nodes.length + " nodes read");
			
			int x = 34;

		} catch (FileNotFoundException e) {
			
			System.out.println("File not found");

		} catch (IOException e) {
			
			System.out.println("I/O exception");
		}
	}
}
