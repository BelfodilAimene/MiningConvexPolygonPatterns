package com.mdi.numericalPattern.polygonsMCTS.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mdi.numericalPattern.polygons.sampling.MCTSExtremePointsEnum;

public class MCTSExtremePointsEnumTest {
	private final static String[] FILENAMES = new String[] { "data/irisSLSW/irisSLSW-10.csv" };
	private final static int[] EXPECTEDS = new int[] { 355 };

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
			testEnumeration(filename, 5, EXPECTEDS[i]);
		}
	}

	public void testEnumeration(String filename, int precision, int expected) throws IOException {
		int countMCTSExtremePoints = MCTSExtremePointsEnum.doTest(filename, precision, -1, false, -1, -1, false);
		assertEquals(expected, countMCTSExtremePoints);
	}
}
