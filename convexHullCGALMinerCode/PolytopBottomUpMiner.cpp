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

#include "Utils.h"
#include "PolytopBottomUpMiner.h"

const char* PolytopBottomUpMiner::PROG_NAME = "cpolybottomup";

PolytopBottomUpMiner::PolytopBottomUpMiner(Dataset& dataset, int maxShapeComplexity, int minSupport, double minDensity, double minPerimeter, double maxPerimeter, double minArea, double maxArea, bool printPattern) {
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
}

PolytopBottomUpMiner::~PolytopBottomUpMiner() {
    delete[] distinctPoints;
    delete[] realExtentPerDistinctPoints;

    for (int i = 0; i < nbDistinctObject - 1; i++) {
        for (map<int, MySegment*>::iterator it = mySegments[i].begin(); it != mySegments[i].end(); ++it) {
            delete it->second;
            mySegments[i].erase(it);
        }
    }
}

void PolytopBottomUpMiner::initIndex(void) {
    map<Point, dynamic_bitset<> > mapping;
    Point tmpPoint;
    int row = 0;
    for (Points::iterator it = this->dataset.points.begin(); it != this->dataset.points.end(); ++it) {
        tmpPoint = (*it);
        if (mapping.find(*it) != mapping.end()) {
            dynamic_bitset<> extent = mapping[tmpPoint];
            extent.flip(row);
            mapping[tmpPoint] = extent;
        } else {
            dynamic_bitset<> extent(nbObject);
            extent = extent.flip(row);
            mapping[tmpPoint] = extent;
        }
        row++;
    }

    nbDistinctObject = mapping.size();
    distinctPoints = new Point[nbDistinctObject];
    realExtentPerDistinctPoints = new dynamic_bitset<>[nbDistinctObject];
    int i = 0;

    for (Index::iterator it = mapping.begin(); it != mapping.end(); ++it) {
        distinctPoints[i] = it->first;
        realExtentPerDistinctPoints[i] = it->second;
        i++;
    }

    mySegments = new map<int, MySegment*>[nbDistinctObject-1];
    double maxPerimeterSquare = maxPerimeter*maxPerimeter;
    for (int i = 0; i < nbDistinctObject - 1; i++) {
        for (int j = i + 1; j < nbDistinctObject; j++) {
            if (maxPerimeter < 0 || CGAL::to_double(CGAL::squared_distance(distinctPoints[i], distinctPoints[j])) <= maxPerimeterSquare) {
                mySegments[i][j] = createSegment(i, j);
            }
        }
    }
}

MySegment* PolytopBottomUpMiner::createSegment(int a, int b) {
    Point p = distinctPoints[a];
    Point q = distinctPoints[b];
    double length = sqrt(CGAL::to_double(CGAL::squared_distance(p, q)));
    dynamic_bitset<> realSupport(nbObject);
    dynamic_bitset<> realStrictPositiveSemiPlan(nbObject);
    dynamic_bitset<> realStrictNegativeSemiPlan(nbObject);
    dynamic_bitset<> distinctStrictPositiveSemiPlan(nbDistinctObject);
    dynamic_bitset<> distinctStrictNegativeSemiPlan(nbDistinctObject);

    realSupport |= realExtentPerDistinctPoints[a];
    realSupport |= realExtentPerDistinctPoints[b];

    for (int i = 0; i < nbDistinctObject; i++) {
        if (i == a || i == b) {
            continue;
        }
        Point r = distinctPoints[i];
        bool notValidInPerimeter =  (maxPerimeter>0 && (sqrt(CGAL::to_double(CGAL::squared_distance(p, r)))+sqrt(CGAL::to_double(CGAL::squared_distance(q, r))))>maxPerimeter-length);
        switch (CGAL::orientation(p, q, r)) {
            case CGAL::LEFT_TURN:
                if (notValidInPerimeter) break;
                distinctStrictPositiveSemiPlan.flip(i);
                realStrictPositiveSemiPlan |= realExtentPerDistinctPoints[i];
                break;
            case CGAL::RIGHT_TURN:
                if (notValidInPerimeter) break;
                distinctStrictNegativeSemiPlan.flip(i);
                realStrictNegativeSemiPlan |= realExtentPerDistinctPoints[i];
                break;
            case CGAL::COLLINEAR:
                if (((p.x() <= r.x() && r.x() <= q.x()) || (q.x() <= r.x() && r.x() <= p.x()))
                        && ((p.y() <= r.y() && r.y() <= q.y()) || (q.y() <= r.y() && r.y() <= p.y()))) {
                    realSupport |= realExtentPerDistinctPoints[i];
                }
                break;
        }
    }

    return new MySegment(a, b, length, realSupport, realStrictPositiveSemiPlan, realStrictNegativeSemiPlan, distinctStrictPositiveSemiPlan, distinctStrictNegativeSemiPlan);
}

