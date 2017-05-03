package com.mdi.numericalPattern;

import java.math.BigDecimal;

import com.mdi.numericalPattern.hyperrectangles.HyperrectanglesAlgorithm;
import com.mdi.numericalPattern.hyperrectangles.MinIntChange;
import com.mdi.numericalPattern.hyperrectangles.MinIntChangeIndex;
import com.mdi.numericalPattern.polygons.constrained.ConstrainedPolygonsAlgorithm;
import com.mdi.numericalPattern.polygons.constrained.DelaunayEnum;
import com.mdi.numericalPattern.polygons.constrained.ExtCbo;
import com.mdi.numericalPattern.polygons.constrained.ExtremePointsEnum;
import com.mdi.numericalPattern.polygons.sampling.MCTSExtremePointsEnum;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class Main {
	private static final String SUBPARSER_DEST = "subparser";
	private static final String HYPERRECTANGLE_SUBPARSER = "hyperrectangle";
	private static final String POLYGON_SUBPARSER = "polygon";
	private static final String POLYGON_MCTS_SUBPARSER = "polygonMCTS";

	private static final String HYPERRECTANGLE_SUBPARSER_DESCRIPTION = "mine closed hyperrectangles";
	private static final String POLYGON_SUBPARSER_DESCRIPTION = "mine convex hulls under some constraints exhaustively";
	private static final String POLYGON_MCTS_SUBPARSER_DESCRIPTION = "mine convex hulls using Monte-Carlo Tree Search sampling technique";

	private static final int PRECISION_DEFAULT = 4;
	private static final String PRECISION_SMALL = "p";
	private static final String PRECISION_DESCRIPTION = "precision. Number of values after the floating point to take into account when reading the file (Default: "+PRECISION_DEFAULT+")";

	private static final int MINSUP_DEFAULT = -1;
	private static final String MINSUP_DETAILED = "minsup";
	private static final String MINSUP_DESCRIPTION = "minimum support (Default: "+MINSUP_DEFAULT+")";

	private static final int MAXSHAPE_DEFAULT = -1;
	private static final String MAXSHAPE_DETAILED = "maxshape";
	private static final String MAXSHAPE_DESCRIPTION = "maximum number of extreme points (Default: "+MAXSHAPE_DEFAULT+")";

	private static final String MINAREA_DEFAULT = "-1";
	private static final String MINAREA_DETAILED = "minarea";
	private static final String MINAREA_DESCRIPTION = "minimum area (Default: "+MINAREA_DEFAULT+")";

	private static final String MAXAREA_DEFAULT = "-1";
	private static final String MAXAREA_DETAILED = "maxarea";
	private static final String MAXAREA_DESCRIPTION = "maximum area (Default: "+MAXAREA_DEFAULT+")";

	private static final double MINPERIMETER_DEFAULT = -1;
	private static final String MINPERIMETER_DETAILED = "minperimeter";
	private static final String MINPERIMETER_DESCRIPTION = "minimum perimeter (Default: "+MINPERIMETER_DEFAULT+")";

	private static final double MAXPERIMETER_DEFAULT = -1;
	private static final String MAXPERIMETER_DETAILED = "maxperimeter";
	private static final String MAXPERIMETER_DESCRIPTION = "maximum perimeter (Default: "+MAXPERIMETER_DEFAULT+")";

	private static final String OUTPUT_SMALL = "o";
	private static final String OUTPUT_DESCRIPTION = "output patterns in standard output when activated (Default: false)";

	private static final String ALGO_POS = "algo";
	private static final String ALGO_DESCRIPTION = "algorithm to use to mine patterns";

	private static final String FILE_POS = "filename";
	private static final String FILE_DESCRIPTION = ".csv filepath. The file is comma-separated file with a header. The first column refers to object identifiers, columns 2 to n-1 refers to numerical attributes and the last colum refer to the class of object. Note that for polygon enumeratoon the file must contains 2 and only 2 numerical attributes";

	private static final int MAXITERATION_DEFAULT = 100;
	private static final String MAXITERATION_DETAILED = "maxiteration";
	private static final String MAXITERATION_DESCRIPTION = "maximum number of iteration (budget) for MCTS algorithm (Default: "+MAXITERATION_DEFAULT+")";

	private static final String MCTSSAMECLASS_DETAILED = "sameclass";
	private static final String MCTSSAMECLASS_DESCRIPTION = "Take extreme point of same class when possible (Default: false)";

	private static final double MAXSEGMENTDISTANCE_DEFAULT = -1;
	private static final String MAXSEGMENTDISTANCE_DETAILED = "maxsegmentdistance";
	private static final String MAXSEGMENTDISTANCE_DESCRIPTION = "Take segment which distance < maxdistance when possible (Default: "+MAXSEGMENTDISTANCE_DEFAULT+")";

	private static final double MAXSEGMENTCOEFF_DEFAULT = -1;
	private static final String MAXSEGMENTCOEFF_DETAILED = "maxsegmentcoeff";
	private static final String MAXSEGMENTCOEFF_DESCRIPTION = "Take point which distance with the centroid of his visible face don't exceed COEFF * length(visibleFace) when possible (Default: "+MAXSEGMENTCOEFF_DEFAULT+")";

	public static void main(String[] args) throws Exception {

		ArgumentParser parser = ArgumentParsers.newArgumentParser("prog");
		Subparsers subparsers = parser.addSubparsers().title("search space").dest(SUBPARSER_DEST);

		Subparser hyperrectangle = subparsers.addParser(HYPERRECTANGLE_SUBPARSER)
				.help(HYPERRECTANGLE_SUBPARSER_DESCRIPTION);
		hyperrectangle.addArgument("-" + PRECISION_SMALL).type(Integer.class).setDefault(PRECISION_DEFAULT)
				.help(PRECISION_DESCRIPTION);
		hyperrectangle.addArgument("-" + OUTPUT_SMALL).type(Boolean.class).action(Arguments.storeTrue())
				.help(OUTPUT_DESCRIPTION);
		hyperrectangle.addArgument("--" + MINSUP_DETAILED).type(Integer.class).setDefault(MINSUP_DEFAULT)
				.help(MINSUP_DESCRIPTION);
		hyperrectangle.addArgument(ALGO_POS)
				.choices(MinIntChange.class.getSimpleName(), MinIntChangeIndex.class.getSimpleName())
				.help(ALGO_DESCRIPTION);
		hyperrectangle.addArgument(FILE_POS).help(FILE_DESCRIPTION);

		Subparser polygon = subparsers.addParser(POLYGON_SUBPARSER).help(POLYGON_SUBPARSER_DESCRIPTION);
		polygon.addArgument("-" + PRECISION_SMALL).type(Integer.class).setDefault(PRECISION_DEFAULT)
				.help(PRECISION_DESCRIPTION);
		polygon.addArgument("-" + OUTPUT_SMALL).type(Boolean.class).action(Arguments.storeTrue())
				.help(OUTPUT_DESCRIPTION);
		polygon.addArgument("--" + MINSUP_DETAILED).type(Integer.class).setDefault(MINSUP_DEFAULT)
				.help(MINSUP_DESCRIPTION);
		polygon.addArgument("--" + MAXSHAPE_DETAILED).type(Integer.class).setDefault(MAXSHAPE_DEFAULT)
				.help(MAXSHAPE_DESCRIPTION);
		polygon.addArgument("--" + MINAREA_DETAILED).type(String.class).setDefault(MINAREA_DEFAULT)
				.help(MINAREA_DESCRIPTION);
		polygon.addArgument("--" + MAXAREA_DETAILED).type(String.class).setDefault(MAXAREA_DEFAULT)
				.help(MAXAREA_DESCRIPTION);
		polygon.addArgument("--" + MINPERIMETER_DETAILED).type(double.class).setDefault(MINPERIMETER_DEFAULT)
				.help(MINPERIMETER_DESCRIPTION);
		polygon.addArgument("--" + MAXPERIMETER_DETAILED).type(double.class).setDefault(MAXPERIMETER_DEFAULT)
				.help(MAXPERIMETER_DESCRIPTION);

		polygon.addArgument(ALGO_POS).choices(ExtCbo.class.getSimpleName(), ExtremePointsEnum.class.getSimpleName(),
				DelaunayEnum.class.getSimpleName()).help(ALGO_DESCRIPTION);
		polygon.addArgument(FILE_POS).help(FILE_DESCRIPTION);

		Subparser polygonMCTS = subparsers.addParser(POLYGON_MCTS_SUBPARSER).help(POLYGON_MCTS_SUBPARSER_DESCRIPTION);
		polygonMCTS.addArgument("-" + PRECISION_SMALL).type(Integer.class).setDefault(PRECISION_DEFAULT)
				.help(PRECISION_DESCRIPTION);
		polygonMCTS.addArgument("-" + OUTPUT_SMALL).type(Boolean.class).action(Arguments.storeTrue())
				.help(OUTPUT_DESCRIPTION);
		polygonMCTS.addArgument("--" + MAXITERATION_DETAILED).type(Integer.class).setDefault(MAXITERATION_DEFAULT)
				.help(MAXITERATION_DESCRIPTION);
		polygonMCTS.addArgument("--" + MCTSSAMECLASS_DETAILED).type(Boolean.class).action(Arguments.storeTrue())
				.help(MCTSSAMECLASS_DESCRIPTION);
		polygonMCTS.addArgument("--" + MAXSEGMENTDISTANCE_DETAILED).type(double.class)
				.setDefault(MAXSEGMENTDISTANCE_DEFAULT).help(MAXSEGMENTDISTANCE_DESCRIPTION);
		polygonMCTS.addArgument("--" + MAXSEGMENTCOEFF_DETAILED).type(double.class).setDefault(MAXSEGMENTCOEFF_DEFAULT)
				.help(MAXSEGMENTCOEFF_DESCRIPTION);
		polygonMCTS.addArgument(FILE_POS).help(FILE_DESCRIPTION);

		try {
			Namespace ns = parser.parseArgs(args);
			switch (ns.getString(SUBPARSER_DEST)) {
			case HYPERRECTANGLE_SUBPARSER:
				HyperrectanglesAlgorithm.doTests(ns.getString(ALGO_POS), ns.getString(FILE_POS),
						ns.getInt(PRECISION_SMALL), ns.getInt(MINSUP_DETAILED), 1, ns.getBoolean(OUTPUT_SMALL));
				break;
			case POLYGON_SUBPARSER:
				ConstrainedPolygonsAlgorithm.doTests(ns.getString(ALGO_POS), ns.getString(FILE_POS),
						ns.getInt(PRECISION_SMALL), ns.getInt(MINSUP_DETAILED), ns.getInt(MAXSHAPE_DETAILED),
						new BigDecimal(ns.getString(MINAREA_DETAILED)), new BigDecimal(ns.getString(MAXAREA_DETAILED)),
						ns.getDouble(MINPERIMETER_DETAILED), ns.getDouble(MAXPERIMETER_DETAILED), 1,
						ns.getBoolean(OUTPUT_SMALL));
				break;
			case POLYGON_MCTS_SUBPARSER:
				MCTSExtremePointsEnum.doTest(ns.getString(FILE_POS), ns.getInt(PRECISION_SMALL),
						ns.getInt(MAXITERATION_DETAILED), ns.getBoolean(MCTSSAMECLASS_DETAILED),
						ns.getDouble(MAXSEGMENTDISTANCE_DETAILED), ns.getDouble(MAXSEGMENTCOEFF_DETAILED),
						ns.getBoolean(OUTPUT_SMALL));
				break;
			}
			return;
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}
	}
}
