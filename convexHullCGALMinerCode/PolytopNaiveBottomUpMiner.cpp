/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   PolytopBottomUpMiner.cpp
 * Author: aimene
 * 
 * Created on February 2, 2017, 8:28 AM
 */

#include "PolytopNaiveBottomUpMiner.h"
#include "Utils.h"

const char* PolytopNaiveBottomUpMiner::PROG_NAME = "cpolynaivebottomup";

PolytopNaiveBottomUpMiner::PolytopNaiveBottomUpMiner(Dataset& dataset, int maxShapeComplexity, int minSupport, double minDensity, double minPerimeter, double maxPerimeter, double minArea, double maxArea, bool printPattern) {
    this->dataset = dataset;
    this->nbObject = dataset.points.size(); 
    this->maxShapeComplexity = maxShapeComplexity;
    this->minSupport = minSupport;
    this->minDensity = minDensity;
    this->minPerimeter = minPerimeter;
    this->maxPerimeter = maxPerimeter;
    this->minArea = minArea;
    this->maxArea = maxArea;
    this->printPattern = printPattern;
    this->numberOfVisitedPatterns = 0;
}

PolytopNaiveBottomUpMiner::~PolytopNaiveBottomUpMiner() {
}

unsigned int PolytopNaiveBottomUpMiner::start() {
    if (minSupport>=0 && nbObject < minSupport) {
        return 0;
    }
    dynamic_bitset<>* extent = new dynamic_bitset<>(nbObject);
    Polygon* intent = new Polygon();
    numberOfVisitedPatterns = 1;
    unsigned int count = 0;
    count = enumerate(extent, intent, 0);
    delete extent;
    delete intent;
    return count;
}

unsigned int PolytopNaiveBottomUpMiner::enumerate(dynamic_bitset<>* extent, Polygon* intent, int startingPointIndex) {
    unsigned int count;
    Polygon* new_intent;
    K traits;
    
    double perimeter = Utils::perimeter(intent);
    double area = abs(CGAL::to_double(intent->area()));
    
    bool maxPerimeterConstraint = (maxPerimeter<0) || (perimeter <= maxPerimeter);
    bool maxAreaConstraint = (maxArea<0) || (area <= maxArea);
    if (!(maxPerimeterConstraint && maxAreaConstraint)) {
        return 0;
    }
    
    bool maxShapeConstraint = (maxShapeComplexity<0) || (intent->size() <= maxShapeComplexity);
    bool minSupportConstraint = (minSupport<0) || (extent->count() >= minSupport);
    bool minDensityConstraint = (minDensity<0) || (extent->count()>0 && extent->count() >= area * minDensity) || (extent->count() == 0 && minDensity == 0);
    bool minPerimeterConstraint = (minPerimeter<0) || (perimeter >= minPerimeter);
    bool minAreaConstraint = (minArea<0) || (area >= minArea);
    
    bool valid = maxShapeConstraint && minSupportConstraint &&  minDensityConstraint && minPerimeterConstraint && minAreaConstraint;
    
    count = valid ? 1 : 0;
    if (valid && printPattern) {
        cout << Utils::toStringPattern(extent, intent, dataset) << endl;
    }
    
    dynamic_bitset<>* new_extent;
    dynamic_bitset<>* to_test_points;
    
    for (int i = startingPointIndex; i<nbObject; i++) {
        Points points = Points(intent->vertices_begin(),intent->vertices_end());
        Point new_point = dataset.points.at(i);
        points.push_back(new_point);
        
        Points convexHull = Points();
        CGAL::convex_hull_2(points.begin(), points.end(), std::back_inserter(convexHull));
        new_intent = new Polygon(convexHull.begin(), convexHull.end());
        
        new_extent = new dynamic_bitset<>(*extent);
        to_test_points = new dynamic_bitset<>(*extent);
        to_test_points->flip();
        bool cannonicity = true;
        int nextStartingPointIndex = i;
        for (int j = to_test_points->find_first(); j != dynamic_bitset<>::npos && cannonicity; j = to_test_points->find_next(j)) {
            Point tmp_point = dataset.points.at(j);
            if (new_intent->size() == 1) {
                if (tmp_point == new_point) {
                    if (j<i) {
                        cannonicity = false;
                        break;
                    }
                    new_extent->flip(j);
                } else {
                    if (nextStartingPointIndex == i && j>i) {
                        nextStartingPointIndex = j;
                    }
                }
            } else {
                switch(CGAL::bounded_side_2(new_intent->vertices_begin(), new_intent->vertices_end(), tmp_point, traits)) {
                    case CGAL::ON_BOUNDED_SIDE :
                        if (j<i) {
                            cannonicity = false;
                            break;
                        }
                        new_extent->flip(j);
                        break;
                    case CGAL::ON_BOUNDARY:
                        if (j<i) {
                            cannonicity = false;
                            break;
                        }
                        new_extent->flip(j);
                        break;
                    case CGAL::ON_UNBOUNDED_SIDE :
                        if (nextStartingPointIndex == i && j>i) {
                            nextStartingPointIndex = j;
                        }
                        break;
                }
            }
        }
        cannonicity = cannonicity && (new_extent->count() > extent->count());
        numberOfVisitedPatterns+=1;
        if (cannonicity) {
            if (nextStartingPointIndex == i) nextStartingPointIndex = nbObject;
            count+=enumerate(new_extent, new_intent, nextStartingPointIndex);
        }
        delete new_intent;
        delete new_extent;
    }
    return count;
}

