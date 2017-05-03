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

#ifndef POLYTOPNAIVEBOTTOMUPMINER_H
#define POLYTOPNAIVEBOTTOMUPMINER_H

#include "Utils.h"

using namespace std;
using boost::dynamic_bitset;

class PolytopNaiveBottomUpMiner {
public:
    PolytopNaiveBottomUpMiner(Dataset& dataset, int maxShapeComplexity = -1, int minSupport = -1, double minDensity = -1,  double minPerimeter = -1, double maxPerimeter = -1, double minArea = -1, double maxArea = -1, bool printPattern = false);
    unsigned int start();   
    unsigned int getNbObject() {return nbObject;};
    int getMaxShapeComplexity() {return maxShapeComplexity;};
    int getMinSupport() {return minSupport;};
    double getMinDensity() {return minDensity;};
    double getMinPerimeter() {return minPerimeter;};
    double getMaxPerimeter() {return maxPerimeter;};
    double getMinArea() {return minArea;};
    double getMaxArea() {return maxArea;};
    
    unsigned int getNumberOfVisitedPatterns() {return numberOfVisitedPatterns;}; 
    ~PolytopNaiveBottomUpMiner();
    
    static const char* PROG_NAME;
    static void doTest(const char* csv_file, unsigned int precision, int maxShapeComplexity = -1, int minSupport = -1, double minDensity = -1,  double minPerimeter = -1, double maxPerimeter = -1, double minArea = -1, double maxArea = -1,  unsigned int nbTest = 1, bool printPattern = false);
    
private:
    Dataset dataset;
    unsigned int nbObject;
    int maxShapeComplexity;
    int minSupport;
    double minDensity;
    double minPerimeter;
    double maxPerimeter;
    double minArea;
    double maxArea;
    
    bool printPattern;
    
    unsigned int numberOfVisitedPatterns;
    
    void initIndex();
    unsigned int enumerate(dynamic_bitset<>* extent, Polygon* intent, int startingPointIndex);
};

#endif /* POLYTOPNAIVEBOTTOMUPMINER_H */

