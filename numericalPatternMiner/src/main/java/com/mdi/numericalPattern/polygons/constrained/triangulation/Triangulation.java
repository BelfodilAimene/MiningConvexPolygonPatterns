package com.mdi.numericalPattern.polygons.constrained.triangulation;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mdi.numericalPattern.polygons.utils.Point2D;
import com.mdi.numericalPattern.polygons.utils.Point2D.AngleComparator;

import java.util.Set;
import java.util.TreeSet;

public class Triangulation {
	final Map<Point2D, Map<Point2D, Set<Point2D>>> graph;
	final List<Point2D> extremePointsSequence;
	private final List<Point2D> unmodifiableExtremePointsSequence;

	Triangulation(Set<Point2D> allPoints) {
		this.graph = new HashMap<>();
		this.extremePointsSequence = new ArrayList<>();
		this.unmodifiableExtremePointsSequence = Collections.unmodifiableList(extremePointsSequence);
		for (Point2D point : allPoints) {
			graph.put(point, new HashMap<>());
		}
	}

	Triangulation(Set<Point2D> allPoints, Map<Point2D, Map<Point2D, Set<Point2D>>> graph,
			List<Point2D> extremePointsSequence) {
		this.graph = graph;
		this.extremePointsSequence = extremePointsSequence;
		this.unmodifiableExtremePointsSequence = Collections.unmodifiableList(extremePointsSequence);
	}

	public List<Point2D> extremePointsSequence() {
		return unmodifiableExtremePointsSequence;
	}

	// Clone triangulation without cloning points
	@Override
	public Triangulation clone() {
		Map<Point2D, Map<Point2D, Set<Point2D>>> graph = new HashMap<>();
		for (Entry<Point2D, Map<Point2D, Set<Point2D>>> entry : this.graph.entrySet()) {
			Map<Point2D, Set<Point2D>> cloneValue = new HashMap<>();
			Map<Point2D, Set<Point2D>> value = entry.getValue();
			for (Entry<Point2D, Set<Point2D>> entry2 : value.entrySet()) {
				cloneValue.put(entry2.getKey(), new HashSet<>(entry2.getValue()));
			}
			graph.put(entry.getKey(), cloneValue);
		}
		Triangulation t = new Triangulation(graph.keySet(), graph, new ArrayList<>(extremePointsSequence));
		return t;
	}

	// can be used when all points in the data set are collinear
	void addEdge(Point2D p0, Point2D p1) {
		Map<Point2D, Set<Point2D>> currentPointMap;
		currentPointMap = graph.get(p0);
		currentPointMap.put(p1, new HashSet<>());
		graph.put(p0, currentPointMap);

		currentPointMap = graph.get(p1);
		currentPointMap.put(p0, new HashSet<>());
		graph.put(p1, currentPointMap);
	}

	void addTriangle(Point2D p0, Point2D p1, Point2D p2) {
		if (Point2D.twiceSignedArea(p0, p1, p2).signum() == 0)
			throw new IllegalArgumentException("Points p0, p1 and p2 are collinear");
		Map<Point2D, Set<Point2D>> currentPointMap;
		Set<Point2D> currentTriangleSet;

		currentPointMap = graph.get(p0);
		if (currentPointMap == null) {
			currentPointMap = new HashMap<>();
			graph.put(p0, currentPointMap);
		}
		currentTriangleSet = currentPointMap.get(p1);
		if (currentTriangleSet == null) {
			currentTriangleSet = new HashSet<>(2);
			currentPointMap.put(p1, currentTriangleSet);
		}
		currentTriangleSet.add(p2);

		currentTriangleSet = currentPointMap.get(p2);
		if (currentTriangleSet == null) {
			currentTriangleSet = new HashSet<>(2);
			currentPointMap.put(p2, currentTriangleSet);
		}
		currentTriangleSet.add(p1);

		currentPointMap = graph.get(p1);
		if (currentPointMap == null) {
			currentPointMap = new HashMap<>();
			graph.put(p1, currentPointMap);
		}
		currentTriangleSet = currentPointMap.get(p0);
		if (currentTriangleSet == null) {
			currentTriangleSet = new HashSet<>(2);
			currentPointMap.put(p0, currentTriangleSet);
		}
		currentTriangleSet.add(p2);

		currentTriangleSet = currentPointMap.get(p2);
		if (currentTriangleSet == null) {
			currentTriangleSet = new HashSet<>(2);
			currentPointMap.put(p2, currentTriangleSet);
		}
		currentTriangleSet.add(p0);

		currentPointMap = graph.get(p2);
		if (currentPointMap == null) {
			currentPointMap = new HashMap<>();
			graph.put(p2, currentPointMap);
		}
		currentTriangleSet = currentPointMap.get(p0);
		if (currentTriangleSet == null) {
			currentTriangleSet = new HashSet<>(2);
			currentPointMap.put(p0, currentTriangleSet);
		}
		currentTriangleSet.add(p1);

		currentTriangleSet = currentPointMap.get(p1);
		if (currentTriangleSet == null) {
			currentTriangleSet = new HashSet<>(2);
			currentPointMap.put(p1, currentTriangleSet);
		}
		currentTriangleSet.add(p0);
	}

