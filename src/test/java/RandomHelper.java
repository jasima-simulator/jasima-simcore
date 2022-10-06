
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