unsigned int PolytopBottomUpMiner::start(unsigned int &realMaxShapeComplexity, unsigned int *&patternCountByShape) {
    this->realMaxShapeComplexity = 0;
    unsigned int arraySize = nbDistinctObject;
    if (maxShapeComplexity>=0) {
        arraySize = maxShapeComplexity + 1;
    }
    this->patternCountByShape = new unsigned int[arraySize];
    for (int i = 0; i < arraySize; i++) {
        this->patternCountByShape[i] = 0;
    }
    
    unsigned int count = 0;
    Pattern* p = new Pattern();
    numberOfVisitedPatterns = 0;
    count += enumerate(p, 0);
    delete p;

    realMaxShapeComplexity = this->realMaxShapeComplexity;
    patternCountByShape = new unsigned int[realMaxShapeComplexity + 1];
    for (int i = 0; i <= realMaxShapeComplexity; i++) {
        patternCountByShape[i] = this->patternCountByShape[i];
    }
    

    delete this->patternCountByShape;

    return count;
}

unsigned int PolytopBottomUpMiner::enumerate(Pattern *p, int startingPointIndex) {
    unsigned int currentComplexity = p->complexiy;
    unsigned int count;
    numberOfVisitedPatterns += 1;
    bool minDensityConstraint = (minDensity<0) || (p->extent.count()>0 && p->extent.count() >= p->area * minDensity) || (p->extent.count() == 0 && minDensity == 0);
    bool minSupportConstraint = (minSupport<0) || (p->extent.count() >= minSupport);
    bool minPerimeterConstraint = (minPerimeter<0) || (p->perimeter >= minPerimeter);
    bool minAreaConstraint = (minArea<0) || (p->area >= minArea);
    bool valid = minDensityConstraint && minSupportConstraint && minPerimeterConstraint && minAreaConstraint;
    count = valid ? 1 : 0;
    if (valid && currentComplexity > this->realMaxShapeComplexity) {
        this->realMaxShapeComplexity = currentComplexity;
    }
    this->patternCountByShape[currentComplexity] += count;
    
    if (valid && this->printPattern) {
        cout << toStringPattern(p) << endl;    
    }
    
    if (currentComplexity == maxShapeComplexity) {
        return count;
    }
    if (currentComplexity == 0) {
        for (int k = 0; k < nbDistinctObject; k++) {
            Pattern* newPattern = insertOutsideVertex(p, 0, k);
            count += enumerate(newPattern, k);
            delete newPattern;
        }
    } else {
        // prune with density/support ---------
        int candidatesCount = 0;
        for (int j = 0; j<currentComplexity; j++) {
            candidatesCount += p->realCandidates[j].count();
        }
        int supportUB = p->support + candidatesCount;
        if (minSupport>0 && supportUB < minSupport ||(minDensity>0 && supportUB < minDensity*p->area)) {
            //return count;
        }
        //------------------------------------- 
        for (int j = 0; j < currentComplexity; j++) {
            dynamic_bitset<> candidates = p->distinctCandidates[j];
            for (int k = candidates.find_next(startingPointIndex); k != dynamic_bitset<>::npos; k = candidates.find_next(k)) {
                Pattern* newPattern = insertOutsideVertex(p, j, k);
                if (newPattern != 0) {
                    count += enumerate(newPattern, k);
                    delete newPattern;
                }
            }
        }
    }


    return count;
}

