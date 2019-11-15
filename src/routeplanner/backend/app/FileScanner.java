package routeplanner.backend.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Vector;

import routeplanner.backend.model.*;

public class FileScanner {
	
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
	
	private static void getNextRelevantLine(StringPosition pos, Scanner scanner) {
		
		while (scanner.hasNextLine()) {
			
			String line = scanner.nextLine();
			
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
	
	private static void readHeader(int[] cnt, Scanner scanner, StringPosition pos) {
		
		getNextRelevantLine(pos, scanner);
		if (pos.string == null) {
			throw null;
		}
			
		int end = skipInteger(pos.string, pos.index);
		cnt[0] = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
		
		end = skipWhitespace(pos.string, end);
		if (end < pos.string.length()) {
			throw null;
		}
		
		getNextRelevantLine(pos, scanner);
		if (pos.string == null) {
			throw null;
		}
		
		end = skipInteger(pos.string, pos.index);
		cnt[1] = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
		
		end = skipWhitespace(pos.string, end);
		if (end < pos.string.length()) {
			throw null;
		}
	}
	
	private static FmiNode readNode(Scanner scanner, StringPosition pos) {
		
		getNextRelevantLine(pos, scanner);
		if (pos.string == null) {
			throw null;
		}
		
		int end = skipInteger(pos.string, pos.index);
		int id = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
		
		pos.index = skipWhitespace(pos.string, end);

		end = skipInteger(pos.string, pos.index);
		if (pos.index == end) {
			throw null;
		}

		pos.index = skipWhitespace(pos.string, end);
		
		end = skipFloat(pos.string, pos.index);
		double latitude = Double.parseDouble(pos.string.substring(pos.index, end));
		
		pos.index = skipWhitespace(pos.string, end);
		
		end = skipFloat(pos.string, pos.index);
		double longitude = Double.parseDouble(pos.string.substring(pos.index, end));
		
		pos.index = skipWhitespace(pos.string, end);

		end = skipFloat(pos.string, pos.index);
		if (pos.index == end) {
			throw null;
		}

		end = skipWhitespace(pos.string, end);
		if (end < pos.string.length()) {
			throw null;
		}
		
		return new FmiNode(id, latitude, longitude);
	}
	
	private static FmiEdge readEdge(Scanner scanner, StringPosition pos) {
		
		getNextRelevantLine(pos, scanner);
		if (pos.string == null) {
			throw null;
		}
		
		int end = skipInteger(pos.string, pos.index);
		int srcId = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
		
		pos.index = skipWhitespace(pos.string, end);
		
		end = skipInteger(pos.string, pos.index);
		int trgId = Integer.parseUnsignedInt(pos.string.substring(pos.index, end));
		
		pos.index = skipWhitespace(pos.string, end);
		
		end = skipFloat(pos.string, pos.index);
		double cost = Double.parseDouble(pos.string.substring(pos.index, end));
		
		pos.index = skipWhitespace(pos.string, end);
		
		end = skipInteger(pos.string, pos.index);
		if (pos.index == end) {
			throw null;
		}
		
		pos.index = skipWhitespace(pos.string, end);

		end = skipFloat(pos.string, pos.index);
		if (pos.index == end) {
			throw null;
		}
		
		end = skipWhitespace(pos.string, end);
		if (end < pos.string.length()) {
			throw null;
		}
		
		return new FmiEdge(srcId, trgId, cost);
	}

	public static Node[] read(String filename) throws FileNotFoundException {
		
		File file = new File(filename);

		Scanner scanner = new Scanner(file);
		
		StringPosition pos = new StringPosition();
		int cnt[] = new int[2];
		
		readHeader(cnt, scanner, pos);
		
		Node[] nodes = new Node[cnt[0]];
		Edge[] edges = new Edge[cnt[1]];
		
		Vector<LinkedList<Edge>> relations = new Vector<LinkedList<Edge>>(cnt[0]);
		
		for (int i = 0; i < cnt[0]; i++) {
			
			FmiNode t = readNode(scanner, pos);

			if (t.id() < 0 || t.id() >= cnt[0]) {
				throw null;
			}

			if (nodes[t.id()] != null) {
				throw null;
			}
			
			nodes[t.id()] = new Node(t.id(), t.latitude(), t.longitude());
			relations.add(new LinkedList<Edge>());
		}
		
		for (int i = 0; i < cnt[1]; i++) {
			
			FmiEdge t = readEdge(scanner, pos);
			
			if (t.srcId() < 0 || t.srcId() >= cnt[0]
					|| t.trgId() < 0 || t.trgId() >= cnt[0]) {
				throw null;
			}
			
			edges[i] = new Edge(nodes[t.srcId()], nodes[t.trgId()], t.cost());

			relations.get(t.srcId()).add(edges[i]);
		}
		
		for (int i = 0; i < cnt[0]; i++) {
			
			Edge[] e = new Edge[relations.get(i).size()];
			e = relations.get(i).toArray(e);

			nodes[i].setEdges(e);
		}
		
		scanner.close();
		
		return nodes;
	}
}
