package ai.wumpus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MapEvaluator {
	// Cost of a safe tile
	private final int SAFE_COST = 1;
	// Cost of a lair or breeze
	private final int POTENTIAL_DANGER_COST = 2;
	// Cost of a wumpus or pit
	private final int DANGER_COST = 3;

	// The grid (world) the agent can operate on
	// Also represents the agent's memory and evaluation of future tiles
	private Grid grid;

	// Wumpus confidence threshold
	private final int WUMPUS_CONFIDENCE_THRESHOLD = 30;

	// Whether the Wumpus is killed or not
	private boolean wumpusKilled = false;

	public MapEvaluator(Grid worldGrid, int startX, int startY) {
		// System.out.println("Log: MapEvaluator constructor called");
		this.grid = worldGrid;
		this.grid.informTilesOfTheirNeighbors();
	}

	// Evaluates a tile by determining cost and neighbors
	public void evaluateTile(Tile aTile) {

		if (!aTile.isVisited()) {
			assignCostForCurrentTile(aTile);
			estimateNeighbors(aTile);
			resolveFalseRatings();
		}
	}

	private void assignCostForCurrentTile(Tile aTile) {
		if (aTile.isBreezy() || aTile.isLair() && !wumpusKilled) {
			aTile.setCost(POTENTIAL_DANGER_COST);
			grid.getUnvisitedSafeTiles().remove(
					aTile.getX() + "," + aTile.getY());
		} else if (aTile.isPit() || aTile.isWumpus() && !wumpusKilled) {
			aTile.setCost(DANGER_COST);
			grid.getUnvisitedSafeTiles().remove(
					aTile.getX() + "," + aTile.getY());
		} else {
			aTile.setCost(SAFE_COST);
		}
		aTile.setVisited(true);
	}

	// Calculates the likely value of any neighbor tiles
	private void estimateNeighbors(Tile aTile) {
		// Update wumpusProb and pitProb for 1-tile and 2-tile neighbors
		if (aTile.isBreezy()) {
			ArrayList<Tile> breezeAffectedNeighbors = aTile.getAllNeighbors();
			for (Tile neighbor : breezeAffectedNeighbors) {
				neighbor.setBreezeCounter(neighbor.getBreezeCounter() + 1);

				// If the neighbor has not been visited then it is a potential
				// danger
				// (Because the current tile is adjacent to a danger square)
				// Update the cost
				if (!neighbor.isVisited()) {
					neighbor.setCost(POTENTIAL_DANGER_COST);
					grid.getUnvisitedSafeTiles().remove(
							neighbor.getX() + "," + neighbor.getY());
				}
			}
		}

		if (aTile.isLair()) {
			ArrayList<Tile> lairAffectedNeighbors = grid
					.getWumpusNeighbors(aTile);
			for (Tile neighbor : lairAffectedNeighbors) {
				neighbor.setLairCounter(neighbor.getLairCounter() + 1);

				// If the neighbor has not been visited then it is a potential
				// danger
				// (Because the current tile is adjacent to a danger square)
				// Update the cost
				if (!neighbor.isVisited()) {
					neighbor.setCost(POTENTIAL_DANGER_COST);
					grid.getUnvisitedSafeTiles().remove(
							neighbor.getX() + "," + neighbor.getY());
				}
			}
		}

		if (!aTile.isLair() && !aTile.isBreezy()) {
			for (Tile aNeighbor : Tile.where(aTile.getAllNeighbors(),
					o -> !o.isVisited())) {
				if (aNeighbor.isSafe()) {
					grid.getUnvisitedSafeTiles().put(
							aNeighbor.getX() + "," + aNeighbor.getY(),
							aNeighbor);
				}
			}
		}

	}

	// Loop through all of the tiles
	// The likely cost of any unvisited tiles which have neighbors who have been
	// visited can be calculated
	private void resolveFalseRatings() {
		// // System.out.println("Log: resolveFalseRatings called");
		ArrayList<Tile> allTiles = grid.getAllMatchingTiles(o -> !o
				.isTunnelNE() && !o.isTunnelNW());

		for (Tile unknown : allTiles) {
			if (!unknown.isVisited()) {
				// Get all visited neighbor tiles, if any
				ArrayList<Tile> neighbors = unknown.getAllNeighbors();

				ArrayList<Tile> visitedNeighbors = Tile.where(neighbors,
						o -> o.isVisited());

				if (visitedNeighbors.size() > 0) {
					estimateUnknownTilesDanger(unknown, visitedNeighbors);
				}
			}
		}
	}

	private void estimateUnknownTilesDanger(Tile aTile,
			ArrayList<Tile> visitedNeighbors) {
		ArrayList<Tile> safeTiles = Tile.where(visitedNeighbors,
				o -> o.getCost() == SAFE_COST);

		// If there are any safe neighbors the unknown tile cannot be dangerous
		if (safeTiles.size() > 0) {
			aTile.setCost(SAFE_COST);
			if (!aTile.isVisited()) {
				grid.getUnvisitedSafeTiles().put(
						aTile.getX() + "," + aTile.getY(), aTile);
			}
		} else {
			// Else there were neighbors, but none of them were safe
			aTile.setCost(DANGER_COST);
			if (!aTile.isVisited()) {
				grid.getUnvisitedSafeTiles().remove(
						aTile.getX() + "," + aTile.getY());
			}
		}
		checkForInconsistentDangers(aTile, visitedNeighbors);
		markTheWumpus();
	}

	// A corner case in false ratings for a situation such as
	//
	// { unknown } { breezy }
	// { smelly }
	//
	// Since the dangers are inconsistant, the unknown tile must be safe
	//
	private void checkForInconsistentDangers(Tile aTile,
			ArrayList<Tile> visitedNeighbors) {
		ArrayList<Tile> dangerousNeighbors = Tile.where(visitedNeighbors,
				o -> o.getCost() == POTENTIAL_DANGER_COST);
		ArrayList<Tile> lairNeighbors = Tile.where(dangerousNeighbors,
				o -> (grid.getTile(o.getX(), o.getY())).isLair());
		ArrayList<Tile> breezyNeighbors = Tile.where(dangerousNeighbors,
				o -> (grid.getTile(o.getX(), o.getY())).isBreezy());

		if (dangerousNeighbors.size() > 2 && lairNeighbors.size() > 0
				&& breezyNeighbors.size() > 0) {
			if (lairNeighbors.size() != dangerousNeighbors.size()
					&& breezyNeighbors.size() == dangerousNeighbors.size())
				return;
			if (breezyNeighbors.size() != dangerousNeighbors.size()
					&& lairNeighbors.size() == dangerousNeighbors.size())
				return;

			aTile.setCost(SAFE_COST);
			if (!aTile.isVisited()) {
				grid.getUnvisitedSafeTiles().put(
						aTile.getX() + "," + aTile.getY(), aTile);
			}
		}
	}

	private void markTheWumpus() {
		ArrayList<Tile> allTiles = grid.getAllMatchingTiles(o -> true);
		ArrayList<Tile> dangerousTiles = Tile.where(allTiles, o -> !o.isSafe());

		for (Tile aTile : dangerousTiles) {

			ArrayList<Tile> visitedNeighbors = Tile.where(
					aTile.getAllNeighbors(), o -> o.isVisited());
			ArrayList<Tile> lairNeighbors = Tile.where(visitedNeighbors,
					o -> (grid.getTile(o.getX(), o.getY())).isLair());

			if (lairNeighbors.size() == visitedNeighbors.size()) {
				aTile.getWumpusProps().put("WumpusProbability",
						(10 * lairNeighbors.size()));
			}
		}
	}

	public ArrayList<Tile> getTilesNeighbors(Tile aTile) {
		Tile tileInMemory = grid.getTile(aTile.getX(), aTile.getY());
		return tileInMemory.getAllNeighbors();
	}

	public ArrayList<Tile> getAllTraversableUnvisitedTiles() {
		ArrayList<Tile> allTiles = grid.getAllMatchingTiles(o -> true);

		ArrayList<Tile> unvisitedTiles = new ArrayList<Tile>();

		for (Tile aTile : allTiles) {
			if (!aTile.isVisited()) {
				// Get all of the tiles visited neighbors, if any
				ArrayList<Tile> visitedNeighbors = Tile.where(
						aTile.getAllNeighbors(), o -> o.isVisited());

				if (visitedNeighbors.size() > 0)
					unvisitedTiles.add(aTile);
			}
		}
		return unvisitedTiles;
	}

	public Tile getLikelyWumpusTile() {
		ArrayList<Tile> orderedWumpusTiles = grid.getAllMatchingTiles(o -> o
				.getWumpusProps().containsKey("WumpusProbability"));

		if (orderedWumpusTiles.size() > 0) {
			Collections.sort(orderedWumpusTiles, new WumpusProbComparator());

			int currentWumpusConfidence = Integer.parseInt(orderedWumpusTiles
					.get(orderedWumpusTiles.size() - 1).getWumpusProps()
					.get("WumpusProbability").toString());
			if (currentWumpusConfidence >= WUMPUS_CONFIDENCE_THRESHOLD) {
				return orderedWumpusTiles.get(orderedWumpusTiles.size() - 1);
			}
		}
		return null;
	}

	public void wumpusKilled(Tile aTile) {
		if (!aTile.isBreezy()) {
			this.wumpusKilled = true;
			aTile.setCost(SAFE_COST);
			if (!aTile.isVisited()) {
				grid.getUnvisitedSafeTiles().put(
						aTile.getX() + "," + aTile.getY(), aTile);
			}

			ArrayList<Tile> wumpusNeighbors = aTile.getAllNeighbors();
			for (Tile neighborTile : wumpusNeighbors) {
				if ((grid.getTile(neighborTile.getX(), neighborTile.getY()))
						.isBreezy())
					continue;

				neighborTile.setCost(SAFE_COST);
				if (!aTile.isVisited()) {
					grid.getUnvisitedSafeTiles().put(
							neighborTile.getX() + "," + neighborTile.getY(),
							neighborTile);
				}
			}
		}
	}

}

class WumpusProbComparator implements Comparator<Tile> {
	@Override
	public int compare(Tile t1, Tile t2) {
		int wumpusProb1 = (int) t1.getWumpusProps().get("WumpusProbability");
		int wumpusProb2 = (int) t2.getWumpusProps().get("WumpusProbability");

		if (wumpusProb1 == wumpusProb2)
			return 0;
		if (wumpusProb1 > wumpusProb2)
			return 1;
		return -1;
	}
}