void PolytopBottomUpMiner::doTest(const char* csv_file, unsigned int precision, int maxShapeComplexity, int minSupport,  double minDensity, double minPerimeter, double maxPerimeter, double minArea, double maxArea, unsigned int nbTest, bool printPattern) {
  cerr << "Enumeration mode: " << "Optimized Bottom Up" << endl;
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
      cout << "\"" << PolytopBottomUpMiner::PROG_NAME << "\",\"" << csv_file << "\"," << precision << "," << dataset.points.size() << "," << maxShapeComplexity << "," << minSupport << "," << minDensity << "," << minPerimeter << "," << maxPerimeter << "," << minArea << "," << maxArea << "," ;
  }
  
  for (int i = 0; i<nbTest; i++) {
      start_s=clock();
      PolytopBottomUpMiner polytopMiner(dataset, maxShapeComplexity, minSupport, minDensity, minPerimeter, maxPerimeter, minArea, maxArea, printPattern);
      unsigned int realMaxShape;
      unsigned int* patternCountByShape;
      count = polytopMiner.start(realMaxShape, patternCountByShape);
      stop_s=clock();
      if (i==0) {
          cerr << " > Number of simple closed itemset (convex hulls): " << count << endl;
          cerr << " > Number of visited patterns: " << polytopMiner.getNumberOfVisitedPatterns() << endl;
          cerr << " > Real Maximum shape comlexity: " << realMaxShape << endl;
          cerr << " > Pattern count per shape: ";
          for (int k=0; k<=realMaxShape; k++) {
              cerr << patternCountByShape[k] << ", ";
          }
          if (!printPattern) {
              cout << count << "," <<  polytopMiner.getNumberOfVisitedPatterns() << ",";
          }
          cerr << endl;
      }
      double execTimeMs = (stop_s-start_s)/double(CLOCKS_PER_SEC)*1000;
      cerr << " > Test " << (i+1) << " exec time: " << execTimeMs << " ms" << endl;
      if (!printPattern) {
          cout << execTimeMs << ";";
      }
      delete [] patternCountByShape;
  }
  if (!printPattern) {
      cout << endl;
  }
  cerr << "-------------------------" << endl;
}

Pattern* PolytopBottomUpMiner::createPattern(unsigned int complexity, unsigned int* ccwVertices, dynamic_bitset<>& extent, dynamic_bitset<>*& distinctCandidates, dynamic_bitset<>*& realCandidates, double perimeter, double area) {
    map<string, int> countPerClass;
    for (int j = extent.find_first(); j != dynamic_bitset<>::npos; j = extent.find_next(j)) {
        string label = dataset.labels.at(j);
        if (countPerClass.find(label) == countPerClass.end()) {
            countPerClass[label] = 1;
        } else {
            countPerClass[label] += 1;
        }
    }
    
    double gini = 1;
    double entropy = 0;
    for (map<string, int>::iterator it = countPerClass.begin(); it != countPerClass.end(); ++it) {
        double proportion = (it->second) / ((double) extent.count());
        gini -= proportion * proportion;
        entropy -= proportion * log(proportion);
    }

    return new Pattern(complexity, ccwVertices, extent, distinctCandidates, realCandidates, perimeter, area, gini, entropy);
}

Pattern* PolytopBottomUpMiner::createPatternFromObject(unsigned int a) {
    unsigned int complexity = 1;
    unsigned int* ccwVertices = new unsigned int[1];
    ccwVertices[0] = a;
    dynamic_bitset<>* distinctCandidates = new dynamic_bitset<>[1];
    dynamic_bitset<>* realCandidates = new dynamic_bitset<>[1];
    distinctCandidates[0] = dynamic_bitset<>(nbDistinctObject);
    distinctCandidates[0].flip();
    distinctCandidates[0].flip(a);
    realCandidates[0] = dynamic_bitset<>(nbObject);
    realCandidates[0].flip();
    realCandidates[0] -= realExtentPerDistinctPoints[a];
    
    Pattern* p = createPattern(complexity, ccwVertices, realExtentPerDistinctPoints[a], distinctCandidates, realCandidates, 0, 0);
    delete ccwVertices;
    return p;
}

