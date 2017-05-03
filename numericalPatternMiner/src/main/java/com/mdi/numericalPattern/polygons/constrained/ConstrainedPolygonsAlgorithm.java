package com.mdi.numericalPattern.polygons.constrained;

import static com.mdi.numericalPattern.utils.NumericalDataReader.readExact;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import com.mdi.numericalPattern.polygons.PolygonsAlgorithm;
import com.mdi.numericalPattern.utils.NumericalDataExact;

public abstract class ConstrainedPolygonsAlgorithm extends PolygonsAlgorithm {
	protected final int minSup, maxShape;
	protected final double minPerimeter, maxPerimeter;
	protected final BigDecimal minArea, maxArea;

	public static int doTests(String algoname, String filename, int precision, int minSupport, int maxShape,
			BigDecimal minArea, BigDecimal maxArea, double minPerimeter, double maxPerimeter, int nbTest,
			boolean printPattern) throws IOException {

		long instantTimeNs;
		double deltatimeMs;
		nbTest = (nbTest < 1 || printPattern) ? 1 : nbTest;
		int count = 0;

		System.err.println("Algorithm: " + algoname);
		System.err.println("File: " + filename);
		System.err.println("Precision: " + precision);
		System.err.println("Number of test: " + nbTest);

		NumericalDataExact data = readExact(filename, precision, true);

		System.err.println("Number of objects: " + data.values.length);
		System.err.println("Minumim support: " + minSupport);
		System.err.println("Maximum shape complexity: " + maxShape);
		System.err.println("Minimum area: " + minArea);
		System.err.println("Maximum area: " + maxArea);
		System.err.println("Minimum perimeter: " + minPerimeter);
		System.err.println("Maximum perimeter: " + maxPerimeter);

		System.err.println("Will print patterns? " + printPattern);

		if (!printPattern)
			System.out.print(algoname + ',' + '"' + filename + '"' + ',' + precision + "," + data.values.length + ","
					+ minSupport + "," + maxShape + "," + minArea + "," + maxArea + "," + minPerimeter + ","
					+ maxPerimeter + ",");

		for (int i = 0; i < nbTest; i++) {
			instantTimeNs = System.nanoTime();
			ConstrainedPolygonsAlgorithm algo;
			try {
				algo = (ConstrainedPolygonsAlgorithm) Class
						.forName(ConstrainedPolygonsAlgorithm.class.getPackage().getName() + "." + algoname)
						.getConstructor(NumericalDataExact.class, int.class, int.class, BigDecimal.class,
								BigDecimal.class, double.class, double.class, boolean.class)
						.newInstance(data, minSupport, maxShape, minArea, maxArea, minPerimeter, maxPerimeter,
								printPattern);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				throw new IllegalArgumentException("Inexistant polygon enumeration algorithm " + algoname);
			}

			if (printPattern) {
				String header = "Intent";
				for (Measure measure : algo.measures) {
					header += "," + measure.getClass().getSimpleName();
				}
				System.out.println(header);
			}
			algo.start();
			if (i == 0) {
				System.err.println(" > Number of frequent closed itemset: " + algo.getValidPatternCount());
				System.err.println(" > Number of visited itemset: " + algo.getVisitedPatternCount());
				count = algo.getValidPatternCount();
				if (!printPattern) {
					System.out.print(algo.getValidPatternCount() + "," + algo.getVisitedPatternCount() + ",");
				}
			}
			deltatimeMs = (System.nanoTime() - instantTimeNs) / 1000000.;
			if (!printPattern) {
				System.out.print(deltatimeMs + ";");
			}

			System.err.println(" > Test " + (i + 1) + " exec time: " + deltatimeMs + " ms");
		}
		if (!printPattern)
			System.out.println();
		return count;
	}

	protected ConstrainedPolygonsAlgorithm(NumericalDataExact numData, int minSup, int maxShape, BigDecimal minArea,
			BigDecimal maxArea, double minPerimeter, double maxPerimeter, boolean printPatterns) {
		super(numData, printPatterns);
		this.minSup = minSup;
		this.maxShape = maxShape;
		this.minArea = minArea;
		this.maxArea = maxArea;
		this.minPerimeter = minPerimeter;
		this.maxPerimeter = maxPerimeter;
	}

	public abstract int getValidPatternCount();

	public abstract int getVisitedPatternCount();
}
