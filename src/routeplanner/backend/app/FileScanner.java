package routeplanner.backend.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

import routeplanner.backend.model.*;

/*
 * Parsing of graph and requests
 */
public class FileScanner {
	
	// Exception classes

	public static class BadHeaderException extends Exception {
		
		private static final long serialVersionUID = 1926680537412845559L;

		public BadHeaderException(String reason) { super(reason); }
	}
	
	public static class BadNodeException extends Exception {
		
		private static final long serialVersionUID = 1378424271026452872L;

		public BadNodeException(String reason) { super(reason); }
	}
	
	public static class BadEdgeException extends Exception {
		
		private static final long serialVersionUID = -1460975851840457803L;

		public BadEdgeException(String reason) { super(reason); }
	}
	
	public static class BadRequestException extends Exception {
		
		private static final long serialVersionUID = 4575260669648696900L;

		public BadRequestException(String reason) { super(reason); }
	}
	
	// Represents the position of a character in a string
	static class StringPosition {
		String string = null;
		int index = -1;
	}
	
	// Jump to next non-whitespace character
	private static int skipWhitespace(String str, int startIndex) {
		
		int i = startIndex;
		for (; i < str.length()
				&& Character.isWhitespace(str.charAt(i)); i++);
		return i;
	}
	
	// Jump to next non-digit character
	private static int skipInteger(String str, int startIndex) {
		
		int i = startIndex;
		for (; i < str.length()
				&& Character.isDigit(str.charAt(i)); i++);
		return i;
	}
	
	// Jump over float (integer with optional decimal places)
	private static int skipFloat(String str, int startIndex) {
		
		int i = skipInteger(str, startIndex);

		if (i < str.length() && str.charAt(i) == '.') {
			
			i = skipInteger(str, i + 1);
		}
		return i;
	}
	
	// Return next line from reader, which is not a comment
	private static void getNextRelevantLine(StringPosition pos, BufferedReader reader) throws IOException {
		
		String line;
		while ((line = reader.readLine()) != null) {
			
			int i = skipWhitespace(line, 0);
			if (i < line.length() && line.charAt(i) != '#') {
				pos.string = line;
				pos.index = i;
				return;
			}
		}
		pos.string = null;
		pos.index = -1;
	}
	
	// Read the file header (number of nodes and edges)
	private static void readHeader(int[] cnt, BufferedReader reader,
			StringPosition pos, Logger logger, boolean isTolerant)
				throws BadHeaderException, IOException {
		
		try {
			
			logger.info("Number of nodes in graph:");

			getNextRelevantLine(pos, reader);
			if (pos.string == null)
				throw new BadHeaderException("No header provided");
				
			int end = skipInteger(pos.string, pos.index);
			
			try {
				cnt[0] = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadHeaderException("No node count provided [uint]");
			}
			
			end = skipWhitespace(pos.string, end);
			if (end < pos.string.length())
				throw new BadHeaderException("Too much data in the first line");
			
			logger.info("Number of edges in graph:");

			getNextRelevantLine(pos, reader);
			if (pos.string == null)
				throw new BadHeaderException("No second line provided");
			
			end = skipInteger(pos.string, pos.index);
			try {
				cnt[1] = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadHeaderException("No edge count provided [uint]");
			}
			
			end = skipWhitespace(pos.string, end);
			if (end < pos.string.length())
				throw new BadHeaderException("Too much data in the second line");	

		} catch (BadHeaderException ex) {
			
			if (isTolerant) {
				
				logger.warning(System.lineSeparator() + ex.getMessage());
				logger.warning("Error in header. Try again...");

				readHeader(cnt, reader, pos, logger, isTolerant);

			} else {

				throw ex;
			}
		}
	}
	
