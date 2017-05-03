package com.mdi.numericalPattern.polygons.utils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Point2D {
	public final static BigDecimal TWO = new BigDecimal(2);
	public final BigDecimal x, y;

	public Point2D(BigDecimal x, BigDecimal y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(x.intValue()) + Integer.hashCode(y.intValue()) + 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point2D other = (Point2D) obj;
		return other.x.compareTo(x) == 0 && other.y.compareTo(y) == 0;
	}

	public BigDecimal sqrDistance(Point2D other) {
		BigDecimal dx = x.subtract(other.x), dy = y.subtract(other.y);
		return dx.multiply(dx).add(dy.multiply(dy));
	}

	public double distance(Point2D other) {
		return Math.sqrt(sqrDistance(other).doubleValue());
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double distance(BigDecimal[] p1, BigDecimal[] p2) {
		return Math.sqrt(p2[0].subtract(p1[0]).multiply(p2[0].subtract(p1[0]))
				.add(p2[1].subtract(p1[1]).multiply(p2[1].subtract(p1[1]))).doubleValue());
	}

	/**
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return return 2 * signed area (multiplied by 2). twiceSignedArea < 0 iff
	 *         the angle between the vector p1p2 and p1p3 is negative.
	 *         twiceSignedArea == 0 iff p1,p2 and p3 are collinear.
	 *         twiceSignedArea > 0 iff the angle between the vector p1p2 and
	 *         p1p3 is positive
	 */
	public static BigDecimal twiceSignedArea(Point2D p1, Point2D p2, Point2D p3) {
		return p1.y.multiply(p3.x.subtract(p2.x)).add(p2.y.multiply(p1.x.subtract(p3.x)))
				.add(p3.y.multiply(p2.x.subtract(p1.x)));
	}

	/**
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return
	 */
	public static BigDecimal signedArea(BigDecimal[] p1, BigDecimal[] p2, BigDecimal[] p3) {
		return p1[1].multiply(p3[0].subtract(p2[0])).add(p2[1].multiply(p1[0].subtract(p3[0])))
				.add(p3[1].multiply(p2[0].subtract(p1[0]))).divide(TWO);
	}

	/**
	 * Polygone area using shoealce formula
	 * 
	 * @param points
	 * @return
	 */
	public static BigDecimal polygoneArea(Point2D... points) {
		return polygoneArea(Arrays.asList(points));
	}

	/**
	 * list of points forming a polygon.
	 * 
	 * @param points
	 * @return
	 */
	public static BigDecimal polygoneArea(List<Point2D> points) {
		BigDecimal area = new BigDecimal(0);
		int size = points.size();
		if (size > 2) {
			Point2D currentPoint = null, nextPoint = points.get(0);
			for (int i = 1; i < size; i++) {
				currentPoint = nextPoint;
				nextPoint = points.get(i);
				area = area.add(currentPoint.x.multiply(nextPoint.y).subtract(currentPoint.y.multiply(nextPoint.x)));
			}

			currentPoint = nextPoint;
			nextPoint = points.get(0);
			area = area.add(currentPoint.x.multiply(nextPoint.y).subtract(currentPoint.y.multiply(nextPoint.x)));
			area = area.divide(new BigDecimal(2));
		}
		return area.abs();
	}

	/**
	 * list of points forming a polygon.
	 * 
	 * @param points
	 * @return
	 */
	public static BigDecimal polygoneArea(BigDecimal[][] points) {
		BigDecimal area = new BigDecimal(0);
		int size = points.length;
		if (size > 2) {
			BigDecimal[] currentPoint = null, nextPoint = points[0];
			for (int i = 1; i < size; i++) {
				currentPoint = nextPoint;
				nextPoint = points[i];
				area = area
						.add(currentPoint[0].multiply(nextPoint[1]).subtract(currentPoint[1].multiply(nextPoint[0])));
			}

			currentPoint = nextPoint;
			nextPoint = points[0];
			area = area.add(currentPoint[0].multiply(nextPoint[1]).subtract(currentPoint[1].multiply(nextPoint[0])));
			area = area.divide(TWO);
		}
		return area.abs();
	}

	/**
	 * 
	 * @param points
	 * @return
	 */
	public static double polygonePerimeter(BigDecimal[][] points) {
		double perimeter = 0;
		int size = points.length;
		if (size < 2) {
			return perimeter;
		}
		if (size == 2) {
			perimeter = distance(points[0], points[1]);
			return perimeter;
		}

		BigDecimal[] currentPoint = null, nextPoint = points[0];
		for (int i = 1; i < size; i++) {
			currentPoint = nextPoint;
			nextPoint = points[i];
			perimeter += distance(currentPoint, nextPoint);
		}

		currentPoint = nextPoint;
		nextPoint = points[0];
		perimeter += distance(currentPoint, nextPoint);

		return perimeter;
	}

	/**
	 * 
	 * @param p2
	 * @param p1
	 * @param p3
	 * @return the cos of the oriented angle between vectors p1p2 and p1p3
	 *         signum
	 */
	public static int cosSignum(Point2D p1, Point2D p2, Point2D p3) {
		BigDecimal p1p2x = p2.x.subtract(p1.x);
		BigDecimal p1p2y = p2.y.subtract(p1.y);

		BigDecimal p1p3x = p3.x.subtract(p1.x);
		BigDecimal p1p3y = p3.y.subtract(p1.y);

		if (p1p2x.signum() == 0 && p1p2y.signum() == 0) {
			throw new IllegalArgumentException("p1p2 doesn't form a vector");
		}

		if (p1p3x.signum() == 0 && p1p3y.signum() == 0) {
			throw new IllegalArgumentException("p2p3 doesn't form a vector");
		}

		return p1p2x.multiply(p1p3x).add(p1p2y.multiply(p1p3y)).signum();
	}

	/**
	 * 
	 * @param p2
	 * @param p1
	 * @param p3
	 * @return the sin of the oriented angle between vectors p1p2 and p1p3
	 *         signum
	 */
	public static int sinSignum(Point2D p1, Point2D p2, Point2D p3) {
		BigDecimal p1p2x = p2.x.subtract(p1.x);
		BigDecimal p1p2y = p2.y.subtract(p1.y);

		BigDecimal p1p3x = p3.x.subtract(p1.x);
		BigDecimal p1p3y = p3.y.subtract(p1.y);

		if (p1p2x.signum() == 0 && p1p2y.signum() == 0) {
			throw new IllegalArgumentException("p1p2 doesn't form a vector");
		}

		if (p1p3x.signum() == 0 && p1p3y.signum() == 0) {
			throw new IllegalArgumentException("p2p3 doesn't form a vector");
		}

		return p1p2x.multiply(p1p3y).subtract(p1p2y.multiply(p1p3x)).signum();
	}

	/**
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return 0 if angle = 0; 1 if angle is in ]0,pi/2[; 2 if angle is = pi/2;
	 *         3 if angle is in ]pi/2,pi[; 4 if angle is = pi -1 if angle is in
	 *         ]-pi/2,0[; -2 if angle is = - pi/2; -3 if angle is in ]-pi,-pi/2[
	 */
	public static int anglePosition(Point2D p1, Point2D p2, Point2D p3) {
		BigDecimal p1p2x = p2.x.subtract(p1.x);
		BigDecimal p1p2y = p2.y.subtract(p1.y);

		BigDecimal p1p3x = p3.x.subtract(p1.x);
		BigDecimal p1p3y = p3.y.subtract(p1.y);

		if (p1p2x.signum() == 0 && p1p2y.signum() == 0) {
			throw new IllegalArgumentException("p1p2 doesn't form a vector");
		}

		if (p1p3x.signum() == 0 && p1p3y.signum() == 0) {
			throw new IllegalArgumentException("p2p3 doesn't form a vector");
		}

		int cosSignum = p1p2x.multiply(p1p3x).add(p1p2y.multiply(p1p3y)).signum();
		int sinSignum = p1p2x.multiply(p1p3y).subtract(p1p2y.multiply(p1p3x)).signum();

		if (cosSignum == 1 && sinSignum == 0)
			return 0;
		if (cosSignum == 1 && sinSignum == 1)
			return 1;
		if (cosSignum == 0 && sinSignum == 1)
			return 2;
		if (cosSignum == -1 && sinSignum == 1)
			return 3;
		if (cosSignum == -1 && sinSignum == 0)
			return 4;
		if (cosSignum == 1 && sinSignum == -1)
			return -1;
		if (cosSignum == 0 && sinSignum == -1)
			return -2;
		return -3;
	}

	// http://mathworld.wolfram.com/Circumcircle.html
	/**
	 * @param a
	 * @param b
	 * @param c
	 * @param other
	 * @return 0 if other is in border of circumcircle(a,b,c), -1 if other is
	 *         inside the circle, 1 if outside
	 */
	public static int circumcircleTestWithRadius(Point2D a, Point2D b, Point2D c, Point2D other) {
		BigDecimal diffYbc = b.y.subtract(c.y);
		BigDecimal diffYca = c.y.subtract(a.y);
		BigDecimal diffYab = a.y.subtract(b.y);

		BigDecimal alpha = a.x.multiply(diffYbc).add(b.x.multiply(diffYca)).add(c.x.multiply(diffYab));
		BigDecimal twoAlpha = alpha.multiply(new BigDecimal(2));

		if (alpha.signum() == 0) {
			throw new IllegalArgumentException("The three points are collinear");
		}

		BigDecimal aSquare = a.x.multiply(a.x).add(a.y.multiply(a.y));
		BigDecimal bSquare = b.x.multiply(b.x).add(b.y.multiply(b.y));
		BigDecimal cSquare = c.x.multiply(c.x).add(c.y.multiply(c.y));

		// Need to be divided (2 * alpha);
		BigDecimal circumcenterX = (aSquare.multiply(diffYbc).add(bSquare.multiply(diffYca))
				.add(cSquare.multiply(diffYab)));
		BigDecimal circumcenterY = (aSquare.multiply(b.x.subtract(c.x)).add(bSquare.multiply(c.x.subtract(a.x)))
				.add(cSquare.multiply(a.x.subtract(b.x)))).negate();

		BigDecimal translateAX = a.x.multiply(twoAlpha);
		BigDecimal translateAY = a.y.multiply(twoAlpha);

		BigDecimal translateOtherX = other.x.multiply(twoAlpha);
		BigDecimal translateOtherY = other.y.multiply(twoAlpha);

		BigDecimal radiusX = circumcenterX.subtract(translateAX);
		BigDecimal radiusY = circumcenterY.subtract(translateAY);
		BigDecimal distX = circumcenterX.subtract(translateOtherX);
		BigDecimal distY = circumcenterY.subtract(translateOtherY);

		BigDecimal sqrRadius = radiusX.multiply(radiusX).add(radiusY.multiply(radiusY));
		BigDecimal sqrDist = distX.multiply(distX).add(distY.multiply(distY));

		return sqrDist.compareTo(sqrRadius);
	}

	/**
	 * @param a
	 * @param b
	 * @param c
	 * @param other
	 * @return 0 if other is in border of circumcircle(a,b,c), -1 if other is
	 *         inside the circle, 1 if outside
	 */
	public static int circumcircleTestWithDeterminant(Point2D a, Point2D b, Point2D c, Point2D o) {
		int orientation = b.x.multiply(c.y).subtract(b.y.multiply(c.x))
				.add(a.x.multiply(b.y).subtract(a.y.multiply(b.x)))
				.compareTo(a.x.multiply(c.y).subtract(a.y.multiply(c.x)));
		if (orientation == 0) {
			throw new IllegalArgumentException("The three points are collinear");
		}
		BigDecimal a2 = a.x.multiply(a.x).add(a.y.multiply(a.y)), b2 = b.x.multiply(b.x).add(b.y.multiply(b.y)),
				c2 = c.x.multiply(c.x).add(c.y.multiply(c.y)), o2 = o.x.multiply(o.x).add(o.y.multiply(o.y));

		int inCircle = a2.multiply(c.x.multiply(o.y).subtract(c.y.multiply(o.x)))
				.subtract(c2.multiply(a.x.multiply(o.y).subtract(a.y.multiply(o.x))))
				.add(o2.multiply(a.x.multiply(c.y).subtract(a.y.multiply(c.x))))
				.add(a2.multiply(b.x.multiply(c.y).subtract(b.y.multiply(c.x)))
						.subtract(b2.multiply(a.x.multiply(c.y).subtract(a.y.multiply(c.x))))
						.add(c2.multiply(a.x.multiply(b.y).subtract(a.y.multiply(b.x)))))
				.compareTo(b2.multiply(c.x.multiply(o.y).subtract(c.y.multiply(o.x)))
						.subtract(c2.multiply(b.x.multiply(o.y).subtract(b.y.multiply(o.x))))
						.add(o2.multiply(b.x.multiply(c.y).subtract(b.y.multiply(c.x))))
						.add(a2.multiply(b.x.multiply(o.y).subtract(b.y.multiply(o.x)))
								.subtract(b2.multiply(a.x.multiply(o.y).subtract(a.y.multiply(o.x))))
								.add(o2.multiply(a.x.multiply(b.y).subtract(a.y.multiply(b.x))))));

		return -inCircle * orientation;
	}

	public static class LeftToRightComparator implements Comparator<Point2D> {
		public int compare(Point2D o1, Point2D o2) {
			int cmp = o1.x.compareTo(o2.x);
			if (cmp != 0)
				return cmp;
			return o1.y.compareTo(o2.y);
		}
	}

	public static class DownToUpComparator implements Comparator<Point2D> {
		public int compare(Point2D o1, Point2D o2) {
			int cmp = o1.y.compareTo(o2.y);
			if (cmp != 0)
				return cmp;
			return o1.x.compareTo(o2.x);
		}
	}

	public static class UpToDownComparator implements Comparator<Point2D> {
		public int compare(Point2D o1, Point2D o2) {
			int cmp = -o1.y.compareTo(o2.y);
			if (cmp != 0)
				return cmp;
			return o1.x.compareTo(o2.x);
		}
	}

	public static class AngleComparator implements Comparator<Point2D> {
		private final Point2D baseSegmentA;
		private final Point2D baseSegmentB;
		private final BigDecimal baseSegmentX;
		private final BigDecimal baseSegmentY;

		public AngleComparator(Point2D baseSegmentA, Point2D baseSegmentB) {
			this.baseSegmentA = baseSegmentA;
			this.baseSegmentB = baseSegmentB;
			this.baseSegmentX = baseSegmentB.x.subtract(baseSegmentA.x);
			this.baseSegmentY = baseSegmentB.y.subtract(baseSegmentA.y);
			if (this.baseSegmentX.signum() == 0 && this.baseSegmentY.signum() == 0) {
				throw new RuntimeException("The two points doesn't form a segment");
			}
		}

		/**
		 * Compare angle between formed between the angles : baseSegmentA,
		 * baseSegmentB ; baseSegmentA, o1 and baseSegmentA, baseSegmentB ;
		 * baseSegmentA, o2
		 */
		@Override
		public int compare(Point2D o1, Point2D o2) {
			BigDecimal ao1x = o1.x.subtract(baseSegmentA.x);
			BigDecimal ao1y = o1.y.subtract(baseSegmentA.y);
			BigDecimal ao2x = o2.x.subtract(baseSegmentA.x);
			BigDecimal ao2y = o2.y.subtract(baseSegmentA.y);
			BigDecimal ao1SizeSquare = ao1x.multiply(ao1x).add(ao1y.multiply(ao1y));
			BigDecimal ao2SizeSquare = ao2x.multiply(ao2x).add(ao2y.multiply(ao2y));

			if (ao1x.signum() == 0 && ao1y.signum() == 0) {
				throw new IllegalArgumentException("p2p3 doesn't form a vector");
			}

			if (ao2x.signum() == 0 && ao2y.signum() == 0) {
				throw new IllegalArgumentException("p2p3 doesn't form a vector");
			}

			Integer o1pos = anglePosition(baseSegmentA, baseSegmentB, o1);
			Integer o2pos = anglePosition(baseSegmentA, baseSegmentB, o2);

			if (!o1pos.equals(o2pos))
				return o1pos.compareTo(o2pos);
			if (o1pos % 2 == 0)
				return 0;

			BigDecimal o1scalar = baseSegmentX.multiply(ao1x).add(baseSegmentY.multiply(ao1y));
			BigDecimal o2scalar = baseSegmentX.multiply(ao2x).add(baseSegmentY.multiply(ao2y));

			BigDecimal o1scalarSquare = o1scalar.multiply(o1scalar);
			BigDecimal o2scalarSquare = o2scalar.multiply(o2scalar);

			int invert = (o1pos == -3 || o1pos == 1) ? -1 : 1;
			int compared = o1scalarSquare.multiply(ao2SizeSquare).compareTo(o2scalarSquare.multiply(ao1SizeSquare));

			return compared * invert;
		}
	}
}
