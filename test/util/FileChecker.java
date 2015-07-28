/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015      jasima solutions UG
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
package util;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id: FileChecker.java 73 2013-01-08 17:16:19Z THildebrandt@gmail.com
 *          $
 */
public class FileChecker {
	private FileChecker() {
	}

	public static void checkFiles(String actual, String expected) {
		try {
			BufferedReader in1 = new BufferedReader(new FileReader(actual));
			BufferedReader in2;
			try {
				in2 = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(new BufferedInputStream(
								new FileInputStream(expected + ".gz")))));
			} catch (FileNotFoundException e) {
				in2 = new BufferedReader(new FileReader(expected));
			}

			int line = 0;

			String s1 = in1.readLine();
			String s2 = in2.readLine();

			while (s1 != null && s2 != null) {
				assertEquals("line " + ++line, s1, s2);

				s1 = in1.readLine();
				s2 = in2.readLine();
			}

			assertEquals("file length differ.", s1, s2); // should both be null

			in1.close();
			in2.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
