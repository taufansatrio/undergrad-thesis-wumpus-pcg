package ai.wumpus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import jgap.WumpusSimulation;

public class Agent {
	// The grid (world) the agent can operate on
	private Grid grid;

	// Agent location
	private int agentXPos;
	private int agentYPos;

	// The agent's "memory"
	private MapEvaluator mapEvaluator;

	// The best cell to move to next turn
	private Tile nextTile;

	// The cell which the agent started on
	private Tile startTile;

	// Amount of arrows available to hunt the Wumpus
	private int ARROW_COUNT = 1;

	// Flag for end of game
	public static boolean IS_GAME_OVER = false;

	private Tile currentTile;

	// Counts the number of movements
	private int moveCounter;

	// Collection of visited tiles
	private ArrayList<Tile> uniqueTiles;

	public Agent(Grid agentGrid, int startX, int startY) {
		this.grid = agentGrid;

		this.agentXPos = startX;
		this.agentYPos = startY;

		this.mapEvaluator = new MapEvaluator(this.grid, startX, startY);
		startTile = grid.getTile(startX, startY);
		currentTile = grid.getTile(agentXPos, agentYPos);

		uniqueTiles = new ArrayList<Tile>();

		IS_GAME_OVER = false;
	}

	// Manually move the agent
	public void moveToTile(int directionConstant) {
		Tile neighbor = grid.getTile(agentXPos, agentYPos).getNeighborTile(
				directionConstant);
		moveToTile(neighbor);
	}

	// Move the agent to a specific cell
	public void moveToTile(Tile destination) {
		if (destination != null) {
			agentXPos = destination.getX();
			agentYPos = destination.getY();

			currentTile = destination;

			if (!uniqueTiles.contains(destination)) {
				uniqueTiles.add(destination);
			}
			moveCounter++;

			// Agent moves into dangerous tile
			if (destination.isWumpus()) {				
				// Log simulation statistics
				WumpusAI.setOutcome(0);
				WumpusAI.setCauseOfDeath("wumpus");
				WumpusAI.setSteps(moveCounter);
				WumpusAI.setUnique(uniqueTiles.size());
				WumpusAI.setMoveratio(uniqueTiles.size() / ((double) moveCounter));
				WumpusSimulation.setSumOfMoves(WumpusSimulation.getSumOfMoves()
						+ moveCounter);

				IS_GAME_OVER = true;
			} else if (destination.isPit()) {

				// Log simulation statistics
				WumpusAI.setOutcome(0);
				WumpusAI.setCauseOfDeath("pit");
				WumpusAI.setSteps(moveCounter);
				WumpusAI.setUnique(uniqueTiles.size());
				WumpusAI.setMoveratio(uniqueTiles.size() / ((double) moveCounter));
				WumpusSimulation.setSumOfMoves(WumpusSimulation.getSumOfMoves()
						+ moveCounter);

				IS_GAME_OVER = true;
			}
		}
	}

	// Selects the next cell to move to
	public void chooseNextTile() {
		Tile currentTile = grid.getTile(agentXPos, agentYPos);
		mapEvaluator.evaluateTile(currentTile);

		ArrayList<Tile> neighbors = mapEvaluator.getTilesNeighbors(currentTile);
		Tile bestNeighbor = chooseBestNeighbors(neighbors);
		nextTile = grid.getTile(bestNeighbor.getX(), bestNeighbor.getY());
	}

	// Selects the most desirable neighbor tile to move to.
	private Tile chooseBestNeighbors(ArrayList<Tile> neighbors) {

		// If there are neighbors we haven't visited, return first the neighbors
		// we haven't visited.
		ArrayList<Tile> nonVisited = Tile.where(neighbors,
				o -> o.isVisited() == false && o.getCost() <= 1);
		if (nonVisited.size() > 0) {
			return nonVisited.get(0);
		}

		// Check unvisitedSafeTiles
		ArrayList<Tile> unvisitedSafeTiles = new ArrayList<Tile>(grid
				.getUnvisitedSafeTiles().values());
		if (!unvisitedSafeTiles.isEmpty()) {
			Tile destination = unvisitedSafeTiles.remove(0);
			grid.getUnvisitedSafeTiles().remove(
					destination.getX() + "," + destination.getY());

			ArrayList<Tile> destinationPath = doBFS(
					currentTile,
					Tile.where(destination.getAllNeighbors(),
							o -> o.isVisited()).get(0));
			for (Tile tileInPath : destinationPath) {
				moveToTile(tileInPath);
			}
			return destination;
		}

		// Check for suspected Wumpus
		Tile likelyWumpusTile = mapEvaluator.getLikelyWumpusTile();
		if (likelyWumpusTile != null
				&& !(Tile.where(likelyWumpusTile.getAllNeighbors(),
						o -> o.isVisited()).isEmpty())) {
			ArrayList<Tile> destinationPath = doBFS(
					currentTile,
					Tile.where(likelyWumpusTile.getAllNeighbors(),
							o -> o.isVisited()).get(0));
			if (!destinationPath.isEmpty()) {
				if (destinationPath.size() > 1) {
					for (int ii = 0; ii < destinationPath.size() - 1; ii++) {
						moveToTile(destinationPath.get(ii));
					}
				}
				return destinationPath.get(destinationPath.size() - 1);
			}
		}

		// Check neighbors with smallest count of pit prob and wumpus prob
		ArrayList<Tile> nonVisitedNeighbors = Tile.where(neighbors,
				o -> o.isVisited() == false);
		int lowestRiskValue = Integer.MAX_VALUE;
		Tile targetTile = null;
		for (Tile aNeighbor : nonVisitedNeighbors) {
			if (aNeighbor.getBreezeCounter() + aNeighbor.getLairCounter() < lowestRiskValue) {
				lowestRiskValue = aNeighbor.getBreezeCounter()
						+ aNeighbor.getLairCounter();
				targetTile = aNeighbor;
			}
		}

		if (targetTile != null) {
			return targetTile;
		} else {
			// Agent surrenders

			// Log simulation statistics
			WumpusAI.setOutcome(0);
			WumpusAI.setCauseOfDeath("surrender");
			WumpusAI.setSteps(moveCounter);
			WumpusAI.setUnique(uniqueTiles.size());
			WumpusAI.setMoveratio(uniqueTiles.size() / ((double) moveCounter));
			WumpusSimulation.setSumOfMoves(WumpusSimulation.getSumOfMoves()
					+ moveCounter);
			
			IS_GAME_OVER = true;
			return currentTile;
		}

	}

