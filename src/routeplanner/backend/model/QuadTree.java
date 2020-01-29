package routeplanner.backend.model;

//prioqueue maybe
import java.util.*;

import com.sun.media.jfxmedia.logging.Logger;

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
			return (Double.compare(l, x) <= 0 && Double.compare(x, r) <= 0)
					&& (Double.compare(b, y) <= 0 && Double.compare(y, t) <= 0);
		}

		public boolean intersects(Rectangle query) {
			return !(Double.compare(query.l, r) > 0 || Double.compare(query.r, l) < 0 || Double.compare(query.b, t) > 0
					|| Double.compare(query.t, b) < 0);
		}
	}

	public static class Point {
		public double x, y;
		private int id;

		public Point(double x, double y, int id) {
			this.id = id;
			this.x = x;
			this.y = y;
		}

		public double distance(double x1, double y1) {
			return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
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

		private Node(Rectangle rect, Node parent) {
			this.rect = rect;
			this.parent = parent;
		}

		private boolean add(Point p) {
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

		private int nearestNeighbour(double x1, double y1) {
			Node quadrand = findQuadrand(x1, y1);
			int result;
			double distance;
			Rectangle query;
			double cdistance;
			if (quadrand.hasPoint) {
				// calculate distance to the point in the quadrant
				result = quadrand.point.getID();
				distance = quadrand.point.distance(x1, y1);
				query = new Rectangle(x1 - distance, x1 + distance, y1 - distance, y1 + distance);

			} else {
				quadrand = quadrand.parent;
				result = quadrand.point.getID();
				distance = quadrand.point.distance(x1, y1);
				// we might create a circle of radius d(x1,y1) for better performance when
				// needed
				query = new Rectangle(x1 - distance, x1 + distance, y1 - distance, y1 + distance);
			}
			ArrayList<Point> candidates = new ArrayList<>();
			findPoints(query, candidates);
			for (Point candidate : candidates) {
				cdistance = candidate.distance(x1, y1);
				if (Double.compare(cdistance, distance) < 0) {
					result = candidate.getID();
					distance = cdistance;
				}

			}
			return result;
		}

		// find all the points that the query rectangle contains
		private void findPoints(Rectangle query, ArrayList<Point> list) {
			if (rect != null) {
				if (hasPoint) {
					if (query.contains(point.x, point.y)) {
						list.add(point);
					}

				}
			}
			if (bl != null) {
				if (query.intersects(bl.rect)) {
					bl.findPoints(query, list);
				}
			}
			if (br != null) {
				if (query.intersects(br.rect)) {
					br.findPoints(query, list);
				}
			}
			if (tl != null) {
				if (query.intersects(tl.rect)) {
					tl.findPoints(query, list);
				}
			}
			if (tr != null) {
				if (query.intersects(tr.rect)) {
					tr.findPoints(query, list);
				}
			}
		}

		// find the node that contains the requested coordinates and only has one or
		// zero points in it.
		private Node findQuadrand(double x1, double y1) {

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

	public boolean add(Point p) {
		return root.add(p);
	}

	public int nearestNeighbour(double x1, double y1) {
		return root.nearestNeighbour(x1, y1);
	}

	// only for testing certain stuff
	public static void main(String[] args) {
	}
}