Pattern* PolytopBottomUpMiner::createPatternFromSegment(unsigned int a, unsigned int b) {
    if (mySegments[a].find(b) == mySegments[a].end()) {
        return 0;
    }
    unsigned int complexity = 2;
    unsigned int* ccwVertices = new unsigned int[2];
    ccwVertices[0] = a;
    ccwVertices[1] = b;
    MySegment* s = mySegments[a][b];
    dynamic_bitset<>* distinctCandidates = new dynamic_bitset<>[2];
    dynamic_bitset<>* realCandidates = new dynamic_bitset<>[2];
    distinctCandidates[0] = s->distinctStrictPositiveSemiPlan;
    distinctCandidates[1] = s->distinctStrictNegativeSemiPlan;
    realCandidates[0] = s->realStrictPositiveSemiPlan;
    realCandidates[1] = s->realStrictNegativeSemiPlan;
    Pattern* p = createPattern(complexity, ccwVertices, s->realSupport, distinctCandidates, realCandidates, s->length, 0);
    delete ccwVertices;
    return p;
}

// Precondition : 
//   0 <= index < p->complexity (new points inserted at (index+1)%p->complexity
//   vertice is in positive side between p->ccwVertices[index] and p->ccwVertices[(index+1)%p->complexity]
//   vertice > max(p->ccwVertices)
// Result :
//   return the new pattern if its perimeter is lower than maxPerimter else 0;

