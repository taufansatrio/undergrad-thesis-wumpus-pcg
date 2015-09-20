package ai.wumpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Tile {

	// Directional constants
	public static final int DIRECTIONS_NORTH = 0;
	public static final int DIRECTIONS_EAST = 1;
	public static final int DIRECTIONS_SOUTH = 2;
	public static final int DIRECTIONS_WEST = 3;

	// Properties of a Wumpus Tile
	private Map<String, Object> wumpusProps = new HashMap<String, Object>();
	private String name;
	private int x;
	private int y;
	private Map<Integer, Tile> neighbors;

	public Tile(Map<String, Object> wumpusProps, String name, int x, int y) {
		this.wumpusProps = wumpusProps;
		this.name = name;
		this.x = x;
		this.y = y;
		neighbors = new HashMap<Integer, Tile>();
	}

	// Registers a given Tile as a neighbor in a given direction
	public void setNeighborTile(Tile neighbor, int directionConstant) {
		neighbors.put(directionConstant, neighbor);
	}

	// Returns all neighbor tiles
	public ArrayList<Tile> getAllNeighbors() {
		return new ArrayList<Tile>(neighbors.values());
	}

	// Returns a neighbor tile from a given direction
	public Tile getNeighborTile(int directionConstant) {
		return neighbors.get(directionConstant);
	}

	public boolean equals(Tile anotherTile) {
		return x == anotherTile.getX() && y == anotherTile.getY();
	}

	// Getter and setter methods
	public Map<String, Object> getWumpusProps() {
		return wumpusProps;
	}

	public void setWumpusProps(Map<String, Object> wumpusProps) {
		this.wumpusProps = wumpusProps;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Map<Integer, Tile> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(Map<Integer, Tile> neighbors) {
		this.neighbors = neighbors;
	}

	// Helper methods
	public static final String NE_SW_TUNNEL = "ne_sw_tunnel";
	public static final String NW_SE_TUNNEL = "nw_se_tunnel";

	public static final String PIT = "pit";
	public static final String BREEZE = "breeze";
	public static final String WUMPUS = "wumpus";
	public static final String LAIR = "lair";

	public static final String VISITED = "visited";
	public static final String COST = "cost";

	public static final String BREEZE_COUNTER = "breeze_counter";
	public static final String LAIR_COUNTER = "lair_counter";

	public boolean isTunnelNE() {

		return name.split("-")[0].equals(NE_SW_TUNNEL);
	}

	public boolean isTunnelNW() {

		return name.split("-")[0].equals(NW_SE_TUNNEL);
	}

	public boolean isPit() {

		return name.split("-")[0].equals(PIT);
	}

	public boolean isBreezy() {
		return wumpusProps.get(BREEZE) == (Boolean) true;
	}

	public boolean isWumpus() {
		return name.split("-")[0].equals(WUMPUS);
	}

	public boolean isLair() {
		return wumpusProps.get(LAIR) == (Boolean) true;
	}

	public boolean isVisited() {
		return wumpusProps.get(VISITED) == (Boolean) true;
	}

	public int getCost() {
		return Integer.parseInt(wumpusProps.get(COST).toString());
	}

	public void setCost(int cost) {
		wumpusProps.put(COST, cost);
	}

	public void setVisited(boolean visited) {
		wumpusProps.put(VISITED, visited);
	}

	public boolean isSafe() {
		return getCost() == 1;
	}

	public int getBreezeCounter() {
		return (int) wumpusProps.get(BREEZE_COUNTER);
	}

	public void setBreezeCounter(int value) {
		wumpusProps.put(BREEZE_COUNTER, value);
	}

	public int getLairCounter() {
		return (int) wumpusProps.get(LAIR_COUNTER);
	}

	public void setLairCounter(int value) {
		wumpusProps.put(LAIR_COUNTER, value);
	}

	public static ArrayList<Tile> where(ArrayList<Tile> tileList,
			Predicate<Tile> filter) {
		ArrayList<Tile> filteredTiles = new ArrayList<Tile>();

		for (int ii = 0; ii < tileList.size(); ii++) {
			if (filter.test(tileList.get(ii)) == true)
				filteredTiles.add(tileList.get(ii));
		}
		return filteredTiles;
	}

	public static int getMinimalCost(ArrayList<Tile> tileList) {
		int minimalCost = Integer.MAX_VALUE;

		for (Tile aTile : tileList) {
			if (aTile.getCost() < minimalCost) {
				minimalCost = aTile.getCost();
			}
		}
		return minimalCost;
	}

}