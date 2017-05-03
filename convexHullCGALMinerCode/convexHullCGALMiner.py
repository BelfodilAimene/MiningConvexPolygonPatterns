#!/usr/bin/env python

import argparse
import subprocess


FILEPATH_DESCRIPTION = "The file is comma-separated file with a header. The first column refers to object identifiers, columns 2 to n-1 refers to numerical attributes and the  last  colum  refer to the class of object. Note that for polygon enumeratoon the file must contains 2 and only 2 numerical attributes"

PRECISION_DEFAULT = 4
PRECISION_DESCRIPTION = "precision. Number of values after the floating point to take into account when reading the file (defualt: "+str(PRECISION_DEFAULT)+")"
OUTPUT_DESCRIPTION = "output patterns in standard output when activated"

MAXSHAPE_DESCRIPTION = "maximum number of extreme points (defualt: -1)"
MINSUPPORT_DESCRIPTION = "minimum support (defualt: -1)"
MINDENSITY_DESCRIPTION = "minimum density (defualt: -1)"
MINPERIMETER_DESCRIPTION = "minimum perimeter (defualt: -1)"
MAXPERIMETER_DESCRIPTION = "maximum perimeter (defualt: -1)"
MINAREA_DESCRIPTION = "minimum area (defualt: -1)"
MAXAREA_DESCRIPTION = "maximum area (defualt: -1)"

MCTSSAMECLASS_DESCRIPTION = "Take extreme point of same class when possible (defualt: -1)"
MAXSEGMENTDISTANCE_DESCRIPTION = "Take segment which distance < maxdistance when possible (defualt: -1)"
MAXSEGMENTCOEFF_DESCRIPTION = "Take point which distance with the centroid of his visible face don't exceed COEFF * length(visibleFace) when possible"

MAXITERATION_DEFAULT = 100
MAXITERATION_DESCRIPTION = "maximum number of iteration (budget) for MCTS algorithm (default = "+str(MAXITERATION_DEFAULT)+")"



def ExtCbo(args):
    printPattern = "0"
    if args.o : printPattern = "1"
    
    command = " ".join(("./convexHullCGALMinerCode/convexHullCGALMiner cpolynaivebottomup",str(args.filepath),str(args.p),str(args.maxshape),str(args.minsupport),str(args.mindensity),str(args.minperimeter),str(args.maxperimeter),str(args.minarea),str(args.maxarea),"1",printPattern))
    process = subprocess.Popen(command,shell=True)
    process.wait()

def DelaunayEnum(args):
    printPattern = "0"
    if args.o : printPattern = "1"
    
    command = " ".join(("./convexHullCGALMinerCode/convexHullCGALMiner cpolytopdown",str(args.filepath),str(args.p),str(args.maxshape),str(args.minsupport),str(args.mindensity),str(args.minperimeter),str(args.maxperimeter),str(args.minarea),str(args.maxarea),"1",printPattern))
    process = subprocess.Popen(command,shell=True)
    process.wait()

def ExtremePointsEnum(args):
    printPattern = "0"
    if args.o : printPattern = "1"
    
    command = " ".join(("./convexHullCGALMinerCode/convexHullCGALMiner cpolybottomup",str(args.filepath),str(args.p),str(args.maxshape),str(args.minsupport),str(args.mindensity),str(args.minperimeter),str(args.maxperimeter),str(args.minarea),str(args.maxarea),"1",printPattern))
    process = subprocess.Popen(command,shell=True)
    process.wait()

