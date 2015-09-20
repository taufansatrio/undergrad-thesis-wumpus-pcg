package jgap;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

import ai.wumpus.WumpusAI;

@SuppressWarnings("serial")
public class WumpusFitnessFunction extends FitnessFunction {
	
	private String targetDifficulty = "";
	private final int ROWS = WumpusSimulation.ROWS;
	private final int COLUMNS = WumpusSimulation.COLUMNS;
	
	/**
     * Constructs WumpusFitnessFunction with the desired
     * level of difficulty.
     *
     * @param targetDifficulty The desired level of difficulty,
     *                         "easy", "medium", or "hard" 
     */
    public WumpusFitnessFunction( String targetDifficulty )
    {
        this.targetDifficulty = targetDifficulty.toLowerCase();
    }

    /**
     * Determine the fitness of the given Chromosome instance. The higher the
     * return value, the more fit the instance. This method should always
     * return the same fitness value for two equivalent Chromosome instances.
     *
     * @param subject: The Chromosome instance to evaluate.
     *
     * @return A positive integer reflecting the fitness rating of the given
     *         Chromosome.
     */
	@Override
	protected double evaluate(IChromosome subject) {		
		// The fitness measures difficulty of a level generated from the Chromosome.
		
		// Step 1: run synthetic player simulation
		int[][] map = GenerateWumpusLevels.convertChromosomeToLevel(subject);
		WumpusAI.runSimulation(map, COLUMNS, ROWS);
		
		// Step 2: record and normalize the statistics 
		// Simulation (steps, unique, moveratio, runtime)
		// Level (noOfTunnels, roomToTunnelsRatio, noOfPits)
				
		double steps = WumpusAI.getSteps();
		double unique = WumpusAI.getUnique();
		double moveratio = WumpusAI.getMoveratio();
		double runtime = WumpusAI.getRuntime();
		
		String[] mapElements = getMapElements(map);
		
		double noOfTunnels = Double.parseDouble(mapElements[0]);
		double roomsToTunnelsRatio = Double.parseDouble(mapElements[1]);
		
		// Normalizer for noOfPits
		NormUtil noOfPitsNormalizer = new NormUtil(1, 3, 0, 1);
		double noOfPits = noOfPitsNormalizer.normalize(Double.parseDouble(mapElements[2]));
		
		double noOfWumpus = Double.parseDouble(mapElements[3]);
		double noOfStart = Double.parseDouble(mapElements[4]);
				
		// Step 3: calculate fitness with the formula and return the fitness
		double fitness = 0;
		
		// Initialize target value for each features
		double targetSteps = 0;
		double targetUnique = 0;
		double targetMoveratio = 0;
		double targetRuntime = 0;
		
		double targetNoOfTunnels = 0;
		double targetRoomsToTunnelsRatio = 0;
		double targetNoOfPits = 0;
		
		if (targetDifficulty.equals("easy")) {
			// Normalizer for steps
			NormUtil stepsNormalizer = new NormUtil(2, 1052, 0, 1);
			
			// Normalizer for unique
			NormUtil uniqueNormalizer = new NormUtil(2, 66, 0, 1);
			
			// Normalizer for moveratio
			NormUtil moveRatioNormalizer = new NormUtil(0.059, 1, 0, 1);
			
			// Normalizer for runtime
			NormUtil runtimeNormalizer = new NormUtil(0, 0.074, 0, 1);
			
			// Normalizer for tunnels
			NormUtil tunnelsNormalizer = new NormUtil(2, 28, 0, 1);
			
			// Normalizer for roomTunnelRatio
			NormUtil roomToTunnelsRatioNormalizer = new NormUtil(1.321, 32.5, 0, 1);
			
			steps = stepsNormalizer.normalize(steps);
			unique = uniqueNormalizer.normalize(unique);
			moveratio = moveRatioNormalizer.normalize(moveratio);
			runtime = runtimeNormalizer.normalize(runtime);
			
			noOfTunnels = tunnelsNormalizer.normalize(noOfTunnels);
			roomsToTunnelsRatio = roomToTunnelsRatioNormalizer.normalize(roomsToTunnelsRatio);
			
			targetSteps = 0.234;
			targetUnique = 0.703;
			targetMoveratio = 0.271;
			targetRuntime = 0.071;
			
			targetNoOfTunnels = 0.461;
//			System.out.println(tunnelNormalizer.normalize(13.991));			
			targetRoomsToTunnelsRatio = 0.086;
			targetNoOfPits = 0.727;
		} else if (targetDifficulty.equals("medium")) {
			// Normalizer for steps
			NormUtil stepsNormalizer = new NormUtil(2, 480, 0, 1);
			
			// Normalizer for unique
			NormUtil uniqueNormalizer = new NormUtil(1, 47, 0, 1);
			
			// Normalizer for moveratio
			NormUtil moveRatioNormalizer = new NormUtil(0.09, 1, 0, 1);
			
			// Normalizer for runtime
			NormUtil runtimeNormalizer = new NormUtil(0, 0.062, 0, 1);
			
			// Normalizer for tunnels
			NormUtil tunnelsNormalizer = new NormUtil(18, 53, 0, 1);
			
			// Normalizer for roomTunnelRatio
			NormUtil roomToTunnelsRatioNormalizer = new NormUtil(0.226, 2.667, 0, 1);
			
			steps = stepsNormalizer.normalize(steps);
			unique = uniqueNormalizer.normalize(unique);
			moveratio = moveRatioNormalizer.normalize(moveratio);
			runtime = runtimeNormalizer.normalize(runtime);
			
			noOfTunnels = tunnelsNormalizer.normalize(noOfTunnels);
			roomsToTunnelsRatio = roomToTunnelsRatioNormalizer.normalize(roomsToTunnelsRatio);
			
			targetSteps = 0.245;
			targetUnique = 0.562;
			targetMoveratio = 0.234;
			targetRuntime = 0.036;
			
			targetNoOfTunnels = 0.485;
			targetRoomsToTunnelsRatio = 0.277;
			targetNoOfPits = 0.711;
		} else {
			// Normalizer for steps
			NormUtil stepsNormalizer = new NormUtil(2, 244, 0, 1);
			
			// Normalizer for unique
			NormUtil uniqueNormalizer = new NormUtil(1, 33, 0, 1);
			
			// Normalizer for moveratio
			NormUtil moveRatioNormalizer = new NormUtil(0.126, 1, 0, 1);
			
			// Normalizer for runtime
			NormUtil runtimeNormalizer = new NormUtil(0, 0.032, 0, 1);
			
			// Normalizer for tunnels
			NormUtil tunnelsNormalizer = new NormUtil(31, 63, 0, 1);
			
			// Normalizer for roomTunnelRatio
			NormUtil roomToTunnelsRatioNormalizer = new NormUtil(0.048, 1.129, 0, 1);
			
			steps = stepsNormalizer.normalize(steps);
			unique = uniqueNormalizer.normalize(unique);
			moveratio = moveRatioNormalizer.normalize(moveratio);
			runtime = runtimeNormalizer.normalize(runtime);
			
			noOfTunnels = tunnelsNormalizer.normalize(noOfTunnels);
			roomsToTunnelsRatio = roomToTunnelsRatioNormalizer.normalize(roomsToTunnelsRatio);
			
			targetSteps = 0.147;
			targetUnique = 0.356;
			targetMoveratio = 0.371;
			targetRuntime = 0.026;
			
			targetNoOfTunnels = 0.556;
			targetRoomsToTunnelsRatio = 0.282;
			targetNoOfPits = 0.684;
		}
		
		// Exactly one wumpus and one start room for any level of difficulty
		double targetNoOfWumpus = 1.0;
		double targetNoOfStart = 1.0;
		
		// Square of difference for each features
		double squareOfDiffSteps = Math.pow(targetSteps - steps, 2);
		double squareOfDiffUnique = Math.pow(targetUnique - unique, 2);
		double squareOfDiffMoveratio = Math.pow(targetMoveratio - moveratio, 2);
		double squareOfDiffRuntime = Math.pow(targetRuntime - runtime, 2);
		
		double squareOfDiffNoOfTunnels = Math.pow(targetNoOfTunnels - noOfTunnels, 2);
		double squareOfDiffRoomsToTunnelsRatio = Math.pow(targetRoomsToTunnelsRatio - roomsToTunnelsRatio, 2);
		double squareOfDiffNoOfPits = Math.pow(targetNoOfPits - noOfPits, 2);
		double squareOfDiffNoOfWumpus = Math.pow(targetNoOfWumpus - noOfWumpus, 2);
		double squareOfDiffNoOfStart = Math.pow(targetNoOfStart - noOfStart, 2);
		
		
		// Euclidean distance of features 
		fitness = Math.sqrt(squareOfDiffSteps + squareOfDiffUnique
				+ squareOfDiffMoveratio + squareOfDiffRuntime
				+ squareOfDiffNoOfTunnels + squareOfDiffRoomsToTunnelsRatio
				+ squareOfDiffNoOfPits + squareOfDiffNoOfWumpus
				+ squareOfDiffNoOfStart);
		if (Double.isNaN(fitness)) {
			return 0;
		}
		// Normalize
		fitness = 100.0 - fitness;
//		System.out.println(fitness);
		return fitness;
	}
	
	public static String[] getMapElements(int[][] map) {
		// 0: noOfTunnels
		// 1: room/tunnel ratio
		// 2: noOfPits
		int[] mapElements = new int[5];
		String[] mapElementsStr = new String[5];
		
		for (int ii = 0; ii < map.length; ii++) {
			for (int jj = 0; jj < map[0].length; jj++) {
				if (map[ii][jj] == 2 || map[ii][jj] == 3) mapElements[0]++;
				else if (map[ii][jj] == 1) mapElements[1]++;
				else if (map[ii][jj] == 4) mapElements[2]++;
				else if (map[ii][jj] == 5) mapElements[3]++;
				else if (map[ii][jj] == 0) mapElements[4]++;
				
			}
		}
		mapElementsStr[0] = mapElements[0] + "";
		mapElementsStr[1] = ((double) mapElements[1] / mapElements[0]) + ""; 
		mapElementsStr[2] = mapElements[2] + "";
		mapElementsStr[3] = mapElements[3] + "";
		mapElementsStr[4] = mapElements[4] + "";
				
		return mapElementsStr;
	}
	
}