Pattern* PolytopBottomUpMiner::insertOutsideVertex(Pattern *p, unsigned int index, unsigned int vertice) {
    if (p->complexiy == 0) {
        return createPatternFromObject(vertice);
    } else if (p->complexiy == 1) {
        return createPatternFromSegment(p->ccwVertices[0], vertice);
    } else if (p->complexiy == 2) {
        unsigned int ccwVertice0 = p->ccwVertices[0], ccwVertice1 = p->ccwVertices[1]; 
        MySegment* s01 = mySegments[ccwVertice0][ccwVertice1];
        MySegment* s0New = mySegments[ccwVertice0][vertice];
        MySegment* s1New = mySegments[ccwVertice1][vertice];
        double perimeter = p->perimeter + s0New->length + s1New->length;
        
        if (maxPerimeter>=0 && perimeter > maxPerimeter) {
            return 0;
        }
        
        double area = abs(CGAL::to_double(CGAL::area(distinctPoints[p->ccwVertices[0]], distinctPoints[p->ccwVertices[1]], distinctPoints[vertice])));
        if (maxArea>=0 && area > maxArea) {
            return 0;
        }
        
        unsigned int* ccwVertices = new unsigned int[3];
        dynamic_bitset<>* candidates = new dynamic_bitset<>[3];
        dynamic_bitset<>* realCandidates = new dynamic_bitset<>[3];
        if (index == 0) {
            ccwVertices[0] = ccwVertice0;
            ccwVertices[1] = ccwVertice1;
            ccwVertices[2] = vertice;
            
            dynamic_bitset<> extent = dynamic_bitset<>(p->extent);
            extent |= s0New->realSupport;
            extent |= s1New->realSupport;
            dynamic_bitset<> strictIn = dynamic_bitset<>(s01->realStrictPositiveSemiPlan);
            strictIn &= s0New->realStrictNegativeSemiPlan;
            strictIn &= s1New->realStrictPositiveSemiPlan;
            extent |= strictIn;
            
            candidates[0] = dynamic_bitset<>(s01->distinctStrictNegativeSemiPlan);
            realCandidates[0] = dynamic_bitset<>(s01->realStrictNegativeSemiPlan);
            candidates[0] &= s1New->distinctStrictPositiveSemiPlan;
            realCandidates[0] &= s1New->realStrictPositiveSemiPlan;
            candidates[0] &= s0New->distinctStrictNegativeSemiPlan;
            realCandidates[0] &= s0New->realStrictNegativeSemiPlan;
            
            candidates[1] = dynamic_bitset<>(s1New->distinctStrictNegativeSemiPlan);
            realCandidates[1] = dynamic_bitset<>(s1New->realStrictNegativeSemiPlan);
            candidates[1] &= s01->distinctStrictPositiveSemiPlan;
            realCandidates[1] &= s01->realStrictPositiveSemiPlan;
            candidates[1] &= s0New->distinctStrictNegativeSemiPlan;
            realCandidates[1] &= s0New->realStrictNegativeSemiPlan;
            
            candidates[2] = dynamic_bitset<>(s0New->distinctStrictPositiveSemiPlan);
            realCandidates[2] = dynamic_bitset<>(s0New->realStrictPositiveSemiPlan);
            candidates[2] &= s1New->distinctStrictPositiveSemiPlan;
            realCandidates[2] &= s1New->realStrictPositiveSemiPlan;
            candidates[2] &= s01->distinctStrictPositiveSemiPlan;
            realCandidates[2] &= s01->realStrictPositiveSemiPlan;
            
            return createPattern(3, ccwVertices, extent, candidates, realCandidates, perimeter, area);
        } else {
            ccwVertices[0] = ccwVertice0;
            ccwVertices[1] = vertice;
            ccwVertices[2] = ccwVertice1;
            dynamic_bitset<> extent = dynamic_bitset<>(p->extent);
            extent |= s0New->realSupport;
            extent |= s1New->realSupport;
            dynamic_bitset<> strictIn = dynamic_bitset<>(s01->realStrictNegativeSemiPlan);
            strictIn &= s0New->realStrictPositiveSemiPlan;
            strictIn &= s1New->realStrictNegativeSemiPlan;
            extent |= strictIn;
            candidates[0] = dynamic_bitset<>(s0New->distinctStrictNegativeSemiPlan);
            realCandidates[0] = dynamic_bitset<>(s0New->realStrictNegativeSemiPlan);
            candidates[0] &= s1New->distinctStrictNegativeSemiPlan;
            realCandidates[0] &= s1New->realStrictNegativeSemiPlan;
            candidates[0] &= s01->distinctStrictNegativeSemiPlan;
            realCandidates[0] &= s01->realStrictNegativeSemiPlan;
            
            candidates[1] = dynamic_bitset<>(s1New->distinctStrictPositiveSemiPlan);
            realCandidates[1] = dynamic_bitset<>(s1New->realStrictPositiveSemiPlan);
            candidates[1] &= s01->distinctStrictNegativeSemiPlan;
            realCandidates[1] &= s01->realStrictNegativeSemiPlan;
            candidates[1] &= s0New->distinctStrictPositiveSemiPlan;
            realCandidates[1] &= s0New->realStrictPositiveSemiPlan;
            
            candidates[2] = dynamic_bitset<>(s01->distinctStrictPositiveSemiPlan);
            realCandidates[2] = dynamic_bitset<>(s01->realStrictPositiveSemiPlan);
            candidates[2] &= s0New->distinctStrictPositiveSemiPlan;
            realCandidates[2] &= s0New->realStrictPositiveSemiPlan;
            candidates[2] &= s1New->distinctStrictNegativeSemiPlan;
            realCandidates[2] &= s1New->realStrictNegativeSemiPlan;
            
            return createPattern(3, ccwVertices, extent, candidates, realCandidates, perimeter, area);
        }
    } else {
        unsigned int pVertice = p->ccwVertices[index], nVertice = p->ccwVertices[(index+1)%p->complexiy];
        MySegment* previousToNewSegment = mySegments[pVertice][vertice];
        MySegment* newToNextSegment = mySegments[nVertice][vertice];
        bool correctOldSegment = p->ccwVertices[index] < p->ccwVertices[(index+1)%p->complexiy];
        MySegment* oldSegment = correctOldSegment ? mySegments[p->ccwVertices[index]][p->ccwVertices[(index+1)%p->complexiy]] : mySegments[p->ccwVertices[(index+1)%p->complexiy]][p->ccwVertices[index]];
        double perimeter = p->perimeter - oldSegment->length + previousToNewSegment->length + newToNextSegment->length;
        if (maxPerimeter>=0 && perimeter > maxPerimeter) {
            return 0;
        }
        
        double area = p->area + abs(CGAL::to_double(CGAL::area(distinctPoints[p->ccwVertices[index]], distinctPoints[vertice], distinctPoints[p->ccwVertices[(index+1)%p->complexiy]])));
        if (maxArea>0 && area > maxArea) {
            return 0;
        }
        
        unsigned int newComplexity = p->complexiy + 1;
        unsigned int ppIndex = (index > 0) ? index - 1 : p->complexiy;
        unsigned int pIndex = index;
        unsigned int nIndex = (index + 2) % newComplexity;
        
        unsigned int* ccwVertices = new unsigned int[newComplexity];
        dynamic_bitset<>* candidates = new dynamic_bitset<>[newComplexity];
        dynamic_bitset<>* realCandidates = new dynamic_bitset<>[newComplexity];
        for (int i = 0; i <= index; i++) {
            ccwVertices[i] = p->ccwVertices[i];
            candidates[i] = dynamic_bitset<>(p->distinctCandidates[i]);
            realCandidates[i] = dynamic_bitset<>(p->realCandidates[i]);
        }
        ccwVertices[index + 1] = vertice;
        for (int i = index + 2; i < newComplexity; i++) {
            ccwVertices[i] = p->ccwVertices[i - 1];
            candidates[i] = dynamic_bitset<>(p->distinctCandidates[i-1]);
            realCandidates[i] = dynamic_bitset<>(p->realCandidates[i-1]);
        }
        
        dynamic_bitset<> extent = dynamic_bitset<>(p->extent);
        extent |= previousToNewSegment->realSupport;
        extent |= newToNextSegment->realSupport;
        
        dynamic_bitset<> strictIn = dynamic_bitset<>(previousToNewSegment->realStrictPositiveSemiPlan);
        strictIn &= newToNextSegment->realStrictNegativeSemiPlan;
        strictIn &= correctOldSegment ? oldSegment->realStrictNegativeSemiPlan : oldSegment->realStrictPositiveSemiPlan;
        extent |= strictIn;
        candidates[ppIndex] &= previousToNewSegment->distinctStrictPositiveSemiPlan;
        realCandidates[ppIndex] &= previousToNewSegment->realStrictPositiveSemiPlan;
        
        candidates[nIndex] &= newToNextSegment->distinctStrictNegativeSemiPlan;
        realCandidates[nIndex] &= newToNextSegment->realStrictNegativeSemiPlan;
        
        candidates[index] = dynamic_bitset<>(p->distinctCandidates[index]);
        realCandidates[index] = dynamic_bitset<>(p->realCandidates[index]);
        candidates[index] &= previousToNewSegment->distinctStrictNegativeSemiPlan;
        realCandidates[index] &= previousToNewSegment->realStrictNegativeSemiPlan;
        candidates[index] &= newToNextSegment->distinctStrictNegativeSemiPlan;
        realCandidates[index] &= newToNextSegment->realStrictNegativeSemiPlan;
        
        candidates[index+1] = dynamic_bitset<>(p->distinctCandidates[index]);
        realCandidates[index+1] = dynamic_bitset<>(p->realCandidates[index]); 
        candidates[index+1] &= previousToNewSegment->distinctStrictPositiveSemiPlan;
        realCandidates[index+1] &= previousToNewSegment->realStrictPositiveSemiPlan;
        candidates[index+1] &= newToNextSegment->distinctStrictPositiveSemiPlan;
        realCandidates[index+1] &= newToNextSegment->realStrictPositiveSemiPlan;
        
        return createPattern(newComplexity, ccwVertices, extent, candidates, realCandidates, perimeter, area);
    }
}

