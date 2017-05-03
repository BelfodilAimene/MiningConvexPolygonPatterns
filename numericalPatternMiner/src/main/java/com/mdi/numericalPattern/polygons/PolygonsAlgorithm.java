package com.mdi.numericalPattern.polygons;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.mdi.numericalPattern.utils.NumericalDataExact;

public abstract class PolygonsAlgorithm {
	protected final NumericalDataExact numData;
	protected final BigDecimal[][] data;
	protected final int objCount;
	protected final boolean printPatterns;

	protected final Measure[] measures = new Measure[] { new Complexity(), new Support(), new Area(), new Perimeter(),
			new Gini(), new Entropy() };

	protected PolygonsAlgorithm(NumericalDataExact numData, boolean printPatterns) {
		if (numData.values[0].length != 2) {
			throw new IllegalArgumentException(
					"Polygon enumeration algorithm accept only spatial dataset (2 numerical attributes)");
		}
		this.numData = numData;
		this.data = numData.values;
		this.objCount = data.length;
		this.printPatterns = printPatterns;
	}

	public abstract void start();

	private class Complexity implements Measure {
		@Override
		public Double apply(PolygonPattern t) {
			return (double) t.complexity;
		}
	}

	private class Support implements Measure {
		@Override
		public Double apply(PolygonPattern t) {
			return (double) t.support;
		}
	}

	private class Area implements Measure {
		@Override
		public Double apply(PolygonPattern t) {
			return t.area.doubleValue();
		}
	}

	private class Perimeter implements Measure {
		@Override
		public Double apply(PolygonPattern t) {
			return t.perimeter;
		}
	}

	private class Gini implements Measure {

		@Override
		public Double apply(PolygonPattern t) {
			return t.gini;
		}
	}

	private class Entropy implements Measure {

		@Override
		public Double apply(PolygonPattern t) {
			return t.entropy;
		}
	}

	protected interface Measure extends Function<PolygonPattern, Double> {
	};

	protected class PolygonPattern {
		public final BigDecimal[][] intent;
		public final BitSet extent;
		public final int complexity;
		public final int support;
		public final BigDecimal area;
		public final double perimeter;
		public final double gini;
		public final double entropy;
		public final double maxClassSupport;

		public PolygonPattern(BigDecimal[][] intent, BitSet extent, BigDecimal area, double perimeter) {
			this.intent = intent;
			this.extent = extent;
			this.complexity = intent.length;
			this.support = extent.cardinality();
			this.area = area;
			this.perimeter = perimeter;

			Map<String, Integer> supportPerClass = supportPerClass(extent);
			double entropy = 0;
			double gini = 1;
			double maxClassSupport = 0;
			for (int ni : supportPerClass.values()) {
				double pi = ni / (1.*support);
				entropy -= pi * Math.log(pi);
				gini -= pi * pi;
				if (ni > maxClassSupport)
					maxClassSupport = ni;
			}

			this.gini = gini;
			this.entropy = entropy;
			this.maxClassSupport = maxClassSupport;
		}

		private String toStringIntent() {
			String result = "";
			for (BigDecimal[] point : intent)
				result += point[0] + " " + point[1] + ";";
			return result;
		}

		@Override
		public String toString() {
			String result = this.toStringIntent();
			for (Measure measure : measures) {
				result += "," + measure.apply(this);
			}
			return result;
		}
	}

	/**
	 * 
	 * @param extent
	 * @return
	 */
	protected Map<String, Integer> supportPerClass(BitSet extent) {
		Map<String, Integer> proportions = new HashMap<>();
		for (int i = extent.nextSetBit(0); i != -1; i = extent.nextSetBit(i + 1)) {
			String label = numData.classLabels[i];
			Integer value = proportions.get(label);
			if (value == null) {
				proportions.put(label, 1);
			} else {
				proportions.put(label, value + 1);
			}
		}
		return proportions;
	}
}
