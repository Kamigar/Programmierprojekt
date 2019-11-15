package routeplanner.backend.app;

import java.io.FileNotFoundException;

public class Main {

	public static void main(String[] args) {

		try {

			FileScanner.read("data/toy.fmi");

		} catch (FileNotFoundException e) {
			
			System.out.println("File not found");
		}
	}
}
