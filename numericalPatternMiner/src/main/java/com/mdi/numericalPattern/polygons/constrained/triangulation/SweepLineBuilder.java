package com.mdi.numericalPattern.polygons.constrained.triangulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.mdi.numericalPattern.polygons.utils.Point2D;



public class SweepLineBuilder implements TriangulationBuilder {
	private Triangulation triangulation;

	private TreeSet<Point2D> advancingFront;
	private TreeSet<Point2D> leftBorderCollinearPoints;
	private TreeSet<Point2D> rightBorderCollinearPoints;

	@Override
	public TriangulationBuilder set(Set<Point2D> points) {
		this.triangulation = new Triangulation(points);
		return this;
	}

	/**
	 * Don't handle yet:
	 * 
	 * 1 - Almost collinear points (w.r.t error)
	 * 
	 * 2 - Almost equals or equals points (w.r.t error)
	 * 
	 * 3 - Basins
	 */
	@Override
	public Triangulation build() {
		advancingFront = new TreeSet<>(new Point2D.LeftToRightComparator());
		leftBorderCollinearPoints = new TreeSet<>(new Point2D.UpToDownComparator());
		rightBorderCollinearPoints = new TreeSet<>(new Point2D.UpToDownComparator());
		
		ArrayList<Point2D> sortedPoints = new ArrayList<>(triangulation.graph.keySet());
		Collections.sort(sortedPoints, new Point2D.DownToUpComparator());
		int size = sortedPoints.size();
		int currentIndex = initialize(sortedPoints);
		if (size > 3) {
			while (currentIndex < size) {
				handleNext(sortedPoints.get(currentIndex));
				currentIndex++;
			}

		}
		triangulation.complete(advancingFront, 0);
		
		advancingFront = null;
		leftBorderCollinearPoints = null;
		rightBorderCollinearPoints = null;

		return triangulation;
	}

