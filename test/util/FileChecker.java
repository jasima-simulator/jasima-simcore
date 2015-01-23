/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
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

	public static void checkFiles(String file1, String file2) {
		try {
			BufferedReader in1 = new BufferedReader(new FileReader(file1));
			BufferedReader in2;
			try {
				in2 = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(new BufferedInputStream(
								new FileInputStream(file2 + ".gz")))));
			} catch (FileNotFoundException e) {
				in2 = new BufferedReader(new FileReader(file2));
			}

			int line = 0;

			String s1 = in1.readLine();
			String s2 = in2.readLine();

			while (s1 != null && s2 != null) {
				assertEquals("line " + ++line, s2, s1);

				s1 = in1.readLine();
				s2 = in2.readLine();
			}

			assertEquals("file length differ.", s2, s1); // should both be null

			in1.close();
			in2.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
