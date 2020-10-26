
/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
import java.util.Random;

/**
 * 
 * @author Torsten Hildebrandt
 */
public class RandomHelper {

	private RandomHelper() {
	}

	public static float expon(float rmean, Random rnd) {
		float u = rnd.nextFloat();
		while (u == 0.0f)
			u = rnd.nextFloat();
		return rmean * ((float) Math.log(u)) * (-1f);
	}

	//
	// public int irandi(int nvalue, float probd[]) {
	// int randInt = nvalue;
	// float u = rand();
	// for (int i = nvalue; i > 0; i--) {
	// if (u < probd[i]) {
	// randInt = i;
	// }
	// }
	// return randInt; // This is an indication of an error
	// }

	public static float unifrm(float a, float b, Random rnd) {
		float u = rnd.nextFloat();
		return a + u * (b - a);
	}

	public static int discreteUniform(int a, int b, Random rnd) {
		return a + rnd.nextInt(b - a + 1);
	}

	public static float erlang(int m, float mean, Random rnd) {
		float mean_exponential, sum;

		mean_exponential = mean / m;
		sum = (float) 0.0;
		for (int i = 0; i < m; i++)
			sum += expon(mean_exponential, rnd);
		return sum;
	}

	public static int randomInteger(float probDistrib[], Random rnd) {
		float u = rnd.nextFloat();
		for (int i = 0; i < probDistrib.length; i++) {
			if (u <= probDistrib[i])
				return i;
		}

		throw new AssertionError("improper probs");
	}

}