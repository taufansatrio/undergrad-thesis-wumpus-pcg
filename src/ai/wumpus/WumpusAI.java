package ai.wumpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WumpusAI {

	private Grid wumpusGrid;
	private Agent wumpusAI;

	private int startX;
	private int startY;

	// Simulations statistics
	private static double outcome;
	private static double steps;
	private static double unique;
	private static double moveratio;
	private static double runtime;

	private static String causeOfDeath;

	public WumpusAI(int[][] rawMap, int rows, int columns) {
		wumpusGrid = new Grid(rows, columns);
		setUpWumpusWorld(rawMap);
		wumpusAI = new Agent(wumpusGrid, startX, startY);
		addHazards();
	}

	public static void runSimulation(int[][] rawMap, int rows, int columns) {
		WumpusAI ai = new WumpusAI(rawMap, rows, columns);

		long startTime = System.currentTimeMillis();

		while (Agent.IS_GAME_OVER == false) {
			if (!ai.wumpusAI.tryToKillWumpus()) {
				ai.wumpusAI.chooseNextTile();
				ai.wumpusAI.moveToBestTile();
			}
		}

		long endTime = System.currentTimeMillis();
		double duration = (endTime - startTime) / 1000.00;
		WumpusAI.setRuntime(duration);
	}

	private void setUpWumpusWorld(int[][] rawMap) {

		// Set Tile properties based on rawMap
		for (int ii = 0; ii < rawMap.length; ii++) {
			for (int jj = 0; jj < rawMap[0].length; jj++) {
				// Initialize properties of a tile
				Map<String, Object> wumpusProps = new HashMap<String, Object>();
				wumpusProps.put(Tile.BREEZE, false);
				wumpusProps.put(Tile.LAIR, false);
				wumpusProps.put(Tile.VISITED, false);
				wumpusProps.put(Tile.COST, 1);
				wumpusProps.put(Tile.BREEZE_COUNTER, 0);
				wumpusProps.put(Tile.LAIR_COUNTER, 0);

				// Start Room
				if (rawMap[ii][jj] == 0) {
					String tileName = "start-" + ii + "," + jj;
					Tile startTile = new Tile(wumpusProps, tileName, ii, jj);
					wumpusGrid.setTile(startTile, ii, jj);
					this.startX = ii;
					this.startY = jj;
				}

				// Empty Room
				if (rawMap[ii][jj] == 1) {
					String tileName = "empty-" + ii + "," + jj;
					Tile emptyTile = new Tile(wumpusProps, tileName, ii, jj);
					wumpusGrid.setTile(emptyTile, ii, jj);
				}

				// NE_SW Tunnel
				if (rawMap[ii][jj] == 2) {
					String tileName = Tile.NE_SW_TUNNEL + "-" + ii + "," + jj;
					Tile neTunnel = new Tile(wumpusProps, tileName, ii, jj);
					wumpusGrid.setTile(neTunnel, ii, jj);
				}

				// NW_SE Tunnel
				if (rawMap[ii][jj] == 3) {
					String tileName = Tile.NW_SE_TUNNEL + "-" + ii + "," + jj;
					Tile nwTunnel = new Tile(wumpusProps, tileName, ii, jj);
					wumpusGrid.setTile(nwTunnel, ii, jj);
				}

				// Pit Room
				if (rawMap[ii][jj] == 4) {
					String tileName = Tile.PIT + "-" + ii + "," + jj;
					Tile pitTile = new Tile(wumpusProps, tileName, ii, jj);
					wumpusGrid.setTile(pitTile, ii, jj);
				}

				// Wumpus Room
				if (rawMap[ii][jj] == 5) {
					String tileName = Tile.WUMPUS + "-" + ii + "," + jj;
					Tile wumpusTile = new Tile(wumpusProps, tileName, ii, jj);
					wumpusGrid.setTile(wumpusTile, ii, jj);
				}
			}
		}
	}

	private void addHazards() {

		// Set all of the cardinal neighbors to the wumpus as smelly
		ArrayList<Tile> wumpusTiles = wumpusGrid.getAllMatchingTiles(o -> o
				.isWumpus());
		for (Tile wumpusTile : wumpusTiles) {
			ArrayList<Tile> neighbors = wumpusTile.getAllNeighbors();
			for (Tile neighborTile : neighbors) {
				if (!neighborTile.isPit()) {
					neighborTile.getWumpusProps().put(Tile.LAIR, true);
				}
			}
		}

		// Set all of the cardinal neighbors to the pit as breezy
		ArrayList<Tile> pitTiles = wumpusGrid.getAllMatchingTiles(o -> o
				.isPit());
		for (Tile pitTile : pitTiles) {
			ArrayList<Tile> neighbors = pitTile.getAllNeighbors();
			for (Tile neighborTile : neighbors) {
				if (!neighborTile.isWumpus()) {
					neighborTile.getWumpusProps().put(Tile.BREEZE, true);
				}
			}
		}
	}

	// Getter and setter methods for logging purpose
	public static double getOutcome() {
		return outcome;
	}

	public static void setOutcome(double outcome) {
		WumpusAI.outcome = outcome;
	}

	public static double getSteps() {
		return steps;
	}

	public static void setSteps(double steps) {
		WumpusAI.steps = steps;
	}

	public static double getUnique() {
		return unique;
	}

	public static void setUnique(double unique) {
		WumpusAI.unique = unique;
	}

	public static double getMoveratio() {
		return moveratio;
	}

	public static void setMoveratio(double moveratio) {
		WumpusAI.moveratio = moveratio;
	}

	public static double getRuntime() {
		return runtime;
	}

	public static void setRuntime(double runtime) {
		WumpusAI.runtime = runtime;
	}

	public static String getCauseOfDeath() {
		return causeOfDeath;
	}

	public static void setCauseOfDeath(String causeOfDeath) {
		WumpusAI.causeOfDeath = causeOfDeath;
	}

}