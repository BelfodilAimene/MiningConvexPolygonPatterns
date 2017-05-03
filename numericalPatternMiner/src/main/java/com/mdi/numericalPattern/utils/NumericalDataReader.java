package com.mdi.numericalPattern.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class NumericalDataReader {
	public final static int DEFAULT_NUMERICAL_PRECISION = 4;
	public final static String DEFAULT_CSV_SEPARATOR = ",";
	public final static boolean DEFAULT_HAVE_CLASS_ATTRIBUTE = false;

	/**
	 * 
	 * @param filename
	 *            .csv file organized as follows: - the first row contains the
	 *            header (row_id, numerical attribute 1, .., numerical attribute
	 *            d, [class label] - the first column indicate the row ids - the
	 *            last column (if haveClassAttribute is set) designate the class
	 *            column
	 * @param csvSeparator
	 * @param classAttributeName
	 * @return NumericalDataExact
	 * @throws IOException,
	 *             RuntimeException
	 */
	public static final NumericalDataExact readExact(String filename, String csvSeparator, boolean haveClassAttribute,
			int numericalPrecision) throws IOException {
		ArrayList<BigDecimal[]> values = new ArrayList<>();
		ArrayList<String> classLabels = new ArrayList<>();
		ArrayList<String> lineIds = new ArrayList<>();

		String[] numericalAttributesId = null;
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		String[] elements;
		BigDecimal[] value;
		int i = 0;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			elements = line.split(csvSeparator);
			if (i == 0) {
				numericalAttributesId = new String[elements.length - (haveClassAttribute ? 2 : 1)];
				for (int j = 0; j < numericalAttributesId.length; j++) {
					numericalAttributesId[j] = elements[j + 1];
				}
			} else {
				lineIds.add(elements[0]);
				value = new BigDecimal[numericalAttributesId.length];
				for (int j = 0; j < numericalAttributesId.length; j++) {
					value[j] = new BigDecimal(elements[j + 1]).setScale(numericalPrecision, RoundingMode.DOWN);
				}
				values.add(value);
				if (haveClassAttribute) {
					classLabels.add(elements[elements.length - 1]);
				}
			}
			i++;
		}
		if (numericalAttributesId == null) {
			reader.close();
			throw new RuntimeException(filename + " is an empty array");
		}

		String[] lineIdsArray = new String[lineIds.size()];
		lineIds.toArray(lineIdsArray);

		BigDecimal[][] valuesArray = new BigDecimal[values.size()][];
		values.toArray(valuesArray);

		String[] classLabelsArray = new String[classLabels.size()];
		classLabels.toArray(classLabelsArray);

		reader.close();

		NumericalDataExact result = new NumericalDataExact(valuesArray, lineIdsArray, numericalAttributesId,
				classLabelsArray);

		return result;
	}

	public static final NumericalDataExact readExact(String filename,
			int numericalPrecision, boolean haveClassAttribute) throws IOException {
		return readExact(filename, DEFAULT_CSV_SEPARATOR, haveClassAttribute, numericalPrecision);
	}

	public static final NumericalDataExact readExact(String filename, boolean haveClassAttribute) throws IOException {
		return readExact(filename, DEFAULT_CSV_SEPARATOR, haveClassAttribute, DEFAULT_NUMERICAL_PRECISION);
	}

	public static final NumericalDataExact readExact(String filename, String csvSeparator, int numericalPrecision)
			throws IOException {
		return readExact(filename, csvSeparator, DEFAULT_HAVE_CLASS_ATTRIBUTE, numericalPrecision);
	}

	public static final NumericalDataExact readExact(String filename, String csvSeparator, boolean haveClassAttribute)
			throws IOException {
		return readExact(filename, csvSeparator, haveClassAttribute, DEFAULT_NUMERICAL_PRECISION);
	}

	public static final NumericalDataExact readExact(String filename, int numericalPrecision) throws IOException {
		return readExact(filename, DEFAULT_CSV_SEPARATOR, DEFAULT_HAVE_CLASS_ATTRIBUTE, numericalPrecision);
	}

	public static final NumericalDataExact readExact(String filename, String csvSeparator) throws IOException {
		return readExact(filename, csvSeparator, DEFAULT_HAVE_CLASS_ATTRIBUTE, DEFAULT_NUMERICAL_PRECISION);
	}

	public static final NumericalDataExact readExact(String filename) throws IOException {
		return readExact(filename, DEFAULT_CSV_SEPARATOR, DEFAULT_HAVE_CLASS_ATTRIBUTE, DEFAULT_NUMERICAL_PRECISION);
	}

	public static final NumericalData read(String filename, int precision, String csvSeparator, boolean haveClassAttribute) throws IOException {
		double multiplier = 1;
		for (int i = 0; i< precision; i++) {
			multiplier*=10;
		}
		ArrayList<double[]> values = new ArrayList<>();
		ArrayList<String> classLabels = new ArrayList<>();
		ArrayList<String> lineIds = new ArrayList<>();

		String[] numericalAttributesId = null;
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		String[] elements;
		double[] value;
		int i = 0;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				continue;
			elements = line.split(csvSeparator);
			if (i == 0) {
				numericalAttributesId = new String[elements.length - (haveClassAttribute ? 2 : 1)];
				for (int j = 0; j < numericalAttributesId.length; j++) {
					numericalAttributesId[j] = elements[j + 1];
				}
			} else {
				lineIds.add(elements[0]);
				value = new double[numericalAttributesId.length];
				for (int j = 0; j < numericalAttributesId.length; j++) {
					value[j] = Math.round(multiplier*Double.parseDouble(elements[j + 1]))/multiplier;
				}
				values.add(value);
				if (haveClassAttribute) {
					classLabels.add(elements[elements.length - 1]);
				}
			}
			i++;
		}
		if (numericalAttributesId == null) {
			reader.close();
			throw new RuntimeException(filename + " is an empty array");
		}

		String[] lineIdsArray = new String[lineIds.size()];
		lineIds.toArray(lineIdsArray);

		double[][] valuesArray = new double[values.size()][];
		values.toArray(valuesArray);

		String[] classLabelsArray = new String[classLabels.size()];
		classLabels.toArray(classLabelsArray);

		reader.close();

		NumericalData result = new NumericalData(valuesArray, lineIdsArray, numericalAttributesId, classLabelsArray);

		return result;
	}

	public static final NumericalData read(String filename, boolean haveClassAttribute) throws IOException {
		return read(filename, DEFAULT_NUMERICAL_PRECISION, DEFAULT_CSV_SEPARATOR, haveClassAttribute);
	}

	public static final NumericalData read(String filename, String csvSeparator) throws IOException {
		return read(filename, DEFAULT_NUMERICAL_PRECISION, csvSeparator, DEFAULT_HAVE_CLASS_ATTRIBUTE);
	}

	public static final NumericalData read(String filename) throws IOException {
		return read(filename, DEFAULT_NUMERICAL_PRECISION, DEFAULT_CSV_SEPARATOR, DEFAULT_HAVE_CLASS_ATTRIBUTE);
	}

	public static final NumericalData read(String filename, int precision, boolean haveClassAttribute)
			throws IOException {
		return read(filename, precision, DEFAULT_CSV_SEPARATOR, haveClassAttribute);
	}
	
	public static final NumericalData read(String filename, int precision)
			throws IOException {
		return read(filename, precision, DEFAULT_CSV_SEPARATOR, DEFAULT_HAVE_CLASS_ATTRIBUTE);
	}
}
