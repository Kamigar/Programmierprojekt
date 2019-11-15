package routeplanner.backend.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import routeplanner.backend.model.fmi.*;

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
	
	private static Node readNode(Scanner scanner, StringPosition pos) {
		
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
		
		return new Node(id, latitude, longitude);
	}
	
	private static Edge readEdge(Scanner scanner, StringPosition pos) {
		
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
		
		return new Edge(srcId, trgId, cost);
	}

	public static routeplanner.backend.model.Node[] read(String filename) throws FileNotFoundException {
		
		File file = new File(filename);

		Scanner scanner = new Scanner(file);
		
		StringPosition pos = new StringPosition();
		int cnt[] = new int[2];
		
		readHeader(cnt, scanner, pos);
		
		Node[] nodes = new Node[cnt[0]];
		Edge[] edges = new Edge[cnt[1]];
		
		for (int i = 0; i < cnt[0]; i++) {
			
			Node t = readNode(scanner, pos);
			if (t.id() < 0 || t.id() >= cnt[0]) {
				throw null;
			}
			if (nodes[t.id()] != null) {
				throw null;
			}
			
			nodes[t.id()] = t;
		}
		
		for (int i = 0; i < cnt[1]; i++) {
			
			edges[i] = readEdge(scanner, pos);
		}
		
		
		scanner.close();
		
		return null;
	}
}
