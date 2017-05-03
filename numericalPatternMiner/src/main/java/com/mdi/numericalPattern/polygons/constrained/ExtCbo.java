package com.mdi.numericalPattern.polygons.constrained;

import java.math.BigDecimal;
import java.util.BitSet;

import com.mdi.numericalPattern.polygons.constrained.convexhull.QuickHull;
import com.mdi.numericalPattern.polygons.utils.Point2D;
import com.mdi.numericalPattern.utils.NumericalDataExact;

public class ExtCbo extends ConstrainedPolygonsAlgorithm {
	// For computation
	private final QuickHull quickHull;
	private int validPatternCount;
	private int visitedPatternCount;

	public ExtCbo(NumericalDataExact numData, int minSup, int maxShape, BigDecimal minArea, BigDecimal maxArea,
			double minPerimeter, double maxPerimeter, boolean printResult) {
		super(numData, minSup, maxShape, minArea, maxArea, minPerimeter, maxPerimeter, printResult);
		this.quickHull = new QuickHull();
		this.quickHull.set(numData.values);
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

	@Override
	public void start() {
		this.validPatternCount = 0;
		this.visitedPatternCount = 0;
		if (objCount < minSup)
			return;
		this.visitedPatternCount++;
		process(getMinimalPattern(), 0);
	}

	protected void process(PolygonPatternForCbO pattern, int pos) {
		if ((maxArea.signum() >= 0 && pattern.area.compareTo(maxArea) > 0)
				|| (maxPerimeter >= 0 && pattern.perimeter > maxPerimeter)) {
			return;
		}
		boolean valid = pattern.extent.cardinality() >= minSup && (pattern.area.compareTo(minArea) >= 0)
				&& (pattern.perimeter >= minPerimeter) && (maxShape < 0 || pattern.complexity <= maxShape);

		if (valid) {
			validPatternCount++;
			if (printPatterns) {
				System.out.println(pattern);
			}
		}

		BitSet toTest = new BitSet(objCount);
		toTest.set(pos, objCount);
		toTest.andNot(pattern.extent);
		for (int i = toTest.nextSetBit(pos); i != -1; i = toTest.nextSetBit(i + 1)) {
			PolygonPatternForCbO newPattern = pattern.addObject(i);
			this.visitedPatternCount++;
			if (newPattern != null) {
				process(newPattern, i + 1);
			}
		}
	}

	public PolygonPatternForCbO getMinimalPattern() {
		return new PolygonPatternForCbO(new BigDecimal[0][], new BitSet(objCount), BigDecimal.ZERO, 0);
	}

	public final class PolygonPatternForCbO extends PolygonPattern {
		public PolygonPatternForCbO(BigDecimal[][] intent, BitSet extent, BigDecimal area, double perimeter) {
			super(intent, extent, area, perimeter);
		}

		private PolygonPatternForCbO addObject(int newObject) {
			if (extent.get(newObject))
				return null;
			BitSet newExtent = (BitSet) extent.clone();
			newExtent.set(newObject);
			int[] tmpIntent = quickHull.prime(newExtent);
			BitSet closedExtent = quickHull.prime(tmpIntent);
			BitSet newObjects = (BitSet) closedExtent.clone();
			newObjects.andNot(extent);
			if (newObjects.nextSetBit(0) < newObject)
				return null;
			BigDecimal[][] newIntent = quickHull.getExtremePointSequence(tmpIntent);
			return new PolygonPatternForCbO(newIntent, closedExtent, Point2D.polygoneArea(newIntent),
					Point2D.polygonePerimeter(newIntent));
		}
	}
}