void PolytopNaiveBottomUpMiner::doTest(const char* csv_file, unsigned int precision, int maxShapeComplexity, int minSupport,  double minDensity, double minPerimeter, double maxPerimeter, double minArea, double maxArea, unsigned int nbTest, bool printPattern) {
  cerr << "Enumeration mode: " << "Naive Bottom Up" << endl;
  cerr << "File: " << csv_file << endl;
  cerr << "Precision: " << precision << endl;
  cerr << "Number of test: " << nbTest << endl;
  
  Dataset dataset = Utils::readPoints(csv_file, precision);
  
  cerr << "Number of objects: " << dataset.points.size() << endl;
  cerr << "Maximum shape complexity: " << maxShapeComplexity << endl;
  cerr << "Minimum support: " << minSupport << endl;
  cerr << "Minimum density: " << minDensity << endl;
  cerr << "Minimum perimeter: " << minPerimeter << endl;
  cerr << "Maximum perimeter: " << maxPerimeter << endl;
  cerr << "Minimum area: " << minArea << endl;
  cerr << "Maximum area: " << maxArea << endl;
  
  if (!printPattern) {
      cout << "\"" << PolytopNaiveBottomUpMiner::PROG_NAME << "\",\"" << csv_file << "\"," << precision << "," << dataset.points.size() << "," << maxShapeComplexity << "," << minSupport << "," << minDensity << "," << minPerimeter << "," << maxPerimeter << "," << minArea << "," << maxArea << "," ;
  }
  
  int start_s,stop_s;
  unsigned int count;
  
  for (int i = 0; i<nbTest; i++) {
      start_s=clock();
      PolytopNaiveBottomUpMiner polytopMiner(dataset, maxShapeComplexity, minSupport, minDensity, minPerimeter, maxPerimeter, minArea, maxArea, printPattern);
      count = polytopMiner.start();
      stop_s=clock();
      if (i==0) {
          cerr << " > Number of simple closed itemset (convex hulls): " << count << endl;
          cerr << " > Number of visited patterns: " << polytopMiner.getNumberOfVisitedPatterns() << endl;
          if (!printPattern) {
              cout << count << "," <<  polytopMiner.getNumberOfVisitedPatterns() << ",";
          }
      }
      double execTimeMs = (stop_s-start_s)/double(CLOCKS_PER_SEC)*1000;
      cerr << " > Test " << (i+1) << " exec time: " << execTimeMs << " ms" << endl;
      if (!printPattern) {
          cout << execTimeMs << ";";
      }
  }
  if (!printPattern) {
      cout << endl;
  }
  cerr << "-------------------------" << endl;
}

