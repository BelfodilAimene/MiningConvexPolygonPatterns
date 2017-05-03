package com.mdi.numericalPattern.polygons.sampling;

import static com.mdi.numericalPattern.polygons.utils.Point2D.distance;
import static com.mdi.numericalPattern.polygons.utils.Point2D.signedArea;
import static com.mdi.numericalPattern.utils.NumericalDataReader.readExact;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import com.mdi.numericalPattern.polygons.PolygonsAlgorithm;
import com.mdi.numericalPattern.polygons.utils.Point2D;
import com.mdi.numericalPattern.utils.NumericalDataExact;

public class MCTSExtremePointsEnum extends PolygonsAlgorithm {

	// Budget And Supervised parameters
	private final int maxIteration;
	private final boolean preferedExtremePointOfSameColor;
	private final double preferedSegmentMaxSize;
	private final double preferedCoeffTriangleMaxDistance;

	// Distinct data
	private BigDecimal[][] distinctData;
	private int distinctCount;

	// Index structures
	private MCTSPolygonPattern[] pointPattern;
	private MCTSPolygonPattern[][] segmentPatterns;

	// MCTS structures
	private MCTSNode mctsRoot;
	private Map<MCTSPolygonPattern, MCTSNode> allNodes;
	private int currentIterationCount;

	// FirstLabelByDistinctPoint
	private Map<String, BitSet> distinctPointPerLabel;
	private String[] labelPerDistinctPoint;

	// For computation
	private final Random randomGenerator;

	public static int doTest(String filename, int precision, int maxIteration, boolean preferedExtremePointOfSameColor,
			double preferedSegmentMaxSize, double preferedCoeffTriangleMaxDistance, boolean printPattern)
			throws IOException {
		long instantTimeNs;
		double deltatimeMs;

		System.err.println("Algorithm: " + MCTSExtremePointsEnum.class.getSimpleName());
		System.err.println("File: " + filename);
		System.err.println("Precision: " + precision);
		System.err.println("Will print patterns? " + printPattern);

		NumericalDataExact data = readExact(filename, precision, true);
		System.err.println("Number of objects: " + data.values.length);
		System.err.println("Maximum iteration count: " + maxIteration);

		MCTSExtremePointsEnum algo = new MCTSExtremePointsEnum(data, maxIteration, preferedExtremePointOfSameColor,
				preferedSegmentMaxSize, preferedCoeffTriangleMaxDistance, printPattern);
		if (printPattern) {
			String header = "Intent";
			for (Measure measure : algo.measures) {
				header += "," + measure.getClass().getSimpleName();
			}
			header += ",quality";
			System.out.println(header);
		}
		instantTimeNs = System.nanoTime();
		algo.start();
		deltatimeMs = (System.nanoTime() - instantTimeNs) / 1000000.;

		System.err.println(" > Number of nodes in MCTS tree: " + algo.allNodes.size());
		System.err.println(" > Number of iterations: " + algo.getcurrentIterationCount());
		System.err.println(" > Duration: " + deltatimeMs + " ms");

		return algo.allNodes.size();
	}

	public MCTSExtremePointsEnum(NumericalDataExact numData, int maxIteration, boolean preferedExtremePointOfSameColor,
			double preferedSegmentMaxSize, double preferedCoeffTriangleMaxDistance, boolean printPattern) {
		super(numData, printPattern);
		this.maxIteration = maxIteration;
		this.preferedExtremePointOfSameColor = preferedExtremePointOfSameColor;
		this.preferedSegmentMaxSize = preferedSegmentMaxSize;
		this.preferedCoeffTriangleMaxDistance = preferedCoeffTriangleMaxDistance;
		this.randomGenerator = new Random(System.currentTimeMillis());

		initIndex();
		this.currentIterationCount = 0;
	}

	public int getcurrentIterationCount() {
		return this.currentIterationCount;
	}

