#!/bin/sh

#Hyperrectangle algorithms
java -jar numericalPatternMiner/numericalPatternMiner.jar hyperrectangle MinIntChange data/irisSLSW/irisSLSW-10.csv >> /dev/null
java -jar numericalPatternMiner/numericalPatternMiner.jar hyperrectangle MinIntChangeIndex data/irisSLSW/irisSLSW-10.csv
/dev/null

#Hyperrectangle algorithm output frequent patterns
java -jar numericalPatternMiner/numericalPatternMiner.jar hyperrectangle -o --minsup 5 MinIntChangeIndex data/irisSLSW/irisSLSW-10.csv > hyperrectangles-patterns.csv

#Polygon algorithms
java -jar numericalPatternMiner/numericalPatternMiner.jar polygon ExtCbo data/irisSLSW/irisSLSW-10.csv
java -jar numericalPatternMiner/numericalPatternMiner.jar polygon DelaunayEnum data/irisSLSW/irisSLSW-10.csv
java -jar numericalPatternMiner/numericalPatternMiner.jar polygon ExtremePointsEnum data/irisSLSW/irisSLSW-10.csv

#Polygon algorithm output frequent patterns with maxshape = 4
java -jar numericalPatternMiner/numericalPatternMiner.jar polygon --maxshape 4 --minsup 5 -o ExtremePointsEnum data/irisSLSW/irisSLSW-10.csv > polygons-patterns.csv

#Polygon mcts algortihm test
java -jar numericalPatternMiner/numericalPatternMiner.jar polygonMCTS --maxiteration -1 -o data/irisSLSW/irisSLSW-10.csv > polygonsMCTS-patterns.csv

#Polygon CGAL test (C++)
./convexHullCGALMinerCode/convexHullCGALMiner.py ExtCbo data/irisSLSW/irisSLSW-10.csv >> /dev/null
./convexHullCGALMinerCode/convexHullCGALMiner.py DelaunayEnum data/irisSLSW/irisSLSW-10.csv >> /dev/null
./convexHullCGALMinerCode/convexHullCGALMiner.py ExtremePointsEnum data/irisSLSW/irisSLSW-10.csv >> /dev/null

