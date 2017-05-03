package com.mdi.numericalPattern.utils;

/**
 * A class to represent a numerical data set
 * 
 *
 */
public class NumericalData {
	// object values
	public final double[][] values;

	// object labels
	public final String[] lineId;

	// numerical attribute labels
	public final String[] numericalAttributesId;

	// Class labels (one label per row)
	public final String[] classLabels;

	// Dim count (only numerical)
	public final int dim;

	// Object count (only numerical)
	public final int objCount;

	public NumericalData(double[][] values, String[] lineId, String[] numericalAttributesId, String[] classLabels) {
		this.values = values;
		this.lineId = lineId;
		this.numericalAttributesId = numericalAttributesId;
		this.classLabels = classLabels;
		this.dim = numericalAttributesId.length;
		this.objCount = values.length;
	}
}