	private void initIndex() {
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
		this.pointPattern = new MCTSPolygonPattern[distinctCount];
		this.distinctPointPerLabel = new HashMap<>();
		this.labelPerDistinctPoint = new String[distinctCount];
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

			this.pointPattern[l] = new MCTSPolygonPattern(new int[] { l }, bitSet, distinctCandidates, realCandidates,
					BigDecimal.ZERO, 0);

			String label = numData.classLabels[entry.getValue().nextSetBit(0)];
			BitSet labelBitset = distinctPointPerLabel.get(label);
			if (labelBitset == null) {
				labelBitset = new BitSet();
				distinctPointPerLabel.put(label, labelBitset);
			}
			labelBitset.set(l);
			this.labelPerDistinctPoint[l] = label;
			l += 1;
		}

		segmentPatterns = new MCTSPolygonPattern[distinctCount - 1][];
		for (int i = 0; i < distinctCount - 1; i++) {
			segmentPatterns[i] = new MCTSPolygonPattern[distinctCount - i - 1];
		}
	}

	// Precondition: i<j
	private MCTSPolygonPattern addSegment(int i, int j) {
		BigDecimal[] a, b, c;
		int[] intent;

		BigDecimal alpha, yy, xx, doubleArea;
		int signum;
		double distance;
		a = distinctData[i];
		b = distinctData[j];
		distance = Math.sqrt(b[0].subtract(a[0]).multiply(b[0].subtract(a[0]))
				.add(b[1].subtract(a[1]).multiply(b[1].subtract(a[1]))).doubleValue());

		BitSet extent = new BitSet();
		intent = new int[] { i, j };
		BitSet[] distinctCandidates = new BitSet[] { new BitSet(), new BitSet() };
		BitSet[] realCandidates = new BitSet[] { new BitSet(), new BitSet() };

		alpha = b[1].multiply(a[0]).subtract(b[0].multiply(a[1]));
		yy = a[1].subtract(b[1]);
		xx = b[0].subtract(a[0]);

		for (int k = 0; k < distinctCount; k++) {
			c = distinctData[k];
			doubleArea = alpha.add(c[0].multiply(yy)).add(c[1].multiply(xx));
			signum = doubleArea.signum();

			if (signum > 0) {
				distinctCandidates[1].set(k);
				realCandidates[1].or(this.pointPattern[k].extent);
			} else if (signum < 0) {
				distinctCandidates[0].set(k);
				realCandidates[0].or(this.pointPattern[k].extent);
			} else if (a[0].compareTo(c[0]) * c[0].compareTo(b[0]) >= 0
					&& a[1].compareTo(c[1]) * c[1].compareTo(b[1]) >= 0) {
				extent.or(this.pointPattern[k].extent);
			}
		}
		MCTSPolygonPattern segment = new MCTSPolygonPattern(intent, extent, distinctCandidates, realCandidates,
				BigDecimal.ZERO, distance);
		segmentPatterns[i][j - i - 1] = segment;

		return segment;
	}

	public void start() {
		this.currentIterationCount = 0;
		MCTSPolygonPattern pattern = getMinimalPattern();
		mctsRoot = new MCTSNode(pattern);
		allNodes = new HashMap<>();
		allNodes.put(mctsRoot.pattern, mctsRoot);
		while (!mctsRoot.terminated && (maxIteration < 0 || currentIterationCount < maxIteration)) {
			if (currentIterationCount % 500 == 0) {
				System.err.println(" >>> iteration: " + currentIterationCount);
			}
			iterateOnce();
		}
		if (printPatterns) {
			for (MCTSNode node : allNodes.values()) {
				System.out.println(node.rewardPattern);
			}
		}
	}

	private void iterateOnce() {
		if (mctsRoot.terminated)
			return;
		MCTSNode currentNode = mctsRoot;
		while (!currentNode.expandable) {
			currentNode = currentNode.select();
		}
		currentNode = currentNode.expand();
		currentNode.rollOut(5);
		currentNode.backpropagate();
		currentIterationCount += 1;
	}

	private MCTSPolygonPattern getMinimalPattern() {
		BitSet realCandidates = new BitSet(objCount);
		realCandidates.flip(0, objCount);

		BitSet distinctCandidates = new BitSet(distinctCount);
		distinctCandidates.flip(0, distinctCount);

		return new MCTSPolygonPattern(new int[0], new BitSet(), new BitSet[] { distinctCandidates },
				new BitSet[] { distinctCandidates }, BigDecimal.ZERO, 0);
	}

	private class MCTSNode {
		private final MCTSPolygonPattern pattern;
		private final BitSet expandableFaces;
		private final BitSet[] candidatePerFace;
		private final Set<MCTSNode> parents;
		private final Set<MCTSNode> children;

		private MCTSPolygonPattern rewardPattern;

		private int visitCount;
		private boolean expandable;
		private boolean terminated;
		private int lastVisitIteration;

		private MCTSNode(MCTSPolygonPattern pattern) {
			this.pattern = pattern;
			this.parents = new HashSet<>();
			this.children = new HashSet<>();
			this.candidatePerFace = new BitSet[pattern.distinctCandidates.length];
			for (int i = 0; i < candidatePerFace.length; i++) {
				this.candidatePerFace[i] = (BitSet) pattern.distinctCandidates[i].clone();
			}
			this.visitCount = 0;
			this.expandableFaces = (BitSet) pattern.expandableFaces.clone();
			this.expandable = expandableFaces.cardinality() > 0;
			this.terminated = !this.expandable;
			this.rewardPattern = this.pattern;
		}

		private MCTSNode select() {
			double maxValue = -1;
			List<MCTSNode> bestChilds = new ArrayList<>();
			for (MCTSNode child : children) {
				if (child.terminated)
					continue;
				double exploitationTerm = rewardPattern.quality == 0 ? 0
						: child.rewardPattern.quality / rewardPattern.quality;
				double d = exploitationTerm + 2 * Math.sqrt(Math.log(visitCount) / (1. * child.visitCount));
				if (d > maxValue) {
					maxValue = d;
					bestChilds.clear();
					bestChilds.add(child);
				} else if (d == maxValue) {
					bestChilds.add(child);
				}
			}
			return bestChilds.get(randomGenerator.nextInt(bestChilds.size()));
		}

		private MCTSNode expand() {

			int[] refinement = pattern.getSupervisedRefinement(expandableFaces, candidatePerFace);
			if (refinement == null) {
				refinement = pattern.getRandomRefinement(expandableFaces, candidatePerFace);
			}

			candidatePerFace[refinement[0]].clear(refinement[1]);
			if (candidatePerFace[refinement[0]].isEmpty()) {
				expandableFaces.clear(refinement[0]);
				if (expandableFaces.isEmpty()) {
					expandable = false;
				}
			}

			MCTSPolygonPattern newPolygonPattern = pattern.refine(refinement);
			MCTSNode childNode = new MCTSNode(newPolygonPattern);

			if (allNodes.containsKey(newPolygonPattern)) {
				childNode = allNodes.get(newPolygonPattern);
			} else {
				allNodes.put(newPolygonPattern, childNode);
			}

			children.add(childNode);
			childNode.parents.add(this);
			if (childNode.terminated) {
				backpropagateAllVisited();
			}

			return childNode;
		}

		private void rollOut(int maxShape) {
			MCTSPolygonPattern bestPattern = pattern;
			double bestQuality = bestPattern.quality;
			MCTSPolygonPattern currPattern = bestPattern;
			double currQuality = bestQuality;
			for (int i = currPattern.complexity; i < maxShape; i++) {
				// int[] refinement = currPattern.getRandomRefinement();
				int[] refinement = currPattern.getSupervisedRefinement(currPattern.expandableFaces,
						currPattern.distinctCandidates);
				currPattern = currPattern.refine(refinement);
				if (currPattern == null) {
					break;
				}
				currQuality = currPattern.quality;

				if (currQuality > bestQuality) {
					bestPattern = currPattern;
					bestQuality = currQuality;
				}
			}

			rewardPattern = bestPattern;
		}

		private final void backpropagate() {
			if (lastVisitIteration < currentIterationCount) {
				visitCount++;
				lastVisitIteration = currentIterationCount;
				for (MCTSNode parent : parents) {
					if (parent.rewardPattern.quality < this.rewardPattern.quality) {
						parent.rewardPattern = this.rewardPattern;
					}
					parent.backpropagate();
				}
			}
		}

		private final void backpropagateAllVisited() {
			Queue<MCTSNode> nodeToPrevent = new LinkedList<>();
			nodeToPrevent.add(this);

			while (!nodeToPrevent.isEmpty()) {
				MCTSNode ancestor = nodeToPrevent.poll();

				if (ancestor.expandable)
					continue;
				boolean allChildrenWereTerminated = true;
				for (MCTSNode child : ancestor.children) {
					if (!child.terminated) {
						allChildrenWereTerminated = false;
						break;
					}
				}
				if (!allChildrenWereTerminated)
					continue;

				// ancestor fully expanded and All his children are visited
				ancestor.terminated = true;
				nodeToPrevent.addAll(ancestor.parents);
			}
		}
	}

	private BigDecimal[][] getIntent(int[] intIntent) {
		BigDecimal[][] realIntent = new BigDecimal[intIntent.length][];
		int i = 0;
		for (int k : intIntent) {
			realIntent[i++] = distinctData[k];
		}
		return realIntent;
	}

	public final class MCTSPolygonPattern extends PolygonPattern {
		private final int[] intIntent;
		private final BitSet[] distinctCandidates;
		private final BitSet[] realCandidates;
		private final BitSet expandableFaces;
		private double quality;

		public MCTSPolygonPattern(int[] intIntent, BitSet extent, BitSet[] distinctCandidates, BitSet[] realCandidates,
				BigDecimal area, double perimeter) {
			super(getIntent(intIntent), extent, area, perimeter);
			this.intIntent = intIntent;
			this.distinctCandidates = distinctCandidates;
			this.realCandidates = realCandidates;
			this.expandableFaces = new BitSet(distinctCandidates.length);
			for (int i = 0; i < distinctCandidates.length; i++) {
				if (!distinctCandidates[i].isEmpty()) {
					this.expandableFaces.set(i);
				}
			}
			this.quality = Math.max(2 * maxClassSupport - extent.cardinality(), 0);
		}

		// refinement = [pos, vertex] (insert vertex in pos + 1)
		private MCTSPolygonPattern refine(int[] refinement) {
			if (refinement == null) {
				return null;
			}
			if (this.complexity == 0) {
				return pointPattern[refinement[1]];
			}
			if (this.complexity == 1) {
				boolean correctOrder = intIntent[0] < refinement[1];
				MCTSPolygonPattern p = correctOrder ? segmentPatterns[intIntent[0]][refinement[1] - intIntent[0] - 1]
						: segmentPatterns[refinement[1]][intIntent[0] - refinement[1] - 1];
				if (p == null) {
					p = correctOrder ? addSegment(intIntent[0], refinement[1])
							: addSegment(refinement[1], intIntent[0]);
				}
				return p;
			}
			return add(refinement[0], refinement[1]);
		}

		private MCTSPolygonPattern add(int pos, int vertex) {
			int beforeVertex = intIntent[pos];
			int afterVertex = intIntent[(pos + 1) % complexity];

			int isBeforeToNewInverted = beforeVertex < vertex ? 0 : 1;
			int isNewToAfterInverted = vertex < afterVertex ? 0 : 1;
			int isOldInverted = beforeVertex < afterVertex ? 0 : 1;

			MCTSPolygonPattern beforeToNewSegment = isBeforeToNewInverted == 0
					? segmentPatterns[beforeVertex][vertex - beforeVertex - 1]
					: segmentPatterns[vertex][beforeVertex - vertex - 1];
			if (beforeToNewSegment == null) {
				beforeToNewSegment = isBeforeToNewInverted == 0 ? addSegment(beforeVertex, vertex)
						: addSegment(vertex, beforeVertex);
			}

			MCTSPolygonPattern newToAfterSegment = isNewToAfterInverted == 0
					? segmentPatterns[vertex][afterVertex - vertex - 1]
					: segmentPatterns[afterVertex][vertex - afterVertex - 1];
			if (newToAfterSegment == null) {
				newToAfterSegment = isNewToAfterInverted == 0 ? addSegment(vertex, afterVertex)
						: addSegment(afterVertex, vertex);
			}

			MCTSPolygonPattern oldSegment = isOldInverted == 0
					? segmentPatterns[beforeVertex][afterVertex - beforeVertex - 1]
					: segmentPatterns[afterVertex][beforeVertex - afterVertex - 1];
			if (oldSegment == null) {
				oldSegment = isOldInverted == 0 ? addSegment(beforeVertex, afterVertex)
						: addSegment(afterVertex, beforeVertex);
			}

			BigDecimal newArea = area;
			newArea = newArea
					.add(signedArea(distinctData[beforeVertex], distinctData[vertex], distinctData[afterVertex]));
			double newPerimeter = perimeter + beforeToNewSegment.perimeter + newToAfterSegment.perimeter
					- (complexity > 2 ? oldSegment.perimeter : 0);

			int[] newIntent = new int[intIntent.length + 1];
			BitSet newRealExtent = (BitSet) extent.clone();
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

			newRealExtent.or(beforeToNewSegment.extent);
			newRealExtent.or(newToAfterSegment.extent);

			Set<Integer> set = new HashSet<>();
			for (int i = 0; i < newIntent.length; i++) {
				set.add(newIntent[i]);
			}
			BitSet interior = (BitSet) realCandidates[pos].clone();

			interior.and(beforeToNewSegment.realCandidates[1 - isBeforeToNewInverted]);
			interior.and(newToAfterSegment.realCandidates[1 - isNewToAfterInverted]);
			newRealExtent.or(interior);

			newDistinctCandidates[pos].and(beforeToNewSegment.distinctCandidates[isBeforeToNewInverted]);
			newDistinctCandidates[pos].and(newToAfterSegment.distinctCandidates[1 - isNewToAfterInverted]);
			newRealCandidates[pos].and(beforeToNewSegment.realCandidates[isBeforeToNewInverted]);
			newRealCandidates[pos].and(newToAfterSegment.realCandidates[1 - isNewToAfterInverted]);

			newDistinctCandidates[pos + 1].and(beforeToNewSegment.distinctCandidates[1 - isBeforeToNewInverted]);
			newDistinctCandidates[pos + 1].and(newToAfterSegment.distinctCandidates[isNewToAfterInverted]);
			newRealCandidates[pos + 1].and(beforeToNewSegment.realCandidates[1 - isBeforeToNewInverted]);
			newRealCandidates[pos + 1].and(newToAfterSegment.realCandidates[isNewToAfterInverted]);

			newDistinctCandidates[pos == 0 ? complexity : (pos - 1)]
					.and(beforeToNewSegment.distinctCandidates[1 - isBeforeToNewInverted]);
			newRealCandidates[pos == 0 ? complexity : (pos - 1)]
					.and(beforeToNewSegment.realCandidates[1 - isBeforeToNewInverted]);

			newDistinctCandidates[(pos + 2) % (complexity + 1)]
					.and(newToAfterSegment.distinctCandidates[1 - isNewToAfterInverted]);
			newRealCandidates[(pos + 2) % (complexity + 1)]
					.and(newToAfterSegment.realCandidates[1 - isNewToAfterInverted]);

			return new MCTSPolygonPattern(newIntent, newRealExtent, newDistinctCandidates, newRealCandidates, newArea,
					newPerimeter);
		}

		@Override
		public String toString() {
			return super.toString() + "," + quality;
		}

		private int[] getRandomRefinement(BitSet expandableFaces, BitSet[] distinctCandidatePerFace) {
			if (expandableFaces.isEmpty())
				return null;
			int rand = randomGenerator.nextInt(expandableFaces.cardinality());
			int faceIndex = -1;
			for (int k = 0; k <= rand; k++) {
				faceIndex = expandableFaces.nextSetBit(faceIndex + 1);
			}
			BitSet candidates = distinctCandidatePerFace[faceIndex];
			rand = randomGenerator.nextInt(candidates.cardinality());
			int vertexIndex = -1;
			for (int k = 0; k <= rand; k++) {
				vertexIndex = candidates.nextSetBit(vertexIndex + 1);
			}
			return new int[] { faceIndex, vertexIndex };
		}

		private int[] getSupervisedRefinement(BitSet expandableFaces, BitSet[] distinctCandidatePerFace) {
			if (expandableFaces.isEmpty() || complexity == 0)
				return null;
			int rand = randomGenerator.nextInt(expandableFaces.cardinality());
			int faceIndex = -1;
			for (int k = 0; k <= rand; k++) {
				faceIndex = expandableFaces.nextSetBit(faceIndex + 1);
			}

			BitSet candidates = (BitSet) distinctCandidatePerFace[faceIndex].clone();
			BigDecimal[] beforeVertex = distinctData[intIntent[faceIndex]];
			BigDecimal[] afterVertex = distinctData[intIntent[(faceIndex + 1) % complexity]];
			if (preferedExtremePointOfSameColor) {
				BitSet candidatesByLabel = (BitSet) distinctPointPerLabel
						.get(labelPerDistinctPoint[intIntent[faceIndex]]).clone();
				candidatesByLabel.or((BitSet) distinctPointPerLabel
						.get(labelPerDistinctPoint[intIntent[(faceIndex + 1) % complexity]]).clone());
				candidates.and(candidatesByLabel);
			}
			int numCandidate = candidates.cardinality();
			if (numCandidate == 0)
				return null;
			if (numCandidate == 1)
				return new int[] { faceIndex, candidates.nextSetBit(0) };

			double oldDistance = distance(beforeVertex, afterVertex);
			double maxDistance = complexity == 1
					? (preferedSegmentMaxSize < 0 ? Double.MAX_VALUE
							: randomGenerator.nextDouble() * preferedSegmentMaxSize)
					: (preferedCoeffTriangleMaxDistance < 0 ? Double.MAX_VALUE
							: (preferedCoeffTriangleMaxDistance * randomGenerator.nextDouble()) * oldDistance);

			double[][] distancePerCandidate = new double[numCandidate][];
			BigDecimal[] observerVertex = complexity == 1 ? beforeVertex
					: new BigDecimal[] { beforeVertex[0].add(afterVertex[0]).divide(new BigDecimal(2)),
							beforeVertex[1].add(afterVertex[1]).divide(new BigDecimal(2)) };
			int l = 0;
			for (int k = candidates.nextSetBit(0); k != -1; k = candidates.nextSetBit(k + 1)) {
				distancePerCandidate[l++] = new double[] { k, distance(observerVertex, distinctData[k]) };
			}

			Arrays.sort(distancePerCandidate, new Comparator<double[]>() {

				@Override
				public int compare(double[] o1, double[] o2) {
					return o1[1] < o2[1] ? -1 : o1[1] > o2[1] ? 1 : 0;
				}
			});
			double bestDifference = distancePerCandidate[1][1] - distancePerCandidate[0][1];
			int bestCandidate = (int) distancePerCandidate[0][0];
			for (int ll = 1; ll < distancePerCandidate.length - 1; ll++) {
				if (distancePerCandidate[ll + 1][1] > maxDistance)
					break;
				double difference = distancePerCandidate[ll + 1][1] - distancePerCandidate[ll][1];
				if (difference > bestDifference) {
					bestDifference = difference;
					bestCandidate = (int) distancePerCandidate[ll][0];
				}
			}

			return new int[] { faceIndex, bestCandidate };
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((extent == null) ? 0 : extent.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MCTSPolygonPattern other = (MCTSPolygonPattern) obj;
			if (!MCTSExtremePointsEnum.this.equals(other.getOuterType()))
				return false;
			if (extent == null) {
				if (other.extent != null)
					return false;
			} else if (!extent.equals(other.extent))
				return false;
			return true;
		}

		private MCTSExtremePointsEnum getOuterType() {
			return MCTSExtremePointsEnum.this;
		}
	}
}
