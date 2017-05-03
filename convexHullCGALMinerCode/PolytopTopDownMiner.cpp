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

#include "PolytopTopDownMiner.h"

const char* PolytopTopDownMiner::PROG_NAME = "cpolytopdown";

PolytopTopDownMiner::PolytopTopDownMiner(Dataset& dataset, int maxShapeComplexity, int minSupport, double minDensity, double minPerimeter, double maxPerimeter, double minArea, double maxArea, bool printPattern) {
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
    initIndex();
    this->nbDistinctObject = this->index.size();
}

PolytopTopDownMiner::~PolytopTopDownMiner() {
}

void PolytopTopDownMiner::initIndex(void) {
   index = Index();
   int row = 0;
   Point tmpPoint;
   for(Points::iterator it = this->dataset.points.begin(); it != this->dataset.points.end(); ++it) {
       tmpPoint = (*it);
       if (index.find(*it) != index.end()) {
            dynamic_bitset<> extent = index[tmpPoint];
            extent.resize(row+1);
            extent.flip(row);
            index[tmpPoint] = extent;
        } else {
            dynamic_bitset<> extent(row+1);
            extent = extent.flip(row);
            index[tmpPoint] = extent;
        }
       row++;
   }
   
   
   distinctPoints = Points();
   for (Index::iterator it = this->index.begin(); it!= this->index.end(); ++it) {
       distinctPoints.push_back(it->first);
   }
}

unsigned int PolytopTopDownMiner::start() {
  if (minSupport>= 0 && nbObject < minSupport) {
    return 0;
  }
  triangulation = new Delaunay();
  triangulation->insert(distinctPoints.begin(), distinctPoints.end());
  dynamic_bitset<>* extent = new dynamic_bitset<>(nbObject);
  extent->flip();
  PointSet* invincibles = new PointSet();
  numberOfVisitedPatterns = 0;
  unsigned int count = enumerate(extent, invincibles);
  delete invincibles;
  delete triangulation;
  delete extent;
  
  // Add empty polygon
  if (minSupport <= 0 && minPerimeter<=0 && minArea <= 0 && minDensity <= 0) {
      count += 1;
      numberOfVisitedPatterns += 1;
      if (printPattern) {
          dynamic_bitset<>* extent = new dynamic_bitset<>(0);
          Polygon* polygon = new Polygon();
          cout << Utils::toStringPattern(extent, polygon, dataset) << endl;
          delete extent;
          delete polygon;
      }
  }
  
  return count;
}

unsigned int PolytopTopDownMiner::enumerate(dynamic_bitset<>* extent, PointSet* invincibles) {
    vector<Vertex_handle>* ch = getCurrentCH();
    Points* chPoints = new Points();
    for (vector<Vertex_handle>::iterator i = ch->begin(); i != ch->end(); ++i) {
        chPoints->push_back((*i)->point());
    }
    numberOfVisitedPatterns += 1;
    unsigned int count = 0;
    Polygon* polygon = new Polygon(chPoints->begin(), chPoints->end());

    double perimeter = Utils::perimeter(polygon);
    double area = abs(CGAL::to_double(polygon->area())) ;
    
    bool minPerimeterConstraint = (minPerimeter<0) || (perimeter >= minPerimeter);
    bool minAreaConstraint = (minArea<0) || (area >= minArea);
    if (!(minAreaConstraint && minPerimeterConstraint)) {
        return 0;
    }
    
    bool maxShapeConstraint = (maxShapeComplexity<0) || (polygon->size() <= maxShapeComplexity); 
    bool minDensityConstraint = (minDensity<0) || (extent->count() >= area * minDensity);
    bool maxPerimeterConstraint = (maxPerimeter<0) || (perimeter <= maxPerimeter);
    bool maxAreaConstraint = (maxArea<0) || (area <= maxArea);
    
    bool valid = maxPerimeterConstraint &&  minDensityConstraint && maxShapeConstraint && maxAreaConstraint;
    
    count = valid ? 1 : 0;
    if (valid && printPattern) {
        cout << Utils::toStringPattern(extent, polygon, dataset) << endl;
    }

    delete polygon;
    delete chPoints;

    PointSet* invincibles_new = new PointSet(*invincibles);
    Vertex_handle vc;
    Point p;
    dynamic_bitset<>* extent_new;
    for (vector<Vertex_handle>::iterator i = ch->begin(); i != ch->end(); ++i) {
        vc = (*i);
        p = vc->point();
        if (invincibles->find(p) != invincibles->end()) 
            continue;
        extent_new = new dynamic_bitset<>(*extent);
        (*extent_new) -= index[p];
        if (extent_new->count() == 0 || (minSupport>=0 && extent_new->count() < minSupport))
            continue;

        triangulation->remove(vc->handle());
        count += enumerate(extent_new,invincibles_new);
        triangulation->insert(p);
        invincibles_new->insert(p);
        delete extent_new;
    }
    delete invincibles_new;
    delete ch;
    return count;
}

vector<Vertex_handle>* PolytopTopDownMiner::getCurrentCH() {
    vector<Vertex_handle>* ch = new std::vector<Vertex_handle>();
    if (triangulation->number_of_vertices() == 0) {
        return ch;
    } else if (triangulation->number_of_vertices() == 1) {
        ch->push_back(triangulation->finite_vertex());
        return ch;
    } 
    Vertex_circulator vc = triangulation->incident_vertices(triangulation->infinite_vertex()),done(vc);
    Point cPoint, pPoint, ppPoint;
    int i = 0;
    if (vc != 0) {
      do {
          i+=1;
          ppPoint = pPoint;
          pPoint = cPoint;
          cPoint = vc->point();
          if (i>2) {
            if (CGAL::collinear(cPoint,pPoint,ppPoint)) {
               ch->pop_back();
            }
          }
          ch->push_back(vc->handle());
      } while(++vc != done);
    }
    
    if (ch->size()>3) {
         ppPoint = pPoint;
         pPoint = cPoint;
         cPoint = ch->at(0)->point();
         if (CGAL::collinear(cPoint,pPoint,ppPoint)) {
             ch->pop_back();
         } 
         ppPoint = pPoint;
         pPoint = cPoint;
         cPoint = ch->at(1)->point();
         if (CGAL::collinear(cPoint,pPoint,ppPoint)) {
             ch->erase(ch->begin());
         }
    }
    return ch;
}

void PolytopTopDownMiner::doTest(const char* csv_file, unsigned int precision, int maxShapeComplexity, int minSupport,  double minDensity, double minPerimeter, double maxPerimeter, double minArea, double maxArea, unsigned int nbTest, bool printPattern) {
  cerr << "Enumeration mode: " << "Top Down" << endl;
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
  
  int start_s,stop_s;
  unsigned int count;
  
  if (!printPattern) {
      cout << "\"" << PolytopTopDownMiner::PROG_NAME << "\",\"" << csv_file << "\"," << precision << "," << dataset.points.size() << "," << maxShapeComplexity << "," << minSupport << "," << minDensity << "," << minPerimeter << "," << maxPerimeter << "," << minArea << "," << maxArea << "," ;
  }
  
  for (int i = 0; i<nbTest; i++) {
      start_s=clock();
      PolytopTopDownMiner polytopMiner(dataset, maxShapeComplexity, minSupport, minDensity, minPerimeter, maxPerimeter, minArea, maxArea, printPattern);
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