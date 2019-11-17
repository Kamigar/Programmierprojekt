package routeplanner.backend.app;

import java.io.FileNotFoundException;
import java.io.IOException;

import routeplanner.backend.model.*;

public class Main {
	
	private enum Command {
		
		CALC_ONE_TO_ALL
	}
	
	private static class Parameters {
		
		public Command command = null;
		public String file = null;
	}

	private static Parameters readParameters(String[] args) {
		
		Parameters p = new Parameters();

		for (int i = 0; i < args.length; i++) {
			
			switch (args[i]) {
			
			case "--input-file":
			case "-i":
				
				i++;
				if (args.length == i)
					throw null;

				p.file = args[i];
				break;
				
			case "--one-to-all":
			case "-ota":
				
				p.command = Command.CALC_ONE_TO_ALL;
				break;
				
			default:
				
				throw null;
			}
		}
		return p;
	}

	public static void main(String[] args) {

		try {
			
			Parameters param = readParameters(args);
			if (param.file == null || param.command == null)
				throw null;


			System.out.println("Reading file");
			
			long startTime = System.nanoTime();

			Node[] nodes = FileScanner.read(param.file);
			
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
			System.exit(-1);

		} catch (IOException e) {
			
			System.out.println("I/O exception");
			System.exit(-2);

		} catch (Exception e) {
			
			System.out.println("Failed");
			System.exit(-3);
		}
	}
}