if __name__ == "__main__" :
    parser = argparse.ArgumentParser(description='Ijcai algorithms.')
    subparsers = parser.add_subparsers(title='subcommands',
                                       description='valid subcommands',
                                       help='additional help')

    parser_ExtCbo = subparsers.add_parser('ExtCbo')
    parser_ExtCbo.add_argument('filepath', type=str,help=FILEPATH_DESCRIPTION)
    parser_ExtCbo.add_argument('-p', type=int,help=PRECISION_DESCRIPTION, default=PRECISION_DEFAULT)
    parser_ExtCbo.add_argument('--maxshape', type=int,help=MAXSHAPE_DESCRIPTION, default=-1)
    parser_ExtCbo.add_argument('--minsupport', type=int,help=MINSUPPORT_DESCRIPTION, default=-1)
    parser_ExtCbo.add_argument('--mindensity', type=float,help=MINDENSITY_DESCRIPTION, default=-1)
    parser_ExtCbo.add_argument('--minperimeter', type=float,help=MINPERIMETER_DESCRIPTION, default=-1)
    parser_ExtCbo.add_argument('--maxperimeter', type=float,help=MAXPERIMETER_DESCRIPTION, default=-1)
    parser_ExtCbo.add_argument('--minarea', type=float,help=MINAREA_DESCRIPTION, default=-1)
    parser_ExtCbo.add_argument('--maxarea', type=float,help=MAXAREA_DESCRIPTION, default=-1)
    parser_ExtCbo.add_argument('-o',action='store_true',help=OUTPUT_DESCRIPTION)
    parser_ExtCbo.set_defaults(func=ExtCbo)

    parser_DelaunayEnum = subparsers.add_parser('DelaunayEnum')
    parser_DelaunayEnum.add_argument('filepath', type=str,help=FILEPATH_DESCRIPTION)
    parser_DelaunayEnum.add_argument('-p', type=int,help=PRECISION_DESCRIPTION, default=PRECISION_DEFAULT)
    parser_DelaunayEnum.add_argument('--maxshape', type=int,help=MAXSHAPE_DESCRIPTION, default=-1)
    parser_DelaunayEnum.add_argument('--minsupport', type=int,help=MINSUPPORT_DESCRIPTION, default=-1)
    parser_DelaunayEnum.add_argument('--mindensity', type=float,help=MINDENSITY_DESCRIPTION, default=-1)
    parser_DelaunayEnum.add_argument('--minperimeter', type=float,help=MINPERIMETER_DESCRIPTION, default=-1)
    parser_DelaunayEnum.add_argument('--maxperimeter', type=float,help=MAXPERIMETER_DESCRIPTION, default=-1)
    parser_DelaunayEnum.add_argument('--minarea', type=float,help=MINAREA_DESCRIPTION, default=-1)
    parser_DelaunayEnum.add_argument('--maxarea', type=float,help=MAXAREA_DESCRIPTION, default=-1)
    parser_DelaunayEnum.add_argument('-o', action='store_true',help=OUTPUT_DESCRIPTION)
    parser_DelaunayEnum.set_defaults(func=DelaunayEnum)

    parser_ExtremePointsEnum = subparsers.add_parser('ExtremePointsEnum')
    parser_ExtremePointsEnum.add_argument('filepath', type=str,help=FILEPATH_DESCRIPTION)
    parser_ExtremePointsEnum.add_argument('-p', type=int, help=PRECISION_DESCRIPTION, default=PRECISION_DEFAULT)
    parser_ExtremePointsEnum.add_argument('--maxshape', type=int,help=MAXSHAPE_DESCRIPTION, default=-1)
    parser_ExtremePointsEnum.add_argument('--minsupport', type=int,help=MINSUPPORT_DESCRIPTION, default=-1)
    parser_ExtremePointsEnum.add_argument('--mindensity', type=float,help=MINDENSITY_DESCRIPTION, default=-1)
    parser_ExtremePointsEnum.add_argument('--minperimeter', type=float,help=MINPERIMETER_DESCRIPTION, default=-1)
    parser_ExtremePointsEnum.add_argument('--maxperimeter', type=float,help=MAXPERIMETER_DESCRIPTION, default=-1)
    parser_ExtremePointsEnum.add_argument('--minarea', type=float,help=MINAREA_DESCRIPTION, default=-1)
    parser_ExtremePointsEnum.add_argument('--maxarea', type=float,help=MAXAREA_DESCRIPTION, default=-1)
    parser_ExtremePointsEnum.add_argument('-o',action='store_true',help=OUTPUT_DESCRIPTION)
    parser_ExtremePointsEnum.set_defaults(func=ExtremePointsEnum)

    args = parser.parse_args()
    args.func(args)