	/**
	 * 
	 * @return the number of consumed points
	 */
	private int initialize(ArrayList<Point2D> sortedPoints) {
		int size = sortedPoints.size();
		if (size == 0)
			return 0;

		if (size == 1) {
			Point2D a = sortedPoints.get(0);
			triangulation.extremePointsSequence.add(a);
			return 1;
		}

		if (size == 2) {
			Point2D a = sortedPoints.get(0);
			Point2D b = sortedPoints.get(1);

			if (a.x.compareTo(b.x)==-1) {
				triangulation.extremePointsSequence.add(a);
				triangulation.extremePointsSequence.add(b);
			} else {
				triangulation.extremePointsSequence.add(b);
				triangulation.extremePointsSequence.add(a);
			}

			triangulation.addEdge(a, b);

			return 2;
		}

		int consumed = 2;

		Point2D a = sortedPoints.get(0);
		Point2D b = sortedPoints.get(1);
		Point2D candidate;
		List<Point2D> middleCollinearPoints = new ArrayList<Point2D>();
		for (int i = 2; i < size; i++) {
			candidate = sortedPoints.get(i);
			if (Point2D.twiceSignedArea(a, b, candidate).signum()!=0)
				break;
			middleCollinearPoints.add(candidate);
			consumed += 1;
		}

		if (!middleCollinearPoints.isEmpty()) {
			middleCollinearPoints.add(0, b);
			b = middleCollinearPoints.remove(middleCollinearPoints.size() - 1);
		}
		if (consumed == size) {
			if (a.x.compareTo(b.x)==-1) {
				triangulation.extremePointsSequence.add(a);
				triangulation.extremePointsSequence.add(b);
			} else {
				triangulation.extremePointsSequence.add(b);
				triangulation.extremePointsSequence.add(a);
			}

			Point2D currentPoint, nextPoint = a;
			for (int i = 0; i < middleCollinearPoints.size(); i++) {
				currentPoint = nextPoint;
				nextPoint = middleCollinearPoints.get(i);
				triangulation.addEdge(currentPoint, nextPoint);
			}
			currentPoint = nextPoint;
			nextPoint = b;
			triangulation.addEdge(currentPoint, nextPoint);

			return consumed;
		}
		Point2D c = sortedPoints.get(consumed++);

		// Create triangles
		Point2D currentPoint, nextPoint = a;
		for (int i = 0; i < middleCollinearPoints.size(); i++) {
			currentPoint = nextPoint;
			nextPoint = middleCollinearPoints.get(i);
			triangulation.addTriangle(c, currentPoint, nextPoint);
		}
		currentPoint = nextPoint;
		nextPoint = b;
		triangulation.addTriangle(c, currentPoint, nextPoint);

		// Preparing advancing Front and lower convex Hull
		advancingFront.add(c);
		triangulation.extremePointsSequence.add(a);

		if (a.x == b.x) {
			advancingFront.add(b);
			if (c.x.compareTo(a.x)<=0) {
				triangulation.extremePointsSequence.add(0, c);
			} else {
				triangulation.extremePointsSequence.add(c);
			}
		} else if (a.x.compareTo(b.x)==-1) {
			if (c.x.compareTo(a.x)<=0) {
				advancingFront.add(b);
				triangulation.extremePointsSequence.add(b);
				triangulation.extremePointsSequence.add(0, c);
			} else if (c.x.compareTo(b.x)==-1) {
				advancingFront.add(a);
				advancingFront.add(b);
				triangulation.extremePointsSequence.add(b);
			} else if (c.x == b.x) {
				advancingFront.add(a);
				triangulation.extremePointsSequence.add(b);
				triangulation.extremePointsSequence.add(c);
			} else {
				advancingFront.add(a);
				if (Point2D.twiceSignedArea(b, a, c).signum()==1) {
					advancingFront.add(b);
					for (Point2D p : middleCollinearPoints) {
						advancingFront.add(p);
					}
				} else {
					triangulation.extremePointsSequence.add(b);
				}
				triangulation.extremePointsSequence.add(c);
			}
		} else {
			if (c.x.compareTo(a.x)>=0) {
				advancingFront.add(b);
				triangulation.extremePointsSequence.add(0, b);
				triangulation.extremePointsSequence.add(c);
			} else if (c.x.compareTo(b.x)==1) {
				advancingFront.add(a);
				advancingFront.add(b);
				triangulation.extremePointsSequence.add(0, b);
			} else if (c.x.compareTo(b.x)==0) {
				advancingFront.add(a);
				triangulation.extremePointsSequence.add(0, b);
				triangulation.extremePointsSequence.add(0, c);
			} else {
				advancingFront.add(a);
				if (Point2D.twiceSignedArea(b, a, c).signum()==1) {
					triangulation.extremePointsSequence.add(0, b);
				} else {
					advancingFront.add(b);
					for (Point2D p : middleCollinearPoints) {
						advancingFront.add(p);
					}
				}
				triangulation.extremePointsSequence.add(0, c);
			}
		}
		return consumed;
	}