	// Parse a node from the reader
	private static FmiNode readNode(BufferedReader reader, StringPosition pos,
			Node[] nodes, Logger logger, boolean isTolerant)
				throws BadNodeException, IOException {
		
		try {

			getNextRelevantLine(pos, reader);
			if (pos.string == null)
				throw new BadNodeException("Unexpected end of file");
			
			int id;
			int end = skipInteger(pos.string, pos.index);
			try {
				id = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadNodeException("No nodeID provided [uint]");
			}
			
			pos.index = skipWhitespace(pos.string, end);

			end = skipInteger(pos.string, pos.index);
			if (pos.index == end)
				throw new BadNodeException("No nodeID2 provided [uint]");

			pos.index = skipWhitespace(pos.string, end);
			
			double latitude;
			end = skipFloat(pos.string, pos.index);
			try {
				latitude = Double.parseDouble(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadNodeException("No latitude provided [double]");
			}
			
			pos.index = skipWhitespace(pos.string, end);
			
			double longitude;
			end = skipFloat(pos.string, pos.index);
			try {
				longitude = Double.parseDouble(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadNodeException("No longitude provided [double]");
			}
			
			pos.index = skipWhitespace(pos.string, end);

			end = skipFloat(pos.string, pos.index);
			if (pos.index == end)
				throw new BadNodeException("No elevation provided [double]");

			end = skipWhitespace(pos.string, end);
			if (end < pos.string.length())
				throw new BadNodeException("Unexpected data in line");
			

			FmiNode node = new FmiNode(id, latitude, longitude);	
			
			if (node.id() < 0 || node.id() >= nodes.length)
				throw new BadNodeException("nodeID out of range");
			
			if (nodes[node.id()] != null)
				throw new BadNodeException("Ambiguous nodeID");
			
			return node;
			
		} catch (BadNodeException ex) {
			
			if (isTolerant) {
				
				logger.warning(System.lineSeparator() + ex.getMessage());
				logger.warning("Error in node. Try again...");
				
				return readNode(reader, pos, nodes, logger, isTolerant);

			} else {
				
				throw ex;
			}
		}
	}

	// Parse an edge from the reader
	private static FmiEdge readEdge(BufferedReader reader, StringPosition pos,
			Node[] nodes, Logger logger, boolean isTolerant)
				throws BadEdgeException, IOException {
		
		try {

			getNextRelevantLine(pos, reader);
			if (pos.string == null)
				throw new BadEdgeException("Unexpected end of file");
			
			int srcId;
			int end = skipInteger(pos.string, pos.index);
			try {
				srcId = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadEdgeException("No srcID provided [uint]");
			}
			
			pos.index = skipWhitespace(pos.string, end);
			
			int trgId;
			end = skipInteger(pos.string, pos.index);
			try {
				trgId = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadEdgeException("No trgID provided [uint]");
			}
			
			pos.index = skipWhitespace(pos.string, end);
			
			int cost;
			end = skipInteger(pos.string, pos.index);
			try {
				cost = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadEdgeException("No cost provided [uint]");
			}
			
			pos.index = skipWhitespace(pos.string, end);
			
			end = skipInteger(pos.string, pos.index);
			if (pos.index == end)
				throw new BadEdgeException("No type provided [uint]");
			
			pos.index = skipWhitespace(pos.string, end);

			end = skipFloat(pos.string, pos.index);
			if (pos.index == end)
				throw new BadEdgeException("No maxspeed provided [double]");
			
			end = skipWhitespace(pos.string, end);
			if (end < pos.string.length())
				throw new BadEdgeException("Unexpected data in line");
			

			FmiEdge edge = new FmiEdge(srcId, trgId, cost);	
			
			if (edge.srcId() < 0 || edge.srcId() >= nodes.length)
				throw new BadEdgeException("srcID out of range");
			
			if (edge.trgId() < 0 || edge.trgId() >= nodes.length)
				throw new BadEdgeException("trgID out of range");
			
			return edge;
			
		} catch (BadEdgeException ex) {
			
			if (isTolerant) {
				
				logger.warning(System.lineSeparator() + ex.getMessage());
				logger.warning("Error in edge. Try again...");
				
				return readEdge(reader, pos, nodes, logger, isTolerant);

			} else {
				
				throw ex;
			}
		}
	}

	// Read the graph description file
	public static Node[] readStructure(BufferedReader reader, Logger logger, boolean isTolerant)
			throws BadHeaderException, BadNodeException, BadEdgeException, IOException {
		
		StringPosition pos = new StringPosition();
		int cnt[] = new int[2];
		
		readHeader(cnt, reader, pos, logger, isTolerant);
		
		logger.info("Header of file parsed successfully" + System.lineSeparator()
			+ "  " + cnt[0] + " nodes" + System.lineSeparator()
			+ "  " + cnt[1] + " edges" + System.lineSeparator() + System.lineSeparator()
			+ "Reading nodes. Input format: [nodeID] [nodeID2] [latitude] [longitude] [elevation]");
		
		Node[] nodes = new Node[cnt[0]];
		Edge[] edges = new Edge[cnt[1]];
		
		for (int i = 0; i < cnt[0]; i++) {
			
			FmiNode t = readNode(reader, pos, nodes, logger, isTolerant);

			nodes[t.id()] = new Node(t.id(), t.latitude(), t.longitude(), new Edge[0]);
		}
		
		logger.info("Nodes parsed" + System.lineSeparator() + System.lineSeparator()
			+ "Reading edges. Input format: [srcID] [trgID] [cost] [type] [maxspeed]");
		
		for (int i = 0; i < cnt[1]; i++) {
			
			FmiEdge t = readEdge(reader, pos, nodes, logger, isTolerant);
			
			edges[i] = new Edge(nodes[t.srcId()], nodes[t.trgId()], t.cost());
			
			// Note: Inefficient if there are a lot of edges per node
			//         but reduces memory footprint
			Node src = nodes[t.srcId()];
			src.setEdges(Arrays.copyOf(src.edges(), src.edges().length + 1));
			src.edges()[src.edges().length - 1] = edges[i];
		}
		
		logger.info("Edges parsed" + System.lineSeparator());
		
		logger.info("Adjacency graph created");	

		return nodes;
	}
	
	// Read a target ID (request for one-to-many)
	public static int readTarget(BufferedReader reader, StringPosition pos,
			int maxId, Logger logger, boolean isTolerant) throws BadRequestException, IOException {
		
		try {
			
			getNextRelevantLine(pos, reader);
			
			if (pos.string == null)
				return -1;
			
			int trgId;
			int end = skipInteger(pos.string, pos.index);
			try {
				trgId = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadRequestException("No trgID provided [uint]");
			}
			
			end = skipWhitespace(pos.string, end);
			if (end < pos.string.length())
				throw new BadRequestException("Unexpected data in line");
			
			if (trgId < 0 || trgId >= maxId)
				throw new BadRequestException("trgID out of range");
			
			return trgId;

		} catch (BadRequestException ex) {
			
			if (isTolerant) {
				
				logger.warning(System.lineSeparator() + ex.getMessage());
				logger.warning("Error in request. Try again...");
				
				return readTarget(reader, pos, maxId, logger, isTolerant);
				
			} else {
				
				throw ex;
			}
		}
	}
	
	// Read a single request
	public static int[] readRequest(BufferedReader reader, StringPosition pos,
			int maxId, Logger logger, boolean isTolerant)
				throws BadRequestException, IOException {
		
		try {
			
			getNextRelevantLine(pos, reader);

			if (pos.string == null)
				return null;
				
			int srcId;
			int end = skipInteger(pos.string, pos.index);
			try {
				srcId = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadRequestException("No srcID provided [uint]");
			}
			
			pos.index = skipWhitespace(pos.string, end);
			
			int trgId;
			end = skipInteger(pos.string, pos.index);
			try {
				trgId = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadRequestException("No trgID provided [uint]");
			}
			
			end = skipWhitespace(pos.string, end);
			if (end < pos.string.length())
				throw new BadRequestException("Unexpected data in line");	
			
			if (srcId < 0 || srcId >= maxId)
				throw new BadRequestException("srcID out of range");
			
			if (trgId < 0 || trgId >= maxId)
				throw new BadRequestException("trgID out of range");

			return new int[] { srcId, trgId };

		} catch (BadRequestException ex) {
			
			if (isTolerant) {
				
				logger.warning(System.lineSeparator() + ex.getMessage());
				logger.warning("Error in request. Try again...");
				
				return readRequest(reader, pos, maxId, logger, isTolerant);
				
			} else {
				
				throw ex;
			}
		}
	}
}
