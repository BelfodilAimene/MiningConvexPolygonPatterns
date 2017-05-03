package com.mdi.numericalPattern.polygons.constrained;

import static com.mdi.numericalPattern.polygons.utils.Point2D.distance;
import static com.mdi.numericalPattern.polygons.utils.Point2D.signedArea;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map.Entry;

import com.mdi.numericalPattern.polygons.utils.Point2D;
import com.mdi.numericalPattern.utils.NumericalDataExact;

public class ExtremePointsEnum extends ConstrainedPolygonsAlgorithm {
	// Distinct data
	private BigDecimal[][] distinctData;
	private int distinctCount;

	// Index structures
	private PolygonPatternWithCandidate[] pointPattern;
	private PolygonPatternWithCandidate[][] segmentPatterns;

	// For computation
	private int validPatternCount;
	private int visitedPatternCount;

	public ExtremePointsEnum(NumericalDataExact numData, int minSup, int maxShape, BigDecimal minArea,
			BigDecimal maxArea, double minPerimeter, double maxPerimeter, boolean printResult) {
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
	 * 
	 * @return time in nanosecond to build the index
	 */
	private long initIndex() {
		long startNS = System.nanoTime();
		HashMap<Point2D, BitSet> map = new HashMap<>();
		BitSet bitSet;
		Point2D point;
		for (int i = 0; i < objCount; i++) {
			point = new Point2D(data[i][0], data[i][1]);
			bitSet = map.get(point);
			if (bitSet == null) {
				bitSet = new BitSet();
				map.put(point, bitSet);
			}
			bitSet.set(i);
		}

		this.distinctCount = map.size();
		this.distinctData = new BigDecimal[distinctCount][];
		this.pointPattern = new PolygonPatternWithCandidate[distinctCount];

		BitSet[] distinctCandidates, realCandidates;
		int l = 0;
		for (Entry<Point2D, BitSet> entry : map.entrySet()) {
			point = entry.getKey();
			bitSet = entry.getValue();
			this.distinctData[l] = new BigDecimal[] { point.x, point.y };

			distinctCandidates = new BitSet[] { new BitSet() };
			distinctCandidates[0].set(0, distinctCount);
			distinctCandidates[0].flip(l);

			realCandidates = new BitSet[] { new BitSet() };
			realCandidates[0].set(0, objCount);
			realCandidates[0].andNot(bitSet);

			this.pointPattern[l] = new PolygonPatternWithCandidate(new int[] { l }, bitSet, distinctCandidates,
					realCandidates, BigDecimal.ZERO, 0);

			l += 1;
		}

		segmentPatterns = new PolygonPatternWithCandidate[distinctCount][];
		BigDecimal[] a, b, c;
		int[] intent;
		BigDecimal alpha, yy, xx, doubleArea;
		int signum;
		double distance, distance1, distance2, leftPerimeter;
		boolean notValidTotalPerimeter;

		for (int i = 0; i < distinctCount - 1; i++) {
			a = distinctData[i];
			segmentPatterns[i] = new PolygonPatternWithCandidate[distinctCount - i - 1];
			for (int j = i + 1; j < distinctCount; j++) {
				b = distinctData[j];
				distance = distance(a, b);
				
				if (maxPerimeter >= 0 && distance > maxPerimeter) {
					continue;
				}

				bitSet = new BitSet();
				intent = new int[] { i, j };
				distinctCandidates = new BitSet[] { new BitSet(), new BitSet() };
				realCandidates = new BitSet[] { new BitSet(), new BitSet() };

				alpha = b[1].multiply(a[0]).subtract(b[0].multiply(a[1]));
				yy = a[1].subtract(b[1]);
				xx = b[0].subtract(a[0]);

				leftPerimeter = maxPerimeter - distance;

				for (int k = 0; k < distinctCount; k++) {
					c = distinctData[k];

					distance1 = distance(a, c);
					distance2 = distance(b, c);
					notValidTotalPerimeter = (maxPerimeter >= 0 && distance1 + distance2 > leftPerimeter);
					doubleArea = alpha.add(c[0].multiply(yy)).add(c[1].multiply(xx));
					signum = doubleArea.signum();

					if (signum > 0) {
						if (notValidTotalPerimeter)
							continue;
						distinctCandidates[1].set(k);
						realCandidates[1].or(this.pointPattern[k].extent);
					} else if (signum < 0) {
						if (notValidTotalPerimeter)
							continue;
						distinctCandidates[0].set(k);
						realCandidates[0].or(this.pointPattern[k].extent);
					} else if (a[0].compareTo(c[0]) * c[0].compareTo(b[0]) >= 0
							&& a[1].compareTo(c[1]) * c[1].compareTo(b[1]) >= 0) {
						bitSet.or(this.pointPattern[k].extent);
					}
				}
				segmentPatterns[i][j - i - 1] = new PolygonPatternWithCandidate(intent, bitSet, distinctCandidates,
						realCandidates, BigDecimal.ZERO, distance);
			}
		}
		return System.nanoTime() - startNS;
	}

	@Override
	public void start() {
		this.validPatternCount = 0;
		this.visitedPatternCount = 0;

		bfsLevel0();
		if (maxShape == 0)
			return;

		bfsLevel1();
		if (maxShape == 1)
			return;

		for (int i = 0; i < distinctCount - 1; i++) {
			for (int j = i + 1; j < distinctCount; j++) {
				PolygonPatternWithCandidate newPattern = segmentPatterns[i][j - i - 1];
				if (newPattern != null) {
					dfsLevels(newPattern, j + 1);
				}
			}
		}
	}

	private void bfsLevel0() {
		PolygonPatternWithCandidate pattern = getMinimalPattern();
		visitedPatternCount++;
		boolean valid = minSup < 1 && minArea.signum() <= 0 && minPerimeter <= 0;
		if (valid) {
			validPatternCount++;
			if (printPatterns)
				System.out.println(pattern);
		}
	}

	private void bfsLevel1() {
		PolygonPatternWithCandidate pattern;
		for (int i = 0; i < distinctCount; i++) {
			pattern = pointPattern[i];
			visitedPatternCount++;
			boolean valid = pattern.extent.cardinality() >= minSup && minArea.signum() <= 0 && minPerimeter <= 0;
			if (valid) {
				validPatternCount++;
				if (printPatterns)
					System.out.println(pattern);
			}
		}
	}

	private void dfsLevels(PolygonPatternWithCandidate pattern, int pos) {
		visitedPatternCount++;
		boolean valid = pattern.extent.cardinality() >= minSup && (pattern.area.compareTo(minArea) >= 0)
				&& (pattern.perimeter >= minPerimeter);
		if (valid) {
			validPatternCount++;
			if (printPatterns)
				System.out.println(pattern);
		}

		if (pattern.complexity == maxShape) {
			return;
		}

		for (int i = 0; i < pattern.complexity; i++) {
			BitSet candidates = pattern.distinctCandidates[i];
			for (int j = candidates.nextSetBit(pos); j != -1; j = candidates.nextSetBit(j + 1)) {
				PolygonPatternWithCandidate newPattern = pattern.add(i, j);
				if (newPattern != null) {
					dfsLevels(newPattern, j + 1);
				}
			}
		}
	}

	private PolygonPatternWithCandidate getMinimalPattern() {
		return new PolygonPatternWithCandidate(new int[0], new BitSet(), new BitSet[0], new BitSet[0], BigDecimal.ZERO,
				0);
	}

	private BigDecimal[][] getIntent(int[] intIntent) {
		BigDecimal[][] realIntent = new BigDecimal[intIntent.length][];
		int i = 0;
		for (int k : intIntent) {
			realIntent[i++] = distinctData[k];
		}
		return realIntent;
	}

	public final class PolygonPatternWithCandidate extends PolygonPattern {
		private final int[] intIntent;
		private final BitSet[] distinctCandidates;
		private final BitSet[] realCandidates;

		public PolygonPatternWithCandidate(int[] intIntent, BitSet extent, BitSet[] distinctCandidates,
				BitSet[] realCandidates, BigDecimal area, double perimeter) {
			super(getIntent(intIntent), extent, area, perimeter);
			this.intIntent = intIntent;
			this.distinctCandidates = distinctCandidates;
			this.realCandidates = realCandidates;
		}

		// pos < |intent| and i is in distinctCandidates[pos] and i>max(intent)
		private PolygonPatternWithCandidate add(int pos, int vertex) {
			int beforeVertex = intIntent[pos];
			int afterVertex = intIntent[(pos + 1) % complexity];
			PolygonPatternWithCandidate beforeToNewSegment = segmentPatterns[beforeVertex][vertex - beforeVertex - 1];
			PolygonPatternWithCandidate newToAfterSegment = segmentPatterns[afterVertex][vertex - afterVertex - 1];
			PolygonPatternWithCandidate oldSegment = beforeVertex < afterVertex
					? segmentPatterns[beforeVertex][afterVertex - beforeVertex - 1]
					: segmentPatterns[afterVertex][beforeVertex - afterVertex - 1];

			BigDecimal newArea = area;
			newArea = newArea
					.add(signedArea(distinctData[beforeVertex], distinctData[vertex], distinctData[afterVertex]));
			if (maxArea.signum() >= 0 && newArea.compareTo(maxArea) > 0) {
				// Not valid for maxArea constraint
				return null;
			}

			double newPerimeter = perimeter + beforeToNewSegment.perimeter + newToAfterSegment.perimeter
					- (complexity > 2 ? oldSegment.perimeter : 0);
			if (maxPerimeter >= 0 && newPerimeter > maxPerimeter) {
				// Not valid for maxPerimeter constraint
				return null;
			}

			int[] newIntent = new int[intIntent.length + 1];
			BitSet newExtent = (BitSet) extent.clone();
			BitSet[] newDistinctCandidates = new BitSet[intIntent.length + 1];
			BitSet[] newRealCandidates = new BitSet[intIntent.length + 1];

			for (int i = 0; i <= pos; i++) {
				newIntent[i] = intIntent[i];
				newDistinctCandidates[i] = (BitSet) distinctCandidates[i].clone();
				newRealCandidates[i] = (BitSet) realCandidates[i].clone();
			}
			newIntent[pos + 1] = vertex;
			newDistinctCandidates[pos + 1] = (BitSet) distinctCandidates[pos].clone();
			newRealCandidates[pos + 1] = (BitSet) realCandidates[pos].clone();
			for (int i = pos + 2; i <= intIntent.length; i++) {
				newIntent[i] = intIntent[i - 1];
				newDistinctCandidates[i] = (BitSet) distinctCandidates[i - 1].clone();
				newRealCandidates[i] = (BitSet) realCandidates[i - 1].clone();
			}

			newExtent.or(beforeToNewSegment.extent);
			newExtent.or(newToAfterSegment.extent);

			BitSet interior = (BitSet) realCandidates[pos].clone();
			interior.and(beforeToNewSegment.realCandidates[1]);
			interior.and(newToAfterSegment.realCandidates[0]);
			newExtent.or(interior);

			newDistinctCandidates[pos].and(beforeToNewSegment.distinctCandidates[0]);
			newDistinctCandidates[pos].and(newToAfterSegment.distinctCandidates[0]);
			newRealCandidates[pos].and(beforeToNewSegment.realCandidates[0]);
			newRealCandidates[pos].and(newToAfterSegment.realCandidates[0]);

			newDistinctCandidates[pos + 1].and(beforeToNewSegment.distinctCandidates[1]);
			newDistinctCandidates[pos + 1].and(newToAfterSegment.distinctCandidates[1]);
			newRealCandidates[pos + 1].and(beforeToNewSegment.realCandidates[1]);
			newRealCandidates[pos + 1].and(newToAfterSegment.realCandidates[1]);

			newDistinctCandidates[pos == 0 ? complexity : (pos - 1)].and(beforeToNewSegment.distinctCandidates[1]);
			newRealCandidates[pos == 0 ? complexity : (pos - 1)].and(beforeToNewSegment.realCandidates[1]);

			newDistinctCandidates[(pos + 2) % (complexity + 1)].and(newToAfterSegment.distinctCandidates[0]);
			newRealCandidates[(pos + 2) % (complexity + 1)].and(newToAfterSegment.realCandidates[0]);

			return new PolygonPatternWithCandidate(newIntent, newExtent, newDistinctCandidates, newRealCandidates,
					newArea, newPerimeter);
		}
	}
}
