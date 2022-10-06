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

import static java.lang.System.out;

import jasima.core.simulation.SimContext;

public class Environment {

	public static void main(String... args) throws Exception {
		SimContext.simulationOf(sim -> {
			Environment e = new Environment(X_DIM, Y_DIM);

			for (int i = 0; i < 10; i++) {
				e.print();
				SimContext.waitFor(1.001);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				}
			}
			e.print();
		});

	}

	private void print() {
		out.println();
		for (int x = 0; x < xDim(); x++) {
			for (int y = 0; y < yDim(); y++) {
				out.print(cell(x, y).isAlive() ? "X" : " ");
			}
			out.println();
		}
	}

	private static final int X_DIM = 5;
	private static final int Y_DIM = 6;

	private static final int[][] INIT = { { 0, 0, 1, 0, 0 }, { 1, 0, 1, 0, 0 }, { 0, 1, 1, 0, 0 }, { 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, };

	public Environment(int numCols, int numRows) {
		cells = new Cell[numRows][numCols];

		for (int x = 0; x < numCols; x++) {
			for (int y = 0; y < numRows; y++) {
				cells[y][x] = new Cell(INIT[y][x] > 0);
			}
		}

		for (int x = 0; x < numCols; x++) {
			for (int y = 0; y < numRows; y++) {
				Cell c = cells[x][y];
				for (int x2 = x - 1; x2 <= x + 1; x2++) {
					for (int y2 = y - 1; y2 <= y + 1; y2++) {
						if (!(x2 == x && y2 == y)) {
							Cell n = cell(x2, y2);
							c.addNeighbour(n);
						}
					}
				}
			}
		}
	}

	private Cell cell(int x2, int y2) {
		System.out.print(x2+" "+y2+"   ");
		if (x2 == -1) {
			x2 = xDim() - 1;
		} else if (x2 == xDim()) {
			x2 = 0;
		}
		assert x2 >= 0 && x2 < xDim();

		if (y2 == -1) {
			y2 = yDim() - 1;
		} else if (y2 == yDim()) {
			y2 = 0;
		}
		assert y2 >= 0 && y2 < yDim();
		System.out.println(x2+" "+y2+"   ");

		return cells[y2][x2];
	}

	private int yDim() {
		return cells.length;
	}

	private int xDim() {
		return cells[0].length;
	}

	private Cell[][] cells;

}
