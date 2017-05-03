/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   PolytopTopDownMiner.h
 * Author: aimene
 *
 * Created on February 2, 2017, 8:22 AM
 */

#ifndef POLYTOPTOPDOWNMINER_H
#define POLYTOPTOPDOWNMINER_H

#include "Utils.h"
#include <CGAL/Triangulation_2.h>
#include <CGAL/Delaunay_triangulation_2.h>

using namespace std;
using boost::dynamic_bitset;

typedef CGAL::Delaunay_triangulation_2<K> Delaunay;
typedef Delaunay::Vertex_circulator Vertex_circulator;
typedef Delaunay::Vertex_iterator Vertex_iterator;
typedef Delaunay::Vertex_handle Vertex_handle;

class PolytopTopDownMiner {
public:
    PolytopTopDownMiner(Dataset& dataset, int maxShapeComplexity = -1, int minSupport = -1, double minDensity = -1,  double minPerimeter = -1, double maxPerimeter = -1, double minArea = -1, double maxArea = -1, bool printPattern = false);
    unsigned int start();   
    unsigned int getNbObject() {return nbObject;};
    unsigned int getNbDistinctObject() {return nbDistinctObject;};
    int getMaxShapeComplexity() {return maxShapeComplexity;};
    int getMinSupport() {return minSupport;};
    double getMinDensity() {return minDensity;};
    double getMinPerimeter() {return minPerimeter;};
    double getMaxPerimeter() {return maxPerimeter;};
    double getMinArea() {return minArea;};
    double getMaxArea() {return maxArea;};
    
    unsigned int getNumberOfVisitedPatterns() {return numberOfVisitedPatterns;}; 
    
    ~PolytopTopDownMiner();
    
    static const char* PROG_NAME;
    static void doTest(const char* csv_file, unsigned int precision, int maxShapeComplexity = -1, int minSupport = -1, double minDensity = -1,  double minPerimeter = -1, double maxPerimeter = -1, double minArea = -1, double maxArea = -1,  unsigned int nbTest = 1, bool printPattern = false);
private:
    Dataset dataset;
    Index index;
    Points distinctPoints;
    unsigned int nbObject;
    unsigned int nbDistinctObject;
    int maxShapeComplexity;
    int minSupport;
    double minDensity;
    double minPerimeter;
    double maxPerimeter;
    double minArea;
    double maxArea;
    
    bool printPattern;
    
    unsigned int numberOfVisitedPatterns;
    
    // For computation
    Delaunay* triangulation;
    
    void initIndex();
    vector<Vertex_handle>* getCurrentCH();
    unsigned int enumerate(dynamic_bitset<>* extent, PointSet* invincibles);
};

#endif /* POLYTOPTOPDOWNMINER_H */

