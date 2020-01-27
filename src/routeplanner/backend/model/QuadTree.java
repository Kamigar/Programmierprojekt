package routeplanner.backend.model;
//prioqueue maybe
import java.util.*;

public class QuadTree {
	public Node root;

	public static class Rectangle {
		private double l, r, t, b;

		// constructor for rectangle left,right,bottom,top
		public Rectangle(double l, double r, double b, double t) {
			this.l = l;
			this.r = r;
			this.b = b;
			this.t = t;

		}

		// check if a point is within the bounding box of a rectangle
		public boolean contains(double x, double y) {
			return (l <= x && x <= r) && (b <= y && y <= t);
		}

		public boolean intersects(Rectangle query) {
			return !(query.l > r || query.r < l || query.b > t || query.t < b);
		}
	}

	public class Point {
		private double x, y;
		private int id;

		public Point(double x, double y, int id) {
			this.id = id;
			this.x = x;
			this.y = y;
		}

		public int getID() {
			return id;
		}
	}

	public class Node {
		private Point point;
		private Rectangle rect;
		private Node tl, tr, bl, br, parent;
		private boolean hasPoint;

		public Node(Rectangle rect, Node parent) {
			this.rect = rect;
		}

		public boolean add(Point p) {
			if (!rect.contains(p.x, p.y)) {
				return false;
			}
			if (!hasPoint) {
				hasPoint = true;
				point = p;
				return true;
			}
			// calculate the middle of the rectangle sides
			double mx = (rect.l + rect.r) / 2;
			double my = (rect.b + rect.t) / 2;
			if (bl == null) {
				bl = new Node(new Rectangle(rect.l, mx, rect.b, my), this);
			}
			if (bl.add(p)) {
				return true;
			}
			if (br == null) {
				br = new Node(new Rectangle(mx, rect.r, rect.b, my), this);
			}
			if (br.add(p)) {
				return true;
			}
			if (tl == null) {
				tl = new Node(new Rectangle(rect.l, mx, my, rect.t), this);
			}
			if (tl.add(p)) {
				return true;
			}
			if (tr == null) {
				tr = new Node(new Rectangle(mx, rect.r, my, rect.t), this);
			}
			return tr.add(p);

		}

		public int nearestNeighbour(double x1, double y1) {
			Node quadrand = findQuadrand(x1,y1);
			if(quadrand.point != null) {
				//calculate distance to the point in the quadrant
				int result = quadrand.point.id;
				Double distance = Math.sqrt((quadrand.point.x - x1)*(quadrand.point.x - x1)+(quadrand.point.y - y1)*(quadrand.point.y - y1));
				Rectangle query = new Rectangle(x1-distance, x1+distance,y1-distance,y1+distance);
				//hier muss noch der fall dafür rein wenn der quadrand leer is ( mit parent) und wie man alle punkte findet die in der query drin sind
			}
			return 0;
		}
		
		// find the node that contains the requested coordinates and only has one or zero points in it.
		public Node findQuadrand(double x1, double y1) {

			if (bl != null) {
				if (bl.rect.contains(x1, y1)) {
					return bl.findQuadrand(x1, y1);
				}
			}
			if (br != null) {
				if (br.rect.contains(x1, y1)) {
					return br.findQuadrand(x1, y1);
				}
			}
			if (tl != null) {
				if (tl.rect.contains(x1, y1)) {
					return tl.findQuadrand(x1, y1);
				}
			}
			if (tr != null) {
				if (tr.rect.contains(x1, y1)) {
					return tr.findQuadrand(x1, y1);
				}
			}
			return this;
		}
	}

	/*
	 * Für den germany.fmi graphen: maxLat: 55.052937500000006minLat:
	 * 47.284206700000006maxLong: 15.0834433minLong: 5.8630797 ----- Creating a
	 * QuadTree with the root having no parent
	 */
	public QuadTree(Rectangle rect) {
		root = new Node(rect, null);
	}


	// only for testing certain stuff
	public static void main(String[] args) {
	}
}
