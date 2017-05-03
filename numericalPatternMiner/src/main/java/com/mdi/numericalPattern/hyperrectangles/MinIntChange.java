package com.mdi.numericalPattern.hyperrectangles;

import java.util.BitSet;
import java.util.TreeSet;

import com.mdi.numericalPattern.utils.NumericalData;

/**
 * The algorithm MinIntChange designed in the submitted paper. Extracts frequent
 * closed interval patterns
 * 
 *
 */
public class MinIntChange extends HyperrectanglesAlgorithm {
	// Each TreeSet stores the different and unique values of an attribute
	// domain.
	// It is needed to operate in log(n) the procedure of minimal change
	protected TreeSet<Double>[] domains; // Unique values only !

	// For computation
	private double[][] patternL, patternR;
	private BitSet prime;

	// For results
	private int closedCount;

	public MinIntChange(NumericalData d, int minSup, boolean printPatterns) {
		super(d, minSup,printPatterns);
		initDomains();
	}

	public int start() {
		closedCount = 0;
		double[][] topPattern = getMinimalPattern();
		BitSet A = new BitSet();
		A.flip(0, objCount);
		process(topPattern, 0, 0, A, topPattern);
		return closedCount;
	}

	/**
	 * 
	 * 
	 * @param B
	 *            was previously generated
	 * @param att
	 *            with a change at attribute att
	 * @param pos
	 *            and position pos
	 * @param C
	 *            = B^\square
	 * @param D
	 *            = C^\square
	 */
	protected void process(double[][] B, int att, int pos, BitSet C, double[][] D) {
		if (!isCanonical(B, D, att) || (C.cardinality() < minSup))
			return;

		closedCount++;
		if (printPatterns)
			System.out.println(toStringPattern(C, D));

		for (int i = att; i < attCount; i++) {
			if (D[i][0] == D[i][1])
				continue;
			if (!(i == att && pos == 1)) {
				patternR = cloneIntent(D);
				patternR[i][1] = domains[i].lower(D[i][1]);
				if (patternR[i][0] <= patternR[i][1]) {
					prime = prime(patternR, C);
					process(patternR, i, 0, prime, prime(prime));
				}
			}
			patternL = cloneIntent(D);
			patternL[i][0] = domains[i].higher(D[i][0]);
			if (patternL[i][0] <= patternL[i][1]) {
				prime = prime(patternL, C);
				process(patternL, i, 1, prime, prime(prime));
			}
		}
	}

	/**
	 * Get most frequent pattern (it is both closed and generators)
	 * 
	 * @return minimal pattern w.r.t. subsumption, i.e. with largest intervals
	 */
	public double[][] getMinimalPattern() {
		double[][] topPattern = new double[attCount][2];
		for (int i = 0; i < topPattern.length; i++) {
			topPattern[i][0] = domains[i].first();
			topPattern[i][1] = domains[i].last();
		}
		return topPattern;
	}

	/**
	 * The first operator of the Galois connection
	 * 
	 * @param D
	 *            an interval pattern
	 * @param Z
	 *            the set of object in which we know image of D is
	 * 
	 * @return the image of D
	 */
	public BitSet prime(double[][] D, BitSet Z) {
		boolean addObject;
		BitSet extent = new BitSet();
		for (int i = Z.nextSetBit(0); i >= 0; i = Z.nextSetBit(i + 1)) {
			addObject = true;
			for (int j = 0; j < attCount && addObject; j++) {
				if (data[i][j] < D[j][0] || data[i][j] > D[j][1]) {
					addObject = false;
					break;
				}
			}
			if (addObject)
				extent.set(i);
		}
		return extent;
	}

	/**
	 * The second operator of the Galois connection
	 * 
	 * @param extent
	 *            a set of object
	 * @return its description
	 */
	public double[][] prime(BitSet extent) {
		int pos = extent.nextSetBit(0);
		double[][] prime = new double[attCount][2];

		for (int i = 0; i < prime.length; i++) {
			prime[i][0] = data[pos][i];
			prime[i][1] = data[pos][i];
		}

		for (int i = pos; i >= 0 && i < data.length; i = extent.nextSetBit(i + 1))
			for (int j = 0; j < prime.length; j++) {
				if (data[i][j] < prime[j][0])
					prime[j][0] = data[i][j];
				if (data[i][j] > prime[j][1])
					prime[j][1] = data[i][j];
			}
		return prime;
	}

	/**
	 * Initialize the list of ordered values for each attribute. Will be used
	 * for getting minimal changes. log(n) operations
	 * 
	 * return domains size in byte if we use an array (rather than a treeSet
	 */
	@SuppressWarnings("unchecked")
	public void initDomains() {
		domains = new TreeSet[attCount];
		for (int i = 0; i < attCount; i++)
			domains[i] = new TreeSet<Double>();

		for (int i = 0; i < objCount; i++)
			for (int j = 0; j < attCount; j++)
				domains[j].add(data[i][j]);
	}

	/**
	 * Canonicity test
	 * 
	 * @param B
	 *            has been generated with a minimal change
	 * @param D
	 *            is the closure of B
	 * @param att
	 *            the current attribute on which minimal change has been applied
	 * @return
	 */
	protected boolean isCanonical(double[][] B, double[][] D, int att) {
		for (int i = 0; i < att; i++)
			if (B[i][0] != D[i][0] || (B[i][1] != D[i][1]))
				return false;
		return true;
	}
}