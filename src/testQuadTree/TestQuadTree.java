package testQuadTree;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import routeplanner.backend.model.Node;
import routeplanner.backend.model.QuadTree;
import routeplanner.backend.app.Main;
import routeplanner.backend.app.Logger;
import routeplanner.backend.app.Main.Parameters;
import routeplanner.backend.app.FileScanner;
import routeplanner.backend.model.QuadTree.*;

public class TestQuadTree {
	//initialize variables needed for the unit test
	Parameters param = new Parameters();
	Logger logger = new Logger();
	Node[] nodes = null;
	String[] args = new String[4];
	double minLat, maxLat, minLong, maxLong;
	double timeI[] = new double[100];
	double timeQT[] = new double[100];
	double averageI = 0;
	double averageQT = 0;
	double timeIn;
	double timeQuad;

	@Before
	public void beforeTest() {
		try {
			args[0] = "-v";
			args[1] = "-t";
			args[2] = "-i";
			// input the graph txtfile location
			args[3] = "C:\\Users\\kamig\\Desktop\\bw.fmi";
			param = Main.readParameters(args);
			logger = new Logger(param.logLevel, param.logOut);
			nodes = FileScanner.readStructure(param.structureIn, logger, param.isTolerant);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws IOException {
		minLat = Double.POSITIVE_INFINITY;
		minLong = Double.POSITIVE_INFINITY;
		maxLat = Double.NEGATIVE_INFINITY;
		maxLong = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < nodes.length; i++) {
			double lat = nodes[i].latitude();
			double lng = nodes[i].longitude();
			if (Double.compare(lat, minLat) < 0) {
				minLat = lat;
			}
			if (Double.compare(lat, maxLat) > 0) {
				maxLat = lat;
			}
			if (Double.compare(lng, minLong) < 0) {
				minLong = lng;
			}
			if (Double.compare(lng, maxLong) > 0) {
				maxLong = lng;
			}
		}
		// create quadtree
		double startTime;
		double endTime;
		startTime = System.nanoTime();
		QuadTree tree = new QuadTree(new Rectangle(minLat, maxLat, minLong, maxLong));
		for (int i = 0; i < nodes.length; i++) {
			Point p = new Point(nodes[i].latitude(), nodes[i].longitude(), nodes[i].id());
			tree.add(p);
		}
		endTime = System.nanoTime();
		logger.info("Creating the QuadTree took " + (endTime - startTime) / 1000000 + " milliseconds ");
		Point points[] = new Point[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			points[i] = new Point(nodes[i].latitude(), nodes[i].longitude(), nodes[i].id());
		}
		for (int i = 0; i < 100; i++) {
			double x = Math.random() * (maxLat - minLat) + minLat;
			double y = Math.random() * (maxLong - minLong) + minLong;
			int nearestNeighbour = 0;
			int nearestNeighbourQT = 0;
			double distance = Double.POSITIVE_INFINITY;
			double cdistance;
			// naive method:
			startTime = System.nanoTime();
			for (Point point : points) {
				cdistance = (point.x - x) * (point.x - x) + (point.y - y) * (point.y - y);
				if (Double.compare(cdistance, distance) < 0) {
					distance = cdistance;
					nearestNeighbour = point.getID();
				}
			}
			endTime = System.nanoTime();
			timeIn = endTime - startTime;
			timeI[i] = timeIn;
			// QuadTree implementation
			startTime = System.nanoTime();
			nearestNeighbourQT = tree.nearestNeighbour(x, y);
			endTime = System.nanoTime();
			timeQuad = endTime - startTime;
			timeQT[i] = timeQuad;
			logger.info("times are " +timeQuad/1000000+ " " + timeIn/1000000 );
			// check if the nearest neighbour calculated by the quad tree is the actual one
			assertEquals(nearestNeighbourQT,nearestNeighbour);
			
		}
		for (int i = 0; i < 100; i++) {
			averageI += timeI[i];
			averageQT += timeQT[i];
		}
		averageI /= 100000000;
		averageQT /= 100000000;
		assertTrue(Double.compare(averageQT, averageI) < 0);
		logger.info("The average time to find the nearest neighbour with sequential search is: " + averageI
				+ " milliseconds." + System.lineSeparator() + "The average time to find the nearest neighbour with a QuadTree is: " + averageQT + " milliseconds");
	}
}