string PolytopBottomUpMiner::toStringPattern(Pattern *p) {
    stringstream sstream;
    for (int i = 0; i < p->complexiy; i++) {
        sstream << distinctPoints[p->ccwVertices[i]] << ";";
    }
    sstream << "," << p->complexiy << "," << p->support << "," << p->area << "," << p->perimeter << "," << p->gini << "," << p->entropy;
    return sstream.str();
}

MySegment::MySegment(unsigned int a, unsigned int b, double length, dynamic_bitset<>& realSupport, dynamic_bitset<>& realStrictPositiveSemiPlan, dynamic_bitset<>& realStrictNegativeSemiPlan, dynamic_bitset<>& distinctStrictPositiveSemiPlan, dynamic_bitset<>& distinctStrictNegativeSemiPlan) {
    this->a = a;
    this->b = b;
    this->length = length;
    this->realSupport = realSupport;
    this->realStrictPositiveSemiPlan = realStrictPositiveSemiPlan;
    this->realStrictNegativeSemiPlan = realStrictNegativeSemiPlan;
    this->distinctStrictPositiveSemiPlan = distinctStrictPositiveSemiPlan;
    this->distinctStrictNegativeSemiPlan = distinctStrictNegativeSemiPlan;
}

MySegment::MySegment(const MySegment& rhs) {
    this->a = rhs.a;
    this->b = rhs.b;
    this->length = rhs.length;
    this->realSupport = rhs.realSupport;
    this->realStrictPositiveSemiPlan = rhs.realStrictPositiveSemiPlan;
    this->realStrictNegativeSemiPlan = rhs.realStrictNegativeSemiPlan;
    this->distinctStrictPositiveSemiPlan = rhs.distinctStrictPositiveSemiPlan;
    this->distinctStrictNegativeSemiPlan = rhs.distinctStrictNegativeSemiPlan;
}