	private void handleNext(Point2D newPoint) {
		Point2D leftPoint = advancingFront.floor(newPoint), rightPoint = advancingFront.ceiling(newPoint);

		if (leftPoint != null && leftPoint.x.compareTo(newPoint.x)==0 && leftPoint == advancingFront.first()) {
			rightPoint = leftPoint;
			leftPoint = null;
		}

		if (leftPoint == null) {
			Point2D nextPoint = advancingFront.first();
			Point2D currentPoint = advancingFront.higher(nextPoint);
			int currentAnglePos = Point2D.anglePosition(newPoint, nextPoint, currentPoint);
			triangulation.extremePointsSequence.add(0, newPoint);
			advancingFront.add(newPoint);
			if (currentAnglePos>0) {
				triangulation.addTriangle(newPoint, currentPoint, nextPoint);
				triangulation.delaunayLegalizeFromEdge(currentPoint, nextPoint);
				advancingFront.remove(nextPoint);
				walkRight(newPoint, currentPoint);
			}
			while (triangulation.extremePointsSequence.size() > 2) {
				
				currentPoint = nextPoint;
				nextPoint = triangulation.extremePointsSequence.get(2);
				currentAnglePos = Point2D.anglePosition(newPoint, nextPoint, currentPoint);
				if (currentAnglePos<0) {
					break;
				}
				if (currentAnglePos>0) {
					Iterator<Point2D> it = leftBorderCollinearPoints.iterator();
					Point2D collinearPoint;
					while (it.hasNext()) {
						collinearPoint = it.next();
						triangulation.addTriangle(newPoint, currentPoint, collinearPoint);
						triangulation.delaunayLegalizeFromEdge(currentPoint, collinearPoint);
						it.remove();
						currentPoint = collinearPoint;
					}
					triangulation.addTriangle(newPoint, currentPoint, nextPoint);
					triangulation.delaunayLegalizeFromEdge(currentPoint, nextPoint);
				} else {
					leftBorderCollinearPoints.add(currentPoint);
				}
				triangulation.extremePointsSequence.remove(1);
			}

			return;
		}

		if (rightPoint == null) {
			Point2D nextPoint = advancingFront.last();
			Point2D currentPoint = advancingFront.lower(nextPoint);
			int currentAnglePos = Point2D.anglePosition(newPoint, currentPoint, nextPoint);

			triangulation.extremePointsSequence.add(triangulation.extremePointsSequence.size(), newPoint);
			advancingFront.add(newPoint);
			if (currentAnglePos > 0) {
				triangulation.addTriangle(newPoint, currentPoint, nextPoint);
				triangulation.delaunayLegalizeFromEdge(currentPoint, nextPoint);
				advancingFront.remove(nextPoint);
				walkLeft(newPoint, currentPoint);
			}

			while (triangulation.extremePointsSequence.size() > 2) {
				currentPoint = nextPoint;
				nextPoint = triangulation.extremePointsSequence.get(triangulation.extremePointsSequence.size() - 3);
				currentAnglePos = Point2D.anglePosition(newPoint, currentPoint, nextPoint);
				if (currentAnglePos < 0) {
					break;
				}
				if (currentAnglePos > 0) {
					Iterator<Point2D> it = rightBorderCollinearPoints.iterator();
					Point2D collinearPoint;
					while (it.hasNext()) {
						collinearPoint = it.next();
						triangulation.addTriangle(newPoint, currentPoint, collinearPoint);
						triangulation.delaunayLegalizeFromEdge(currentPoint, collinearPoint);
						it.remove();
						currentPoint = collinearPoint;
					}
					triangulation.addTriangle(newPoint, currentPoint, nextPoint);
					triangulation.delaunayLegalizeFromEdge(currentPoint, nextPoint);
				} else {
					rightBorderCollinearPoints.add(currentPoint);
				}
				triangulation.extremePointsSequence.remove(triangulation.extremePointsSequence.size() - 2);
			}

			return;
		}

		advancingFront.add(newPoint);
		triangulation.addTriangle(newPoint, leftPoint, rightPoint);
		triangulation.delaunayLegalizeFromEdge(leftPoint, rightPoint);
		walkRight(newPoint, rightPoint);
		walkLeft(newPoint, leftPoint);

		// TODO: Bassin case
	}

	private void walkRight(Point2D newPoint, Point2D startingRightPoint) {
		boolean stop = false;
		Point2D rightPoint;
		Point2D rightRightPoint = startingRightPoint;
		int anglePos = 0;
		while (!stop) {
			rightPoint = rightRightPoint;
			rightRightPoint = advancingFront.higher(rightPoint);
			if (rightRightPoint != null) {
				anglePos = Point2D.anglePosition(rightPoint, rightRightPoint, newPoint);
				if (0 < anglePos && anglePos <= 2) {
					triangulation.addTriangle(newPoint, rightPoint, rightRightPoint);
					triangulation.delaunayLegalizeFromEdge(rightPoint, rightRightPoint);
					triangulation.delaunayLegalizeFromEdge(newPoint, rightPoint);
					advancingFront.remove(rightPoint);
				} else {
					stop = true;
				}
			} else {
				stop = true;
			}
		}
	}

	private void walkLeft(Point2D newPoint, Point2D startingLeftPoint) {
		boolean stop = false;
		Point2D leftPoint;
		Point2D leftLeftPoint = startingLeftPoint;
		int anglePos = 0;
		while (!stop) {
			leftPoint = leftLeftPoint;
			leftLeftPoint = advancingFront.lower(leftLeftPoint);
			if (leftLeftPoint != null) {
				anglePos = Point2D.anglePosition(leftPoint, newPoint, leftLeftPoint);
				if (0 < anglePos && anglePos <= 2) {
					triangulation.addTriangle(newPoint, leftPoint, leftLeftPoint);
					triangulation.delaunayLegalizeFromEdge(leftPoint, leftLeftPoint);
					triangulation.delaunayLegalizeFromEdge(newPoint, leftPoint);
					advancingFront.remove(leftPoint);
				} else {
					stop = true;
				}
			} else {
				stop = true;
			}
		}
	}
}
