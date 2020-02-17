package examples.processes.gameOfLife;

import static jasima.core.simulation.SimContext.currentSimulation;

import java.util.ArrayList;
import java.util.List;

public class Cell {

	private boolean isAlive = false;
	private int aliveNeigbours = 0;

	private List<Cell> neighbours = new ArrayList<>();

	public Cell(boolean initialState) {
		super();
		isAlive = initialState;
		currentSimulation().scheduleIn(0.0, 0, this::checkStateChange);
	}

	public int countAliveNeighbours() {
		int n = 0;
		for (Cell c : neighbours) {
			if (c.isAlive()) {
				n++;
			}
		}
		return n;
	}

	public boolean isAlive() {
		return false;
	}

	public void checkStateChange() {
		boolean oldState = isAlive;
		if (aliveNeigbours < 2 || aliveNeigbours > 3) {
			isAlive = false;
		} else if (aliveNeigbours == 3) {
			isAlive = true;
		} else if (aliveNeigbours == 2) {
			// leave current state unchanged
		} else {
			throw new AssertionError("Can't occur.");
		}

		if (oldState != isAlive) {
			for (Cell c : neighbours) {
				c.neighbourChangedState(isAlive);
			}
		}
	}

	private void neighbourChangedState(boolean isNeighbourAlive) {
		if (isNeighbourAlive) {
			aliveNeigbours++;
		} else {
			aliveNeigbours--;
		}
		assert aliveNeigbours >= 0 && aliveNeigbours <= 8 : aliveNeigbours;
		currentSimulation().scheduleIn(1.0, 0, this::checkStateChange);
	}

	public void addNeighbour(Cell cell) {
		System.out.println(this+"  n> "+cell);
		if (cell == this || neighbours.contains(cell)) {
			throw new IllegalArgumentException();
		}
		neighbours.add(cell);
	}

}
