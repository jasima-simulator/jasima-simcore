/*
This file is part of jasima, the Java simulator for manufacturing and logistics.
 
Copyright 2010-2022 jasima contributors (see license.txt)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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
