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

#ifndef UTILS_H
#define UTILS_H

#include <vector>       // std::vector
#include <set>          // std::set
#include <map>          // std::map
#include <iomanip>      // std::setprecision
#include <string.h>     // std::string
#include <iostream>
#include <fstream>      // std::ifstream
#include <sstream>      // std::stringstream
#include <CGAL/Exact_predicates_exact_constructions_kernel.h>
#include <boost/dynamic_bitset.hpp>
#include <CGAL/enum.h>
#include <CGAL/Polygon_2.h>
#include <CGAL/Polygon_2_algorithms.h>
#include <CGAL/convex_hull_2.h>

using namespace std;
using boost::dynamic_bitset;

typedef CGAL::Exact_predicates_exact_constructions_kernel K;
typedef K::Point_2 Point;
typedef K::Line_2 Line;
typedef vector<Point> Points;
typedef set<Point> PointSet;
typedef map<Point,  dynamic_bitset<> > Index;
typedef CGAL::Polygon_2<K> Polygon;

class Dataset {
public:
  Points points;
  vector<string> labels;
  
  Dataset() {};
  Dataset(Points points, vector<string> labels);
  Dataset(const Dataset& rhs);
  ~Dataset();
};

class Utils {
public:
    static Dataset readPoints(const char* csv_file, unsigned int precision);
    static double perimeter(Polygon* polygon);
    static string toStringPattern(dynamic_bitset<>* extent, Polygon* intent, Dataset& dataset);
};

#endif /* POLYTOPTOPDOWNMINER_H */

