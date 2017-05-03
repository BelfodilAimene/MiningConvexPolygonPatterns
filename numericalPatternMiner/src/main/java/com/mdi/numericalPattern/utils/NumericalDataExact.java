package com.mdi.numericalPattern.utils;

import java.math.BigDecimal;

/**
 * A class to represent a numerical data set
 * 
 *
 */
public class NumericalDataExact {

	// object values
	public final BigDecimal[][] values;

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

	public NumericalDataExact(BigDecimal[][] values, String[] lineId, String[] numericalAttributesId,
			String[] classLabels) {
		this.values = values;
		this.lineId = lineId;
		this.numericalAttributesId = numericalAttributesId;
		this.classLabels = classLabels;
		this.dim = numericalAttributesId.length;
		this.objCount = values.length;
	}
}
