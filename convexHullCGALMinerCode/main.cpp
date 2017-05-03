#include <iostream>

#include "PolytopTopDownMiner.h"
#include "PolytopBottomUpMiner.h"
#include "PolytopNaiveBottomUpMiner.h"

using namespace std;

void cerrHelp(void) {
    cerr << "Please Use correctly the command (TODO document ahah!)" << endl;
}


int doTestSeries(const char* prog_name, const char* csv_file) {
    int row = 0; 
    int column;
    
    if (strcmp(prog_name, PolytopTopDownMiner::PROG_NAME) != 0 && strcmp(prog_name, PolytopBottomUpMiner::PROG_NAME) != 0 && strcmp(prog_name, PolytopNaiveBottomUpMiner::PROG_NAME) != 0) {
        cerrHelp();
        return 1;
    }
    
    string filename;
    int minSupport, maxShapeComplexity;
    double minDensity, minPerimeter, maxPerimeter, minArea, maxArea;
    unsigned int precision;
    
    unsigned int nbtest;
    
    std::ifstream ifs;
    ifs.open(csv_file, std::ifstream::in);
    string line;
    std::getline(ifs,line); // read header
    while(std::getline(ifs,line))
    {
        stringstream lineStream(line);
        stringstream stringStream;
        string  cell;
        column = 0;
        while(std::getline(lineStream,cell,','))
        {
            if (column == 0) {
                filename = cell;
            } else {
                stringStream << cell << ' ';
            }
            column++;
        }
        stringStream >> precision >> maxShapeComplexity >> minSupport >> minDensity >> minPerimeter >> maxPerimeter >> minArea >> maxArea >> nbtest;
        if (strcmp(prog_name, PolytopTopDownMiner::PROG_NAME) == 0) {
            PolytopTopDownMiner::doTest(filename.c_str(), precision, maxShapeComplexity, minSupport, minDensity, minPerimeter, maxPerimeter, minArea, maxArea, nbtest, false);
        } else if (strcmp(prog_name, PolytopNaiveBottomUpMiner::PROG_NAME) == 0) {
            PolytopNaiveBottomUpMiner::doTest(filename.c_str(), precision, maxShapeComplexity, minSupport, minDensity, minPerimeter, maxPerimeter, minArea, maxArea,  nbtest, false);
        } else if (strcmp(prog_name, PolytopBottomUpMiner::PROG_NAME) == 0) {
            PolytopBottomUpMiner::doTest(filename.c_str(),precision, maxShapeComplexity, minSupport, minDensity, minPerimeter, maxPerimeter, minArea, maxArea, nbtest, false);
        } else {
            ifs.close();
            return 1;
        }
    }
    ifs.close();
    return 0;
}

int main(int argc, char* argv[])
{
    if (argc < 3) {
        cerrHelp();
        return 1;
    }
    
    const char* prog_name = argv[1];
    const char* file_name = argv[2];
    if (argc == 3) {
        return doTestSeries(prog_name, file_name);
    }
    if (argc < 4) {
        cerrHelp();
        return 1;
    }
    stringstream precisionStr;
    unsigned int precision;
    precisionStr << argv[3];
    precisionStr >> precision;
    if (precision<1) {
        precision = 0;
    }
    if (argc != 13) {
        cerrHelp();
        return 1;
    }
    stringstream stringStream;
    stringStream << argv[4] << ' ' << argv[5] << ' ' << argv[6]  << ' ' << argv[7] << ' ' << argv[8] << ' ' << argv[9] << ' ' << argv[10] << ' ' << argv[11] << ' ' << argv[12];
    unsigned int nbTest;
    int maxShapeComplexity,minSupport,printPatternInt;
    double minDensity,minPerimeter,maxPerimeter, minArea, maxArea;
    stringStream >> maxShapeComplexity >> minSupport >> minDensity >> minPerimeter >> maxPerimeter >> minArea >> maxArea >> nbTest >> printPatternInt; 
    bool printPattern = printPatternInt==1;
    if (strcmp(prog_name,PolytopBottomUpMiner::PROG_NAME)==0) {
        PolytopBottomUpMiner::doTest(file_name, precision , maxShapeComplexity, minSupport, minDensity, minPerimeter, maxPerimeter, minArea, maxArea, nbTest, printPattern);
        return 0;
    } else if (strcmp(prog_name,PolytopNaiveBottomUpMiner::PROG_NAME)==0) {
        PolytopNaiveBottomUpMiner::doTest(file_name, precision , maxShapeComplexity, minSupport, minDensity, minPerimeter, maxPerimeter, minArea, maxArea, nbTest, printPattern);
        return 0;
    } else if (strcmp(prog_name,PolytopTopDownMiner::PROG_NAME)==0) {
        PolytopTopDownMiner::doTest(file_name, precision , maxShapeComplexity, minSupport, minDensity, minPerimeter, maxPerimeter, minArea, maxArea, nbTest, printPattern);
        return 0;
    } else {
        cerrHelp();
        return 1;
    }
}