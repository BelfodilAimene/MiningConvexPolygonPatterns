package com.mdi.numericalPattern.polygons.constrained.convexhull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.BitSet;

import com.mdi.numericalPattern.polygons.utils.Point2D;

public class QuickHull {
	private BigDecimal[][] dataset;

	public QuickHull set(BigDecimal[][] dataset) {
		this.dataset = dataset;
		return this;
	}

	public BitSet prime(int[] intent) {
		BitSet extent = new BitSet(dataset.length);
		if (intent.length == 0)
			return extent;
		if (intent.length == 1) {
			int i = 0;
			BigDecimal[] current = dataset[intent[0]];
			for (BigDecimal[] point : dataset) {
				if (current[0].compareTo(point[0]) == 0 && current[1].compareTo(point[1]) == 0)
					extent.set(i);
				i++;
			}
			return extent;
		}
		if (intent.length == 2) {
			int i = 0;
			BigDecimal[] current = dataset[intent[0]];
			BigDecimal[] next = dataset[intent[1]];
			for (BigDecimal[] point : dataset) {
				if (Point2D.signedArea(current, next, point).signum() == 0
						&& current[0].compareTo(point[0]) * point[0].compareTo(next[0]) >= 0
						&& current[1].compareTo(point[1]) * point[1].compareTo(next[1]) >= 0)
					extent.set(i);
				i++;
			}
			return extent;
		}
		int i = 0;
		boolean add;
		int sign;
		for (BigDecimal[] point : dataset) {
			add = true;
			BigDecimal[] current = null;
			BigDecimal[] next = dataset[intent[0]];
			for (int k = 1; k <= intent.length; k++) {
				current = next;
				next = dataset[intent[k % intent.length]];
				sign = Point2D.signedArea(current, next, point).signum();
				if (sign < 0) {
					add = false;
					break;
				}
				if (sign == 0 && !(current[0].compareTo(point[0]) * point[0].compareTo(next[0]) >= 0
						&& current[1].compareTo(point[1]) * point[1].compareTo(next[1]) >= 0)) {
					add = false;
					break;
				}
			}
			if (add)
				extent.set(i);
			i++;
		}

		return extent;
	}

	public int[] prime(BitSet extent) {
		int cardinality = extent.cardinality();
		if (cardinality == 0) {
			return new int[0];
		}
		if (cardinality == 1) {
			return new int[] { extent.nextSetBit(0) };
		}

		BitSet negativeCandidates = new BitSet(extent.length());
		BitSet positiveCandidates = new BitSet(extent.length());
		BigDecimal maxNegativeArea = BigDecimal.ZERO;
		BigDecimal maxPositiveArea = BigDecimal.ZERO;
		int farthestPositiveCandidate = -1;
		int farthestNegativeCandidate = -1;
		BigDecimal area;
		int sign;
		int startBit = -1;
		int endBit = -1;
		BigDecimal[] firstPoint = null;
		BigDecimal[] secondPoint = null;
		BigDecimal[] point;
		for (int i = extent.nextSetBit(0); i != -1; i = extent.nextSetBit(i + 1)) {
			point = dataset[i];
			if (firstPoint == null) {
				firstPoint = point;
				startBit = i;
			}
			sign = point[0].compareTo(firstPoint[0]);
			if (sign < 0 || (sign == 0 && point[1].compareTo(firstPoint[1]) < 0)) {
				firstPoint = point;
				startBit = i;
			}

			if (secondPoint == null) {
				secondPoint = point;
				endBit = i;
			}
			sign = point[0].compareTo(secondPoint[0]);
			if (sign > 0 || (sign == 0 && point[1].compareTo(secondPoint[1]) > 0)) {
				secondPoint = point;
				endBit = i;
			}
		}

		for (int i = extent.nextSetBit(0); i != -1; i = extent.nextSetBit(i + 1)) {
			point = dataset[i];
			area = Point2D.signedArea(firstPoint, secondPoint, point);
			sign = area.signum();
			if (sign < 0) {
				negativeCandidates.flip(i);
				if (area.compareTo(maxNegativeArea) < 0) {
					farthestNegativeCandidate = i;
					maxNegativeArea = area;
				}
			} else if (sign > 0) {
				positiveCandidates.flip(i);
				if (area.compareTo(maxPositiveArea) > 0) {
					farthestPositiveCandidate = i;
					maxPositiveArea = area;
				}
			}
		}
		ArrayList<Integer> extremePointSequence = new ArrayList<>(extent.cardinality());
		extremePointSequence.add(startBit);
		if (farthestNegativeCandidate >= 0)
			extremePointSequence.addAll(updateHull(startBit, endBit, negativeCandidates, farthestNegativeCandidate));
		extremePointSequence.add(endBit);
		if (farthestPositiveCandidate >= 0)
			extremePointSequence.addAll(updateHull(endBit, startBit, positiveCandidates, farthestPositiveCandidate));

		int[] result = new int[extremePointSequence.size()];
		int i = 0;
		for (int element : extremePointSequence) {
			result[i++] = element;
		}
		return result;
	}

	private ArrayList<Integer> updateHull(int before, int next, BitSet candidates, int newExtremePoint) {
		BitSet beforeToNewCandidates = new BitSet(candidates.length());
		BitSet newToNextCandidates = new BitSet(candidates.length());
		BigDecimal beforeToNewMaxArea = BigDecimal.ZERO;
		BigDecimal newToNextMaxArea = BigDecimal.ZERO;
		int beforeToNewFarthestCandidate = -1;
		int newToNextFarthestCandidate = -1;
		BigDecimal area1, area2;
		int sign, sign2;
		BigDecimal[] beforePoint = dataset[before];
		BigDecimal[] nextPoint = dataset[next];
		BigDecimal[] newPoint = dataset[newExtremePoint];

		BigDecimal[] point;

		for (int i = candidates.nextSetBit(0); i != -1; i = candidates.nextSetBit(i + 1)) {
			point = dataset[i];
			area1 = Point2D.signedArea(beforePoint, newPoint, point);
			area2 = Point2D.signedArea(newPoint, nextPoint, point);

			sign = area1.signum();
			sign2 = area2.signum();

			if (sign < 0) {
				beforeToNewCandidates.flip(i);
				if (area1.compareTo(beforeToNewMaxArea) < 0) {
					beforeToNewFarthestCandidate = i;
					beforeToNewMaxArea = area1;
				}
			}

			if (sign2 < 0) {
				newToNextCandidates.flip(i);
				if (area2.compareTo(newToNextMaxArea) < 0) {
					newToNextFarthestCandidate = i;
					newToNextMaxArea = area2;
				}
			}
		}

		ArrayList<Integer> extremePointSequence = new ArrayList<>(candidates.cardinality());
		if (beforeToNewFarthestCandidate >= 0)
			extremePointSequence
					.addAll(updateHull(before, newExtremePoint, beforeToNewCandidates, beforeToNewFarthestCandidate));
		extremePointSequence.add(newExtremePoint);
		if (newToNextFarthestCandidate >= 0)
			extremePointSequence
					.addAll(updateHull(newExtremePoint, next, newToNextCandidates, newToNextFarthestCandidate));

		return extremePointSequence;
	}

	public BigDecimal[][] getExtremePointSequence(int[] extremePointSequence) {
		BigDecimal[][] extremePointSequenceResult = new BigDecimal[extremePointSequence.length][];
		int i = 0;
		for (int k : extremePointSequence) {
			extremePointSequenceResult[i++] = dataset[k];
		}
		return extremePointSequenceResult;
	}
}
