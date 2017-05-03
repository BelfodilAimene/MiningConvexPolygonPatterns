package com.mdi.numericalPattern.hyperrectangles.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mdi.numericalPattern.hyperrectangles.HyperrectanglesAlgorithm;
import com.mdi.numericalPattern.hyperrectangles.MinIntChange;
import com.mdi.numericalPattern.hyperrectangles.MinIntChangeIndex;

public class HyperrectanglesEnumerationTest {
	private final static String[] FILENAMES = new String[] { "data/irisSLSW/irisSLSW-10.csv",
			"data/irisSLSW/irisSLSW-20.csv", "data/irisSLSW/irisSLSW-30.csv" };
	private final static int[][] PRECISIONS = new int[][] { new int[] { 1 }, new int[] { 1 }, new int[] { 1 } };
	private final static int[][] MINSUPPORTS = new int[][] { new int[] { 1 }, new int[] { 1 }, new int[] { 10 } };

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
				for (int minSup : MINSUPPORTS[i]) {
					testEnumeration(filename, precision, minSup);
				}
			}
		}
	}

	public void testEnumeration(String filename, int precision, int minSup) throws IOException {
		int count1 = HyperrectanglesAlgorithm.doTests(MinIntChange.class.getSimpleName(), filename, precision, minSup,
				1, false);
		int count2 = HyperrectanglesAlgorithm.doTests(MinIntChangeIndex.class.getSimpleName(), filename, precision,
				minSup, 1, false);

		assertEquals(count1, count2);

	}
}
