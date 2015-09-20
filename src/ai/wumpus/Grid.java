package ai.wumpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Grid {

	// Default height, equals number of rows
	private final int DEFAULT_HEIGHT = 7;

	// Default width, equals number of columns
	private final int DEFAULT_WIDTH = 10;

	private int gridWidth;
	private int gridHeight;

	private Tile[][] tiles;

	// All visited safe tiles
	private Map<String, Tile> unvisitedSafeTiles;

	public Grid() {
		gridWidth = DEFAULT_WIDTH;
		gridHeight = DEFAULT_HEIGHT;

		tiles = new Tile[gridHeight][gridWidth];

		unvisitedSafeTiles = new HashMap<String, Tile>();
	}

	public Grid(int width, int height) {
		gridWidth = width;
		gridHeight = height;

		tiles = new Tile[gridHeight][gridWidth];

		unvisitedSafeTiles = new HashMap<String, Tile>();
	}

	public Tile getTile(int xPos, int yPos) {
		return tiles[xPos][yPos];
	}

	public void setTile(Tile aTile, int xPos, int yPos) {
		this.tiles[xPos][yPos] = aTile;
	}

	// Links tiles to each other
	public void informTilesOfTheirNeighbors() {
		for (int row = 0; row < gridHeight; row++) {
			for (int column = 0; column < gridWidth; column++) {

				Tile currentTile = tiles[row][column];
				if (!currentTile.isTunnelNE() && !currentTile.isTunnelNW()) {
					for (int directionConstants = 0; directionConstants < 4; directionConstants++) {
						Tile tmp = getNeighbor(row, column, directionConstants);
						currentTile.setNeighborTile(tmp, directionConstants);
					}
				}
			}
		}
	}

	// Get neighboring tile in certain direction
	private Tile getNeighbor(int row, int column, int directionConstant) {
		int neighborRow = row;
		int neighborColumn = column;

		Tile neighbor;
		switch (directionConstant) {
		case Tile.DIRECTIONS_NORTH:
			neighborRow = (row == 0) ? gridHeight - 1 : row - 1;
			neighbor = tiles[neighborRow][neighborColumn];
			if (neighbor.isTunnelNE())
				return getNeighbor(neighborRow, neighborColumn,
						Tile.DIRECTIONS_WEST);
			else if (neighbor.isTunnelNW())
				return getNeighbor(neighborRow, neighborColumn,
						Tile.DIRECTIONS_EAST);
			return neighbor;
		case Tile.DIRECTIONS_EAST:
			neighborColumn = (column + 1) % gridWidth;
			neighbor = tiles[neighborRow][neighborColumn];
			if (neighbor.isTunnelNE())
				return getNeighbor(neighborRow, neighborColumn,
						Tile.DIRECTIONS_SOUTH);
			else if (neighbor.isTunnelNW())
				return getNeighbor(neighborRow, neighborColumn,
						Tile.DIRECTIONS_NORTH);
			return neighbor;
		case Tile.DIRECTIONS_SOUTH:
			neighborRow = (row + 1) % gridHeight;
			neighbor = tiles[neighborRow][neighborColumn];
			if (neighbor.isTunnelNE())
				return getNeighbor(neighborRow, neighborColumn,
						Tile.DIRECTIONS_EAST);
			else if (neighbor.isTunnelNW())
				return getNeighbor(neighborRow, neighborColumn,
						Tile.DIRECTIONS_WEST);
			return neighbor;
		default:
			neighborColumn = (column == 0) ? gridWidth - 1 : column - 1;
			neighbor = tiles[neighborRow][neighborColumn];
			if (neighbor.isTunnelNE())
				return getNeighbor(neighborRow, neighborColumn,
						Tile.DIRECTIONS_NORTH);
			else if (neighbor.isTunnelNW())
				return getNeighbor(neighborRow, neighborColumn,
						Tile.DIRECTIONS_SOUTH);
			return neighbor;
		}
	}

	// Return all tiles surrounding the Wumpus
	public ArrayList<Tile> getWumpusNeighbors(Tile tile) {
		ArrayList<Tile> wumpusNeighbors = new ArrayList<Tile>();

		// Loop for all tiles within two distance in all directions
		for (int ii = 0; ii < 4; ii++) {
			Tile oneTileNeighbor = getNeighbor(tile.getX(), tile.getY(), ii);

			// If the one tile distance neighbor isn't the current tile, check
			// for its neighbors
			if (!oneTileNeighbor.equals(tile)) {
				if (!wumpusNeighbors.contains(oneTileNeighbor))
					wumpusNeighbors.add(oneTileNeighbor);
				for (int jj = 0; jj < 4; jj++) {

					// If the two tiles distance neighbor isn't already in the
					// list, add it
					Tile twoTilesNeighbor = getNeighbor(oneTileNeighbor.getX(),
							oneTileNeighbor.getY(), jj);
					if (!wumpusNeighbors.contains(twoTilesNeighbor)
							&& !twoTilesNeighbor.equals(tile)) {
						wumpusNeighbors.add(twoTilesNeighbor);
					}
				}
			}

		}
		return wumpusNeighbors;
	}

	// Returns all tiles matching the filter criteria
	public ArrayList<Tile> getAllMatchingTiles(Predicate<Tile> filter) {
		ArrayList<Tile> matchingTiles = new ArrayList<Tile>();

		for (int rows = 0; rows < gridHeight; rows++) {
			for (int columns = 0; columns < gridWidth; columns++) {
				Tile tmpTile = tiles[rows][columns];
				if (filter.test(tmpTile))
					matchingTiles.add(tmpTile);
			}
		}
		return matchingTiles;
	}

	// Getter and setter method
	public Tile[][] getGrid() {
		return tiles;
	}

	public void setGrid(Tile[][] grid) {
		this.tiles = grid;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(int gridWidth) {
		this.gridWidth = gridWidth;
	}

	public int getGridHeight() {
		return gridHeight;
	}

	public void setGridHeight(int gridHeight) {
		this.gridHeight = gridHeight;
	}

	public Map<String, Tile> getUnvisitedSafeTiles() {
		return unvisitedSafeTiles;
	}

}