MySegment::~MySegment() {

}

Pattern::Pattern(unsigned int complexity, unsigned int* ccwVertices, dynamic_bitset<>& extent, dynamic_bitset<>*& distinctCandidates, dynamic_bitset<>*& realCandidates , double perimeter, double area, double gini, double entropy) {
    this->complexiy = complexity;
    this->ccwVertices = new unsigned int[complexity];
    this->extent = extent;
    for (int i = 0; i < complexity; i++) {
        this->ccwVertices[i] = ccwVertices[i];
    }
    this->distinctCandidates = distinctCandidates;
    this->realCandidates = realCandidates;
    this->support = extent.count();
    this->perimeter = perimeter;
    this->area = area;
    this->extent = extent;
    this->gini = gini;
    this->entropy = entropy;
}

Pattern::Pattern(const Pattern& rhs) {
    this->complexiy = rhs.complexiy;
    this->ccwVertices = new unsigned int[rhs.complexiy];
    this->extent = rhs.extent;
    this->distinctCandidates = new dynamic_bitset<>[rhs.complexiy];
    this->realCandidates = new dynamic_bitset<>[rhs.complexiy];
    for (int i = 0; i < rhs.complexiy; i++) {
        this->ccwVertices[i] = rhs.ccwVertices[i];
        this->distinctCandidates[i] = rhs.distinctCandidates[i];
        this->realCandidates[i] = rhs.realCandidates[i];
    }
    this->support = rhs.extent.count();
    this->perimeter = rhs.perimeter;
    this->area = rhs.area;
    this->extent = rhs.extent;
    this->entropy = rhs.entropy;
}

Pattern::~Pattern() {

    if (ccwVertices != 0)
        delete ccwVertices;
    
    if (distinctCandidates != 0)
        delete[] distinctCandidates;
        
    if (realCandidates != 0) {
        delete[] realCandidates;
    }

}