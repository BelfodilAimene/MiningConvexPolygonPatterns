package com.mdi.numericalPattern.polygons.constrained;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.mdi.numericalPattern.polygons.constrained.triangulation.SweepLineBuilder;
import com.mdi.numericalPattern.polygons.constrained.triangulation.Triangulation;
import com.mdi.numericalPattern.polygons.constrained.triangulation.TriangulationBuilder;
import com.mdi.numericalPattern.polygons.utils.Point2D;
import com.mdi.numericalPattern.utils.NumericalDataExact;

public class DelaunayEnum extends ConstrainedPolygonsAlgorithm {
	// Index structures
	private TreeMap<BigDecimal, BitSet>[] index;

	// For computation
	private int validPatternCount;
	private int visitedPatternCount;

	public DelaunayEnum(NumericalDataExact numData, int minSup, int maxShape, BigDecimal minArea, BigDecimal maxArea,
			double minPerimeter, double maxPerimeter, boolean printResult) {
		super(numData, minSup, maxShape, minArea, maxArea, minPerimeter, maxPerimeter, printResult);
		initIndex();
		this.validPatternCount = 0;
		this.visitedPatternCount = 0;
	}

	@Override
	public int getValidPatternCount() {
		return validPatternCount;
	}

	@Override
	public int getVisitedPatternCount() {
		return visitedPatternCount;
	}

	/**
	 * Initialize the list of ordered values for each attribute. Will be used
	 * for getting minimal changes. log(n) operations
	 * 
	 * return domains size in byte if we use an array (rather than a treeSet)
	 */
	@SuppressWarnings("unchecked")
	private void initIndex() {
		index = new TreeMap[2];
		TreeMap<BigDecimal, BitSet> treeMap;
		BitSet bitSet;
		BigDecimal value;
		for (int j = 0; j < 2; j++) {
			treeMap = new TreeMap<>();
			index[j] = treeMap;
			for (int i = 0; i < objCount; i++) {
				value = data[i][j];
				bitSet = treeMap.get(value);
				if (bitSet == null) {
					bitSet = new BitSet();
					treeMap.put(value, bitSet);
				}
				bitSet.set(i);
			}
		}
	}

	@Override
	public void start() {
		this.validPatternCount = 0;
		this.visitedPatternCount = 0;
		if (objCount < minSup)
			return;
		PolygonPatternWithTriangulation topPattern = getMinimalPattern();
		process(topPattern, 0);
	}

	protected void process(PolygonPatternWithTriangulation pattern, int pos) {
		visitedPatternCount++;
		if (pattern.area.compareTo(minArea) < 0 || pattern.perimeter < minPerimeter) {
			return;
		}
		
		boolean valid = (maxArea.signum() < 0 || pattern.area.compareTo(maxArea) <= 0)
				&& (maxPerimeter < 0 || pattern.perimeter <= maxPerimeter)
				&& (maxShape < 0 || pattern.complexity <= maxShape);
		if (valid) {
			validPatternCount++;
			if (printPatterns) {
				System.out.println(pattern);
			}
		}

		PolygonPatternWithTriangulation newPattern;
		for (int k = pos; k < pattern.complexity; k++) {
			newPattern = pattern.deleteExtremePoint(k);
			if (newPattern != null) {
				process(newPattern, k);
			}
		}
	}

	public PolygonPatternWithTriangulation getMinimalPattern() {
		TriangulationBuilder sweepLineBuilder = new SweepLineBuilder();
		Set<Point2D> points = new HashSet<>();
		for (BigDecimal[] p : data) {
			points.add(new Point2D(p[0], p[1]));
		}
		sweepLineBuilder.set(points);
		Triangulation triangulation = sweepLineBuilder.build();
		BigDecimal[][] intent = getIntent(triangulation);

		BitSet extent = new BitSet(objCount);
		extent.flip(0, objCount);

		return new PolygonPatternWithTriangulation(triangulation, intent, extent, Point2D.polygoneArea(intent),
				Point2D.polygonePerimeter(intent));
	}

	private class PolygonPatternWithTriangulation extends PolygonPattern {
		private final Triangulation triangulation;

		private PolygonPatternWithTriangulation(Triangulation triangulation, BigDecimal[][] intent, BitSet extent,
				BigDecimal area, double perimeter) {
			super(intent, extent, area, perimeter);
			this.triangulation = triangulation;
		}

		private PolygonPatternWithTriangulation deleteExtremePoint(int k) {
			Point2D toDeletePoint = triangulation.extremePointsSequence().get(k);

			// Update extent ------------------------------------
			BitSet newExtent = (BitSet) extent.clone();
			BitSet tmpBitset = (BitSet) index[0].get(toDeletePoint.x).clone();
			if (tmpBitset != null) {
				BitSet tmpBitset2 = index[1].get(toDeletePoint.y);
				if (tmpBitset2 != null) {
					tmpBitset.and(tmpBitset2);
					if (!tmpBitset.isEmpty()) {
						newExtent.andNot(tmpBitset);
						if (newExtent.cardinality() < minSup) {
							return null;
						}
					}
				}
			}
			// --------------------------------------------------

			// ---- Update new intent ---------------------------
			Triangulation newTriangulation = triangulation.clone();
			newTriangulation.deleteExtremePoint(k);
			BigDecimal[][] newIntent = getIntent(newTriangulation);
			// --------------------------------------------------

			return new PolygonPatternWithTriangulation(newTriangulation, newIntent, newExtent,
					Point2D.polygoneArea(newIntent), Point2D.polygonePerimeter(newIntent));
		}
	}

	private static BigDecimal[][] getIntent(Triangulation triangulation) {
		List<Point2D> extremePoints = triangulation.extremePointsSequence();
		BigDecimal[][] result = new BigDecimal[extremePoints.size()][];
		int i = 0;
		for (Point2D p : extremePoints) {
			result[i++] = new BigDecimal[] { p.x, p.y };
		}
		return result;
	}
}
