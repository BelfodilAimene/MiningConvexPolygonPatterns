package com.mdi.numericalPattern.hyperrectangles;

import static com.mdi.numericalPattern.utils.NumericalDataReader.read;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import com.mdi.numericalPattern.hyperrectangles.utils.Measure;
import com.mdi.numericalPattern.utils.NumericalData;

/**
 * This abstract class represents an algorithm with common methods and
 * initialization
 * 
 *
 */
public abstract class HyperrectanglesAlgorithm {

	protected final NumericalData numData;
	protected final double[][] data;
	protected final int objCount, attCount;
	protected final int minSup;
	protected final boolean printPatterns;
	private final Measure[] measures = new Measure[] { new Support(), new Volume(), new Gini(), new Entropy() };

	public static int doTests(String algoname, String filename, int precision, int minSupport, int nbTest,
			boolean printPattern) throws IOException {
		long instantTimeNs;
		double deltatimeMs;
		int count = 0;
		nbTest = (nbTest < 1 || printPattern) ? 1 : nbTest;
		minSupport = minSupport < 1 ? 1 : minSupport;

		System.err.println("Algorithm: " + algoname);
		System.err.println("File: " + filename);
		System.err.println("Precision: " + precision);
		System.err.println("Number of test: " + nbTest);
		NumericalData data = read(filename, precision, true);
		System.err.println("Number of objects: " + data.values.length);
		System.err.println("Minumim support: " + minSupport);
		System.err.println("Will print patterns? " + printPattern);
		if (!printPattern)
			System.out.print(algoname + "," + '"' + filename + '"' + ',' + precision + "," + data.values.length + ","
					+ minSupport + ",");

		for (int i = 0; i < nbTest; i++) {
			instantTimeNs = System.nanoTime();
			HyperrectanglesAlgorithm algo;
			try {
				algo = (HyperrectanglesAlgorithm) Class
						.forName(HyperrectanglesAlgorithm.class.getPackage().getName() + "." + algoname)
						.getConstructor(NumericalData.class, int.class, boolean.class)
						.newInstance(data, minSupport, printPattern);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				throw new IllegalArgumentException("Inexistant hyperrectangle enumeration algotithm " + algoname);
			}
			if (printPattern) {
				String header = "Intent";
				for (Measure measure : algo.measures) {
					header += "," + measure.getClass().getSimpleName();
				}
				System.out.println(header);
			}
			count = algo.start();
			if (i == 0) {
				System.err.println(" > Number of frequent closed itemset (minimal bounding box): " + count);
				if (!printPattern)
					System.out.print(count + ",");
			}
			deltatimeMs = (System.nanoTime() - instantTimeNs) / 1000000.;
			if (!printPattern)
				System.out.print(deltatimeMs + ";");

			System.err.println(" > Test " + (i + 1) + " exec time: " + deltatimeMs + " ms");
		}
		if (!printPattern)
			System.out.println();
		return count;
	}

	/**
	 * Constructor
	 * 
	 * @param data
	 * @param minSup
	 */
	protected HyperrectanglesAlgorithm(NumericalData d, int minSup, boolean printPatterns) {
		this.numData = d;
		this.data = d.values;
		this.objCount = data.length;
		this.attCount = objCount == 0 ? 0 : data[0].length;
		this.minSup = minSup < 1 ? 1 : minSup;
		this.printPatterns = printPatterns;
	}

	/**
	 * The algorithm. return the number of patterns
	 */
	public abstract int start();

	private class Support implements Measure {
		@Override
		public Double apply(BitSet t, double[][] u) {
			return (double) t.cardinality();
		}
	}

	private class Volume implements Measure {
		@Override
		public Double apply(BitSet t, double[][] u) {
			double volume = 1;
			for (int i = 0; i < u.length; i++) {
				volume *= (u[i][1] - u[i][0]);
			}
			return volume;
		}
	}

	private class Gini implements Measure {

		@Override
		public Double apply(BitSet t, double[][] u) {
			if (t.cardinality() == 0)
				return 1.;
			Map<String, Integer> supportPerClass = supportPerClass(t);
			double gini = 1;
			double n2 = t.cardinality();
			n2 *= n2;
			for (int ni : supportPerClass.values()) {
				double niSquare = ni * ni;
				gini -= niSquare / n2;
			}
			return gini;
		}
	}

	private class Entropy implements Measure {

		@Override
		public Double apply(BitSet t, double[][] u) {
			if (t.cardinality() == 0)
				return 0.;
			Map<String, Integer> supportPerClass = supportPerClass(t);
			double entropy = 0;
			for (int ni : supportPerClass.values()) {
				double pi = ni / (1. * t.cardinality());
				entropy -= ((double) pi) * Math.log(pi);
			}
			return entropy;
		}
	}

	protected String toStringPattern(BitSet extent, double[][] intent) {
		String result = toStringIntent(intent);
		for (Measure measure : measures) {
			result += "," + measure.apply(extent, intent);
		}
		return result;
	}

	/**
	 * Builds a string representation "a_1 b_1;...;a_k b_k" for a given interval
	 * pattern
	 * 
	 * @param intent
	 *            an interval pattern
	 * @return its string representation
	 */
	protected String toStringIntent(double[][] intent) {
		String res = "";
		for (int i = 0; i < intent.length; i++)
			res += intent[i][0] + " " + intent[i][1] + ";";
		return res;
	}

	/**
	 * A set of object is represented by a bitset This method allows returns set
	 * of associated object labels.
	 * 
	 * @param extent
	 *            bit set representation of an object set
	 * @return String representation of an object set
	 */
	protected String toStringExtent(BitSet extent) {
		String res = "Image: {";
		for (int i = extent.nextSetBit(0); i != -1; i = extent.nextSetBit(i + 1))
			res += this.numData.lineId[i] + ",";
		return res.substring(0, res.length() - 1) + "}";
	}

	/**
	 * To make a clone (exact copy but not same reference) of a pattern
	 * 
	 * @param pattern
	 *            the pattern to clone
	 * @return the clone
	 */
	protected double[][] cloneIntent(double[][] intent) {
		double[][] clone = new double[intent.length][2];
		for (int i = 0; i < clone.length; i++) {
			clone[i][0] = intent[i][0];
			clone[i][1] = intent[i][1];
		}
		return clone;
	}

	/**
	 * 
	 */
	private Map<String, Integer> supportPerClass(BitSet extent) {
		Map<String, Integer> supportPerClass = new HashMap<>();
		for (int i = extent.nextSetBit(0); i != -1; i = extent.nextSetBit(i + 1)) {
			String label = numData.classLabels[i];
			Integer value = supportPerClass.get(label);
			if (value == null) {
				supportPerClass.put(label, 1);
			} else {
				supportPerClass.put(label, value + 1);
			}
		}
		return supportPerClass;
	}
}
