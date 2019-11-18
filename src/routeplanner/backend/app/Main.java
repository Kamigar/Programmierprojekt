package routeplanner.backend.app;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import routeplanner.backend.model.*;

public class Main {
	
	private enum Command {
		
		ONE_TO_ALL,
		ONE_TO_ONE,
		INTERACTIVE
	}
	
	private static class Parameters {
		
		public Command command = null;
		public InputStreamReader in = null;
		public OutputStreamWriter out = null;
		public int start = -1;
		public int end = -1;
	}

	private static Parameters readParameters(String[] args) throws IOException {
		
		Parameters p = new Parameters();
		
		p.in = new InputStreamReader(System.in);
		p.out = new OutputStreamWriter(System.out);

		for (int i = 0; i < args.length; i++) {
			
			switch (args[i]) {
			
			case "--input-file":
			case "-i":
				
				i++;
				if (args.length == i)
					throw null;

				p.in = new FileReader(args[i]);
				break;
				
			case "--output-file":
			case "-o" :
				
				i++;
				if (args.length == i)
					throw null;
				
				p.out = new FileWriter(args[i]);
				break;
				
			case "--one-to-all":
			case "-ota":
				
				p.command = Command.ONE_TO_ALL;
				break;
				
			case "--one-to-one":
			case "-oto":
				
				p.command = Command.ONE_TO_ONE;
				break;
				
			case "--interactive":
			case "-ita":
				
				p.command = Command.INTERACTIVE;
				break;
				
			case "--start":
			case "-s":
				
				i++;
				if (args.length == i)
					throw null;
				
				p.start = Integer.parseUnsignedInt(args[i]);
				break;
				
			case "--end":
			case "-e":
				
				i++;
				if (args.length == i)
					throw null;
				
				p.end = Integer.parseUnsignedInt(args[i]);
				break;
				
			default:
				
				throw null;
			}
		}
		
		if (p.command == null
				|| p.command == Command.INTERACTIVE && (p.start != -1 || p.end != -1)
				|| p.command == Command.ONE_TO_ALL && (p.start == -1 || p.end != -1)
				|| p.command == Command.ONE_TO_ONE && (p.start == -1 || p.end == -1)) {
			
			throw null;
		}

		return p;
	}

	public static void main(String[] args) {

		try {
			
			Parameters param = readParameters(args);


			System.out.println("Reading file");
			
			long startTime = System.nanoTime();

			Node[] nodes = FileScanner.read(param.in);
			
			long endTime = System.nanoTime();
			
			System.out.println("" + nodes.length + " nodes read in "
					+ (double)(endTime - startTime) / 1000000000 + " seconds");
			
			
			if (param.command == Command.INTERACTIVE) {
				
				// ToDo: Read input.
				throw null;
			}
			
			if (param.start < 0 || param.start >= nodes.length)
				throw null;

			startTime = System.nanoTime();
			
			Dijkstra.DijkstraStructure struct = Dijkstra.calculate(nodes, param.start);
			
			endTime = System.nanoTime();
			
			System.out.println("Path calculated in "
					+ (double)(endTime - startTime) / 1000000000 + " seconds");
			
			
			switch (param.command) {
			
			case ONE_TO_ALL:

				for (DijkstraNode node : struct.ordered) {

					System.out.println("Node " + node.node().id()
							+ " distance " + node.distance());
				}
				
				break;
				
			case ONE_TO_ONE:
				
				if (param.end < 0 || param.end >= nodes.length)
					throw null;

				System.out.println("Node " + struct.nodes[param.end].node().id()
						+ " distance " + struct.nodes[param.end].distance());
				
				break;
			
			case INTERACTIVE:
				
				throw null;
			}

		} catch (IOException e) {
			
			System.out.println("I/O exception");
			System.exit(-1);

		} catch (Exception e) {
			
			System.out.println("General failure");
			System.exit(-2);
		}
	}
}
