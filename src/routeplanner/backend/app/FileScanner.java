package routeplanner.backend.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Vector;

import routeplanner.backend.model.*;

public class FileScanner {
	
	public static class BadHeaderException extends Exception {
		
		public BadHeaderException(String reason) { super(reason); }
	}
	
	public static class BadNodeException extends Exception {
		
		public BadNodeException(String reason) { super(reason); }
	}
	
	public static class BadEdgeException extends Exception {
		
		public BadEdgeException(String reason) { super(reason); }
	}
	
	public static class BadRequestException extends Exception {
		
		public BadRequestException(String reason) { super(reason); }
	}
	
	
	private static class StringPosition {
		String string = null;
		int index = -1;
	}
	
	private static int skipWhitespace(String str, int startIndex) {
		
		int i = startIndex;
		for (; i < str.length()
				&& Character.isWhitespace(str.charAt(i)); i++);
		return i;
	}
	
	private static int skipInteger(String str, int startIndex) {
		
		int i = startIndex;
		for (; i < str.length()
				&& Character.isDigit(str.charAt(i)); i++);
		return i;
	}
	
	private static int skipFloat(String str, int startIndex) {
		
		int i = skipInteger(str, startIndex);

		if (i < str.length() && str.charAt(i) == '.') {
			
			i = skipInteger(str, i + 1);
		}
		return i;
	}
	
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
	
	private static FmiNode readNode(BufferedReader reader, StringPosition pos,
			Node[] nodes, Logger logger, boolean isTolerant)
				throws BadNodeException, IOException {
		
		try {

			logger.info("Reading node");
			
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

	private static FmiEdge readEdge(BufferedReader reader, StringPosition pos,
			Node[] nodes, Logger logger, boolean isTolerant)
				throws BadEdgeException, IOException {
		
		try {

			logger.info("Reading edge");

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
			
			double cost;
			end = skipFloat(pos.string, pos.index);
			try {
				cost = Double.parseDouble(pos.string.substring(pos.index, end));
			} catch (NumberFormatException ex) {
				throw new BadEdgeException("No cost provided [double]");
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

	public static Node[] readStructure(BufferedReader reader, Logger logger, boolean isTolerant)
			throws BadHeaderException, BadNodeException, BadEdgeException, IOException {
		
		StringPosition pos = new StringPosition();
		int cnt[] = new int[2];
		
		readHeader(cnt, reader, pos, logger, isTolerant);
		
		logger.info("Header of file parsed successfully");
		logger.info("  " + cnt[0] + " nodes");
		logger.info("  " + cnt[1] + " edges" + System.lineSeparator());
		
		logger.info("Reading nodes. Input format: [nodeID] [nodeID2] [latitude] [longitude] [elevation]");
		
		Node[] nodes = new Node[cnt[0]];
		Edge[] edges = new Edge[cnt[1]];
		
		Vector<LinkedList<Edge>> relations = new Vector<LinkedList<Edge>>(cnt[0]);
		
		for (int i = 0; i < cnt[0]; i++) {
			
			logger.info(""); // Print new line
			
			FmiNode t = readNode(reader, pos, nodes, logger, isTolerant);

			nodes[t.id()] = new Node(t.id(), t.latitude(), t.longitude());
			relations.add(new LinkedList<Edge>());
		}
		
		logger.info("Nodes parsed" + System.lineSeparator());

		logger.info("Reading edges. Input format: [srcID] [trgID] [cost] [type] [maxspeed]");
		
		for (int i = 0; i < cnt[1]; i++) {
			
			logger.info(""); // Print new line

			FmiEdge t = readEdge(reader, pos, nodes, logger, isTolerant);
			
			edges[i] = new Edge(nodes[t.srcId()], nodes[t.trgId()], t.cost());

			relations.get(t.srcId()).add(edges[i]);
		}
		
		logger.info("Edges parsed" + System.lineSeparator());
		
		for (int i = 0; i < cnt[0]; i++) {
			
			Edge[] e = new Edge[relations.get(i).size()];
			e = relations.get(i).toArray(e);

			nodes[i].setEdges(e);
		}
		
		logger.info("Adjacency graph created");	

		return nodes;
	}
	
	public static int[] readRequest(BufferedReader reader, int maxId, Logger logger, boolean isTolerant)
				throws BadRequestException, IOException {
		
		try {
			
			logger.info("Reading request");

			StringPosition pos = new StringPosition();

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
				
				return readRequest(reader, maxId, logger, isTolerant);
				
			} else {
				
				throw ex;
			}
		}
	}
}