	public void deleteExtremePoint(int k) {
		Point2D toDeletePoint = extremePointsSequence.get(k); 
		extremePointsSequence.remove(k);
		ArrayList<Point2D> neighbors = new ArrayList<>(graph.get(toDeletePoint).keySet());
		graph.remove(toDeletePoint);

		if (graph.size() == 0)
			return;

		Point2D firstNeighbor = neighbors.get(0);
		AngleComparator cmp = new AngleComparator(toDeletePoint, firstNeighbor);
		TreeSet<Point2D> sortedNeighbors = new TreeSet<>(cmp);
		sortedNeighbors.addAll(neighbors);
		for (Point2D p : neighbors) {
			graph.get(p).remove(toDeletePoint);
		}

		Point2D nextPoint = sortedNeighbors.first();
		Point2D currentPoint;
		for (Point2D p : sortedNeighbors.tailSet(nextPoint, false)) {
			currentPoint = nextPoint;
			nextPoint = p;
			graph.get(currentPoint).get(nextPoint).remove(toDeletePoint);
			graph.get(nextPoint).get(currentPoint).remove(toDeletePoint);
		}

		complete(sortedNeighbors, k);
	}

	void complete(TreeSet<Point2D> sortedExtremePointsCandidates, int lastIndex) {
		if (sortedExtremePointsCandidates.size() > 2) {
			Deque<Point2D> stack = new ArrayDeque<>();
			Set<Point2D> collinearPoints = new HashSet<>();
			Point2D p1 = sortedExtremePointsCandidates.first();
			Point2D p2 = sortedExtremePointsCandidates.higher(p1);
			Point2D p3 = sortedExtremePointsCandidates.higher(p2);
			while (p3 != null) {
				BigDecimal twiceSignedArea = Point2D.twiceSignedArea(p1, p2, p3);
				if (twiceSignedArea.signum() == 1) {
					// Missed Triangle
					addTriangle(p1, p2, p3);
					delaunayLegalizeFromEdge(p2, p1);
					delaunayLegalizeFromEdge(p2, p3);
					sortedExtremePointsCandidates.remove(p2);
					if (!stack.isEmpty()) {
						p1 = stack.pop();
					}
				} else {
					if (twiceSignedArea.signum() == 0) {
						collinearPoints.add(p2);
					}
					stack.push(p1);
					p1 = sortedExtremePointsCandidates.higher(p1);
				}
				p2 = sortedExtremePointsCandidates.higher(p1);
				p3 = sortedExtremePointsCandidates.higher(p2);
			}
			sortedExtremePointsCandidates.removeAll(collinearPoints);
		}

		ArrayList<Point2D> convexHullNewPart = new ArrayList<>(sortedExtremePointsCandidates);
		convexHullNewPart.removeAll(extremePointsSequence);
		for (Point2D p : convexHullNewPart) {
			extremePointsSequence.add(lastIndex, p);
		}
	}

	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return the number of flips
	 */
	int delaunayLegalizeFromEdge(Point2D p1, Point2D p2) {
		Map<Point2D, Set<Point2D>> pMap = graph.get(p1);
		if (pMap == null)
			return 0;
		Set<Point2D> pSet = graph.get(p1).get(p2);
		if (pSet == null || pSet.size() != 2)
			return 0;
		Iterator<Point2D> it = pSet.iterator();
		Point2D p3 = it.next(), p4 = it.next();
		int count = 0;
		if (Point2D.circumcircleTestWithDeterminant(p1, p2, p3, p4) == -1) {
			pMap.remove(p2);
			pMap.get(p3).remove(p2);
			pMap.get(p3).add(p4);
			pMap.get(p4).remove(p2);
			pMap.get(p4).add(p3);

			pMap = graph.get(p2);
			pMap.remove(p1);
			pMap.get(p3).remove(p1);
			pMap.get(p3).add(p4);
			pMap.get(p4).remove(p1);
			pMap.get(p4).add(p3);

			pMap = graph.get(p3);
			pSet = new HashSet<>(2);
			pSet.add(p1);
			pSet.add(p2);
			pMap.put(p4, pSet);

			pMap.get(p1).remove(p2);
			pMap.get(p1).add(p4);
			pMap.get(p2).remove(p1);
			pMap.get(p2).add(p4);

			pMap = graph.get(p4);
			pSet = new HashSet<>(pSet);
			pMap.put(p3, pSet);
			pMap.get(p1).remove(p2);
			pMap.get(p1).add(p3);
			pMap.get(p2).remove(p1);
			pMap.get(p2).add(p3);

			count += 1;

			int countTemp = delaunayLegalizeFromEdge(p1, p3);
			if (countTemp > 0) {
				count += countTemp;
			} else {
				count += delaunayLegalizeFromEdge(p1, p4);
			}

			countTemp = delaunayLegalizeFromEdge(p2, p3);
			if (countTemp > 0) {
				count += countTemp;
			} else {
				count += delaunayLegalizeFromEdge(p2, p4);
			}
		}
		return count;
	}
}
