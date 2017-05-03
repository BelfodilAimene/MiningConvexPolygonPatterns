package com.mdi.numericalPattern.polygons.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mdi.numericalPattern.polygons.constrained.ConstrainedPolygonsAlgorithm;
import com.mdi.numericalPattern.polygons.constrained.DelaunayEnum;
import com.mdi.numericalPattern.polygons.constrained.ExtCbo;
import com.mdi.numericalPattern.polygons.constrained.ExtremePointsEnum;

public class PolygonsEnumerationTest {
	private final static String[] FILENAMES = new String[] { "data/irisSLSW/irisSLSW-10.csv" };
	private final static int[][] PRECISIONS = new int[][] { new int[] { 1 } };
	private final static int[][] MINSUPPORTS = new int[][] { new int[] { 0, 5, 10 } };
	private final static int[][] MAXSHAPE = new int[][] { new int[] { -1, 2, 5 } };
	private final static BigDecimal[][] MINAREA = new BigDecimal[][] {
			new BigDecimal[] { new BigDecimal(-1), new BigDecimal(0.5) } };
	private final static BigDecimal[][] MAXAREA = new BigDecimal[][] {
			new BigDecimal[] { new BigDecimal(-1), new BigDecimal(2) } };
	private final static double[][] MINPERIMETER = new double[][] { new double[] { 1, 3 } };
	private final static double[][] MAXPERIMETER = new double[][] { new double[] { -1, 1, 3 } };

	@BeforeClass
	public static void desactivatePrints() {

		System.setOut(new PrintStream(new OutputStream() {

			@Override
			public void write(int b) throws IOException {
			}
		}));
	}

	@Test
	public void testEnumeration() throws IOException {
		for (int i = 0; i < FILENAMES.length; i++) {
			String filename = FILENAMES[i];
			for (int precision : PRECISIONS[i]) {
				for (int minSupport : MINSUPPORTS[i]) {
					for (int maxShape : MAXSHAPE[i]) {
						for (BigDecimal minArea : MINAREA[i]) {
							for (BigDecimal maxArea : MAXAREA[i]) {
								for (double minPerimeter : MINPERIMETER[i]) {
									for (double maxPerimeter : MAXPERIMETER[i]) {
										testEnumeration(filename, precision, minSupport, maxShape, minArea, maxArea,
												minPerimeter, maxPerimeter);
									}
								}

							}
						}

					}

				}
			}
		}
	}

	public void testEnumeration(String filename, int precision, int minSup, int maxShape, BigDecimal minArea,
			BigDecimal maxArea, double minPerimeter, double maxPerimeter) throws IOException {
		int countExtremePoints = ConstrainedPolygonsAlgorithm.doTests(ExtremePointsEnum.class.getSimpleName(), filename,
				precision, minSup, maxShape, minArea, maxArea, minPerimeter, maxPerimeter, 1, false);
		int countDelaunay = ConstrainedPolygonsAlgorithm.doTests(DelaunayEnum.class.getSimpleName(), filename,
				precision, minSup, maxShape, minArea, maxArea, minPerimeter, maxPerimeter, 1, false);
		int countExtCbO = ConstrainedPolygonsAlgorithm.doTests(ExtCbo.class.getSimpleName(), filename, precision,
				minSup, maxShape, minArea, maxArea, minPerimeter, maxPerimeter, 1, false);
		String helpMessage = filename + ", precision = " + precision + ", minsup = " + minSup + ", maxshape = "
				+ maxShape + ", minArea = " + minArea + ", maxArea = " + maxArea + ", minPerimeter = " + minPerimeter
				+ ", maxPerimeter = " + maxPerimeter;
		assertEquals(helpMessage, countExtremePoints, countDelaunay);
		assertEquals(helpMessage, countExtremePoints, countExtCbO);
	}
}
