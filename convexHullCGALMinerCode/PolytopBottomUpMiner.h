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

#ifndef POLYTOPBOTTOMUPMINER_H
#define POLYTOPBOTTOMUPMINER_H

#include "Utils.h"
#include <CGAL/squared_distance_2.h>
#include <CGAL/number_utils.h>
#include <math.h> 

using namespace std;
using boost::dynamic_bitset;

class Pattern {
public:
    Pattern(unsigned int complexity, unsigned int* ccwVertices, dynamic_bitset<>& extent, dynamic_bitset<>*& distinctCandidates, dynamic_bitset<>*& realCandidates ,double perimeter, double area, double gini, double entropy);
    Pattern():complexiy(0), ccwVertices(0), extent(dynamic_bitset<>(0)), distinctCandidates(0), realCandidates(0), support(0), perimeter(0), area(0), gini(1), entropy(0) {};
    Pattern(const Pattern& rhs);
    ~Pattern();
    
    static Pattern emptyPattern();
    
    unsigned int complexiy;
    
    unsigned int* ccwVertices; 
    dynamic_bitset<> extent;
    dynamic_bitset<>* distinctCandidates;
    dynamic_bitset<>* realCandidates;
    
    unsigned int support;
    double perimeter;
    double area;
    double gini;
    double entropy;
};
        
class MySegment {
public:
    MySegment(unsigned int a, unsigned int b, double length, dynamic_bitset<>& realSupport, dynamic_bitset<>& realStrictPositiveSemiPlan, dynamic_bitset<>& realStrictNegativeSemiPlan, dynamic_bitset<>& distinctStrictPositiveSemiPlan, dynamic_bitset<>& distinctStrictNegativeSemiPlan);
    MySegment(const MySegment& rhs);
    MySegment() {};
    ~MySegment();
    
    unsigned int a;
    unsigned int b;
    double length;
    
    dynamic_bitset<> realSupport;
    dynamic_bitset<> realStrictPositiveSemiPlan;
    dynamic_bitset<> realStrictNegativeSemiPlan;
    dynamic_bitset<> distinctStrictPositiveSemiPlan;
    dynamic_bitset<> distinctStrictNegativeSemiPlan;
};

class PolytopBottomUpMiner {
public:
    PolytopBottomUpMiner(Dataset& dataset, int maxShapeComplexity = -1, int minSupport = -1, double minDensity = -1,  double minPerimeter = -1, double maxPerimeter = -1, double minArea = -1, double maxArea = -1, bool printPattern = false);
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
    unsigned int start(unsigned int &realMaxShapeComplexity, unsigned int *&patternCountByShape);
    ~PolytopBottomUpMiner();
    
    static const char* PROG_NAME;
    static void doTest(const char* csv_file, unsigned int precision, int maxShapeComplexity = -1, int minSupport = -1, double minDensity = -1,  double minPerimeter = -1, double maxPerimeter = -1, double minArea = -1, double maxArea = -1,  unsigned int nbTest = 1, bool printPattern = false);
private:
    Dataset dataset;
    Point* distinctPoints;
    dynamic_bitset<>* realExtentPerDistinctPoints;
    map<int, MySegment*>* mySegments; 
    
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
    unsigned int* patternCountByShape;
    unsigned int realMaxShapeComplexity; 
    
    void initIndex();
    MySegment* createSegment(int a, int b);
    unsigned int enumerate(Pattern *p, int startingPointIndex);
    
    Pattern* createPattern(unsigned int complexity, unsigned int* ccwVertices, dynamic_bitset<>& extent, dynamic_bitset<>*& distinctCandidates, dynamic_bitset<>*& realCandidates, double perimeter, double area);
    Pattern* createPatternFromObject(unsigned int a);
    Pattern* createPatternFromSegment(unsigned int a, unsigned int b);
    Pattern* insertOutsideVertex(Pattern *p, unsigned int index, unsigned int vertice);
    
    string toStringPattern(Pattern *p);
};

#endif /* POLYTOPBOTTOMUPMINER_H */

