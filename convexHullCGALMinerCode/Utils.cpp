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

Dataset Utils::readPoints(const char* csv_file, unsigned int precision) {
    int row = 0; 
    int column;
    std::ifstream ifs;
    ifs.open(csv_file, std::ifstream::in);
    string line;
    std::getline(ifs,line); // read header
    int point_dimension = 0;
    Points result;
    vector<string> labels;
    Point tmp_point;
    while(std::getline(ifs,line))
    {
        column = 0;
        stringstream  lineStream(line);
        std::stringstream exact_values_stream;
        exact_values_stream << std::fixed;
        string  cell;
        while(std::getline(lineStream,cell,','))
        {
            if (row == 0 && column>0) {
                point_dimension+=1;
            }
            if (column != 0) {
                double d = std::atof(cell.c_str());
                exact_values_stream << std::setprecision(precision) << d << " ";
            }
            column++;
        }
        if (column>2) {
            labels.push_back(cell);
        } 
        exact_values_stream >> tmp_point;
        result.push_back(tmp_point);
        row++;
    }
    ifs.close();
    return Dataset(result, labels);
}

string Utils::toStringPattern(dynamic_bitset<>* extent, Polygon* intent, Dataset& dataset) {
    stringstream sstream;
    for (Polygon::Vertex_iterator it = intent->vertices_begin(); it != intent->vertices_end(); ++it) {
        sstream << (*it) << ";";
    }
    
    map<string, int> countPerClass;
    for (int j = extent->find_first(); j != dynamic_bitset<>::npos; j = extent->find_next(j)) {
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
        double proportion = (it->second)/((double) extent->count());
        gini -= proportion * proportion;
        entropy -= proportion * log(proportion);
    }
    
    sstream << "," << intent->size() << "," << extent->count() << "," << abs(CGAL::to_double(intent->area())) << "," << perimeter(intent) << "," << gini << "," << entropy;
    return sstream.str();
}

double Utils::perimeter(Polygon* polygon) {
  if (polygon->size() < 2) {
      return 0;
  } else if (polygon->size() == 2) {
      K::Segment_2 edge = *(polygon->edges_begin());
      return sqrt(CGAL::to_double(edge.squared_length()));
  }
  double perimeter = 0;
  for (Polygon::Edge_const_iterator vi = polygon->edges_begin(); vi != polygon->edges_end(); ++vi) {
      K::Segment_2 edge = *vi;
      perimeter+=sqrt(CGAL::to_double(edge.squared_length()));
  }
  return perimeter;
}
