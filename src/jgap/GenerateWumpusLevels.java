package jgap;

import java.io.PrintWriter;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.BestChromosomesSelector;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.MutationOperator;

/**
 * Main class where generation of Wumpus levels by Genetic Algorithm occurs
 * 
 * @author taufansatrio
 *
 */

public class GenerateWumpusLevels {
	// Constant for number of rows
	private static int ROWS = WumpusSimulation.ROWS;
	
	// Constant for number of columns
	private static int COLUMNS = WumpusSimulation.COLUMNS;
	
	// Constant for population size
	private static final int POPULATION_SIZE = 50;
	
	// Constant for number of generation
	private static final int NO_OF_GENERATION = 50;
	
	// Constant for selection rate
	private static final double SELECTION_RATE = 0.35d;
	
	// Constant for crossover rate
	private static final double CROSSOVER_RATE = 0.35d;
	
	// Constant for mutation rate
	private static final int MUTATION_RATE_DENOMINATOR = 12;
	
	public static int[][] generate(PrintWriter logWriter, String difficulty) {
		long startTime = System.currentTimeMillis();

		// The result array to be returned
		int[][] rawMap = new int[ROWS][COLUMNS];

		// Start with a DefaultConfiguration for the most common settings.
		Configuration conf = new DefaultConfiguration();
		
		try {
			// Set selection rate
			BestChromosomesSelector bestChromsSelector = new BestChromosomesSelector(
					conf, SELECTION_RATE);
			bestChromsSelector.setDoubletteChromosomesAllowed(true);
			conf.addNaturalSelector(bestChromsSelector, false);
			
			// Set crossover rate
			conf.addGeneticOperator(new CrossoverOperator(conf, CROSSOVER_RATE));
			
			// Set mutation rate
			conf.addGeneticOperator(new MutationOperator(conf, MUTATION_RATE_DENOMINATOR));

			// Set the fitness function
			FitnessFunction myFunc = new WumpusFitnessFunction(difficulty);
			
			conf.setFitnessFunction(myFunc);
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Chromosome setup
		Chromosome sampleChromosome;

		try {
			IntegerGene[] sampleGene = new IntegerGene[ROWS * COLUMNS];

			// ID for a gene:
			// 0 = Start room
			// 1 = Empty room
			// 2 = NE_SW tunnel
			// 3 = NW_SE tunnel
			// 4 = Pit room
			// 5 = Wumpus room
			for (int ii = 0; ii < ROWS * COLUMNS; ii++) {
				sampleGene[ii] = new IntegerGene(conf, 0, 5);
			}

			sampleChromosome = new Chromosome(conf, sampleGene);
			conf.setSampleChromosome(sampleChromosome);
			conf.setPopulationSize(POPULATION_SIZE);

			Genotype population = Genotype.randomInitialGenotype(conf);
			IChromosome bestSolutionSoFar = population.getFittestChromosome();
			System.out.printf("Fitness at 0: %3.3f\n", bestSolutionSoFar.getFitnessValue());

			for (int i = 1; i <= NO_OF_GENERATION; i++) {
				population.evolve();
				
				bestSolutionSoFar = population.getFittestChromosome();
//				if (i % 10 == 0) {
//					System.out.printf("Fitness at %d: %3.3f\n", i, bestSolutionSoFar.getFitnessValue());
//				}
			}
			System.out.println("Final fitness is: "
					+ bestSolutionSoFar.getFitnessValue());
			WumpusSimulation.fitnessLogger.addValue(bestSolutionSoFar.getFitnessValue());

			rawMap = convertChromosomeToLevel(bestSolutionSoFar);

			// Print raw map
			for (int ii = 0; ii < ROWS; ii++) {
				logWriter.print("{");
				for (int jj = 0; jj < COLUMNS; jj++) {
					if (jj == COLUMNS - 1) {
						logWriter.print(rawMap[ii][jj]);
					} else
					logWriter.print(rawMap[ii][jj] + ",");
				}
				logWriter.println("},");
			}

			long endTime = System.currentTimeMillis();
			double duration = (endTime - startTime) / 1000.0;
			WumpusSimulation.levelGenDurationLogger.addValue(duration);
			logWriter.println("Level generation runtime is " + duration
					+ " seconds");

		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		Configuration.reset();
		return rawMap;
	}

	public static int[][] convertChromosomeToLevel(IChromosome currentSolution) {
		int[][] map = new int[ROWS][COLUMNS];

		// Convert to two dimensional array
		for (int ii = 0; ii < ROWS * COLUMNS; ii++) {
			int cellValue = (int) currentSolution.getGene(ii).getAllele();
			if (ROWS * COLUMNS >= 100) {
				map[ii / ROWS][ii % COLUMNS] = cellValue;
			} else {
				map[ii / 10][ii % 10] = cellValue;
			}
		}
		return map;
	}

}