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

Dataset::Dataset(Points points, vector<string> labels) {
    this->points = points;
    this->labels = labels;
}


Dataset::Dataset(const Dataset& rhs) {
    this->points = rhs.points;
    this->labels = rhs.labels;
}

Dataset::~Dataset() {
    
}