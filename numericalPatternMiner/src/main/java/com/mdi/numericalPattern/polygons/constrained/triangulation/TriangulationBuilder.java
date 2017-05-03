package com.mdi.numericalPattern.polygons.constrained.triangulation;

import java.util.Set;

import com.mdi.numericalPattern.polygons.utils.Point2D;

public interface TriangulationBuilder {
	public TriangulationBuilder set(Set<Point2D> points);
	public Triangulation build();
}