	// Do BFS within all visited safe tiles
	private ArrayList<Tile> doBFS(Tile start, Tile dest) {
		ArrayList<Tile> result = new ArrayList<Tile>();

		HashMap<Tile, Boolean> flag = new HashMap<Tile, Boolean>();
		for (Tile t : grid.getAllMatchingTiles(o -> o.isVisited())) {
			flag.put(t, false);
		}

		HashMap<Tile, Tile> prev = new HashMap<Tile, Tile>();

		LinkedList<Tile> tileQ = new LinkedList<Tile>();
		tileQ.push(start);
		flag.put(start, true);

		while (!tileQ.isEmpty()) {
			Tile current = tileQ.pop();
			if (current.equals(dest))
				break;
			for (Tile neighborTile : Tile.where(current.getAllNeighbors(),
					o -> o.isVisited())) {
				if (flag.get(neighborTile) == false) {
					prev.put(neighborTile, current);
					tileQ.push(neighborTile);
					flag.put(neighborTile, true);
				}
			}
		}

		Tile targetDest = dest;

		while (!targetDest.equals(start)) {
			// System.out.println("BFS target dest: " + targetDest.getName());
			result.add(targetDest);
			targetDest = prev.get(targetDest);
		}
		Collections.reverse(result);
		return result;
	}

	// Moves the agent to the desired tile
	public void moveToBestTile() {
		if (nextTile != null) {
			moveToTile(nextTile);
		}
	}

	// If we are next to what we think is the Wumpus, then kill it!
	public boolean tryToKillWumpus() {
		if (mapEvaluator.getLikelyWumpusTile() == null || ARROW_COUNT <= 0)
			return false;

		Tile testTile = mapEvaluator.getLikelyWumpusTile();

		for (Tile neighbor : this.currentTile.getAllNeighbors()) {
			if ((testTile.getX() == neighbor.getX())
					&& (testTile.getY() == neighbor.getY())) {
				return killTheWumpus(neighbor);
			}
		}
		return false;
	}

	// Tries to kill the wumpus
	private boolean killTheWumpus(Tile suspectedTile) {
		if (ARROW_COUNT > 0) {
			this.ARROW_COUNT = 0;
			
			if (suspectedTile.isWumpus()) {
				// Agent killed wumpus
				mapEvaluator.wumpusKilled(suspectedTile);

				// Log simulation statistics
				WumpusAI.setOutcome(1);
				WumpusAI.setCauseOfDeath("n/a");
				WumpusAI.setSteps(moveCounter);
				WumpusAI.setUnique(uniqueTiles.size());
				WumpusAI.setMoveratio(uniqueTiles.size() / ((double) moveCounter));
				WumpusSimulation.setSumOfMoves(WumpusSimulation.getSumOfMoves()
						+ moveCounter);
				WumpusSimulation.setSumOfKilledWumpus(WumpusSimulation
						.getSumOfKilledWumpus() + 1);

				IS_GAME_OVER = true;
				return true;				
			}
		}
		return false;
	}

	public boolean haveAllSafeTilesBeenVisited() {
		ArrayList<Tile> unvisitedTiles = mapEvaluator
				.getAllTraversableUnvisitedTiles();
		if (!unvisitedTiles.isEmpty()) {
			ArrayList<Tile> safeTiles = Tile.where(unvisitedTiles,
					o -> o.isSafe());
			return safeTiles.isEmpty();
		}
		return false;
	}

	public boolean isAtStartTile() {
		return this.startTile == currentTile;
	}

	public Tile getCurrentTile() {
		return currentTile;
	}

	public void setCurrentTile(Tile currentTile) {
		this.currentTile = currentTile;
	}

}
