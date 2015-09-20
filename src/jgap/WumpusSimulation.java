package jgap;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import ai.wumpus.WumpusAI;

public class WumpusSimulation {
	public static PrintWriter logWriter;

	public static SummaryStatistics fitnessLogger;
	public static SummaryStatistics levelGenDurationLogger;

	// Number of levels to simulate
	public static final int NO_OF_LEVELS = 5;

	// Rows and columns in a level
	public static final int ROWS = 7;
	public static final int COLUMNS = 10;

	private static int sumOfMoves = 0;
	private static int sumOfKilledWumpus = 0;

	public static void main(String[] args) throws FileNotFoundException,
			UnsupportedEncodingException {
		long simStartTime = System.currentTimeMillis();

		// Create file writer
		logWriter = new PrintWriter("parameter-tweaking.txt", "UTF-8");

		// Create simulation logger
		PrintWriter simulationLogger = new PrintWriter("tmp.txt", "UTF-8");

		// Log timestamp
		Date date = new Date();
		logWriter.println("Experiment started at "
				+ new Timestamp(date.getTime()) + " with " + NO_OF_LEVELS
				+ " levels");
		logWriter
				.println("--------------------------------------------------------------------");

		fitnessLogger = new SummaryStatistics();
		levelGenDurationLogger = new SummaryStatistics();

		// Run experiment
		for (int ii = 0; ii < NO_OF_LEVELS; ii++) {
			// Generate the level from GA
			int[][] rawMap = GenerateWumpusLevels.generate(logWriter, "easy");

			// Record map information
			String[] mapElements = WumpusFitnessFunction.getMapElements(rawMap);
			simulationLogger.printf("%s,%3.3f,%s,", mapElements[0],
					Double.parseDouble(mapElements[1]), mapElements[2]);

			// Run AI simulation
			WumpusAI.runSimulation(rawMap, COLUMNS, ROWS);

			simulationLogger.printf("%d,%d,%d,%3.3f,%3.3f,hard\n",
					(int) WumpusAI.getOutcome(), (int) WumpusAI.getSteps(),
					(int) WumpusAI.getUnique(), WumpusAI.getMoveratio(),
					WumpusAI.getRuntime());

			simulationLogger.flush();
		}
		logWriter
				.println("--------------------------------------------------------------------");

		long simEndTime = System.currentTimeMillis();
		double duration = (simEndTime - simStartTime) / 1000.000;
		logWriter.println("Total simulation runtime is " + duration
				+ " seconds");

		logWriter.printf("Mean of fitness from %d levels: %3.3f\n",
				NO_OF_LEVELS, fitnessLogger.getMean());
		logWriter.printf("StDev of fitness from %d levels: %3.3f\n",
				NO_OF_LEVELS, fitnessLogger.getStandardDeviation());

		logWriter.printf(
				"Mean of generation duration from %d levels: %3.3f seconds\n",
				NO_OF_LEVELS, levelGenDurationLogger.getMean());
		logWriter.printf(
				"StDev of generation duration from %d levels: %3.3f seconds\n",
				NO_OF_LEVELS, levelGenDurationLogger.getStandardDeviation());

		logWriter.close();
		simulationLogger.close();
	}

	public static int getSumOfMoves() {
		return sumOfMoves;
	}

	public static void setSumOfMoves(int sumOfMoves) {
		WumpusSimulation.sumOfMoves = sumOfMoves;
	}

	public static int getSumOfKilledWumpus() {
		return sumOfKilledWumpus;
	}

	public static void setSumOfKilledWumpus(int sumOfKilledWumpus) {
		WumpusSimulation.sumOfKilledWumpus = sumOfKilledWumpus;
	}

}