package com.mdi.numericalPattern.hyperrectangles;

import java.util.BitSet;
import java.util.Map.Entry;

import com.mdi.numericalPattern.utils.NumericalData;

import java.util.TreeMap;

// Direct closed Algorithm
public class MinIntChangeIndex extends HyperrectanglesAlgorithm {
	private double[][] values;
	private BitSet[][] index;

	// Computation temporary variables
	private BitSet tempExtent;
	private int[][] tempIntent;

	// For results
	private int closedCount;

	public MinIntChangeIndex(NumericalData numData, int minSup, boolean printPatterns) {
		super(numData, minSup, printPatterns);
		initIndex();
	}

	public int start() {
		closedCount = 0;
		int[][] topPattern = getMinimalPattern();
		BitSet extent = new BitSet();
		extent.flip(0, objCount);
		if (extent.cardinality() < minSup)
			return 0;
		process(topPattern, extent, 0, 0);
		return closedCount;
	}

	protected void process(int[][] intent, BitSet extent, int att, int pos) {

		closedCount++;
		if (printPatterns) {
			double[][] realPattern = getRealPattern(intent);
			System.out.println(toStringPattern(extent, realPattern));
		}

		for (int i = att; i < attCount; i++) {
			if (!(i == att && pos == 1)) {
				tempIntent = cloneIntegerIntent(intent);
				tempExtent = (BitSet) extent.clone();
				if (minChange(tempIntent, tempExtent, i, true))
					process(tempIntent, tempExtent, i, 0);
			}
			tempIntent = cloneIntegerIntent(intent);
			tempExtent = (BitSet) extent.clone();
			if (minChange(tempIntent, tempExtent, i, false))
				process(tempIntent, tempExtent, i, 1);
		}
	}

	private boolean minChange(int[][] intent, BitSet extent, int att, boolean isRight) {
		BitSet bitset, toRemove = isRight ? index[att][intent[att][1]] : index[att][intent[att][0]];
		extent.andNot(toRemove);

		if (extent.cardinality() < minSup) {
			return false;
		}

		for (int i = 0; i < att; i++) {
			if (!(index[i][intent[i][0]].intersects(extent) && index[i][intent[i][1]].intersects(extent))) {
				return false;
			}
		}

		if (isRight) {
			intent[att][1] -= 1;
			bitset = index[att][intent[att][1]];
			while (!bitset.intersects(extent)) {
				intent[att][1] -= 1;
				bitset = index[att][intent[att][1]];
			}
		} else {
			intent[att][0] += 1;
			bitset = index[att][intent[att][0]];
			while (!bitset.intersects(extent)) {
				intent[att][0] += 1;
				bitset = index[att][intent[att][0]];
			}
		}

		for (int i = att + 1; i < attCount; i++) {
			if (!index[i][intent[i][1]].intersects(extent)) {
				intent[i][1] -= 1;
				bitset = index[i][intent[i][1]];
				while (!bitset.intersects(extent)) {
					intent[i][1] -= 1;
					bitset = index[i][intent[i][1]];
				}
			}

			if (!index[i][intent[i][0]].intersects(extent)) {
				intent[i][0] += 1;
				bitset = index[i][intent[i][0]];
				while (!bitset.intersects(extent)) {
					intent[i][0] += 1;
					bitset = index[i][intent[i][0]];
				}
			}
		}

		return true;
	}

	/**
	 * 
	 * @return index structures estimated size in byte (values and index
	 *         structure here)
	 */
	private long initIndex() {
		long size = 0;
		TreeMap<Double, BitSet> treeMap;
		BitSet bitSet;
		double value;
		int k;

		values = new double[attCount][];
		index = new BitSet[attCount][];

		for (int j = 0; j < attCount; j++) {
			treeMap = new TreeMap<>();
			for (int i = 0; i < objCount; i++) {
				value = data[i][j];
				bitSet = treeMap.get(value);
				if (bitSet == null) {
					bitSet = new BitSet();
					treeMap.put(value, bitSet);
				}
				bitSet.set(i);
			}
			k = 0;
			values[j] = new double[treeMap.size()];
			// 12 bytes for array object housekeeping data (see
			// http://www.javamex.com/tutorials/memory/object_memory_usage.shtml)
			size += 8 * treeMap.size() + 12 + 12;
			index[j] = new BitSet[treeMap.size()];
			for (Entry<Double, BitSet> entry : treeMap.entrySet()) {
				values[j][k] = entry.getKey();
				index[j][k] = entry.getValue();
				size += entry.getValue().size() / 8;
				k++;
			}
		}
		return size;
	}

	private double[][] getRealPattern(int[][] pattern) {
		double[][] result = new double[attCount][2];
		for (int i = 0; i < attCount; i++) {
			result[i][0] = values[i][pattern[i][0]];
			result[i][1] = values[i][pattern[i][1]];
		}
		return result;
	}

	private int[][] getMinimalPattern() {
		int[][] topPattern = new int[attCount][2];
		for (int i = 0; i < attCount; i++) {
			topPattern[i][0] = 0;
			topPattern[i][1] = values[i].length - 1;
		}
		return topPattern;
	}

	private int[][] cloneIntegerIntent(int[][] intent) {
		int[][] clone = new int[intent.length][2];
		for (int i = 0; i < clone.length; i++) {
			clone[i][0] = intent[i][0];
			clone[i][1] = intent[i][1];
		}
		return clone;
	}
}