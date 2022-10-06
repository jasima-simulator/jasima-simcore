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
 * @author Torsten Hildebrandt
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
						new GZIPInputStream(new BufferedInputStream(new FileInputStream(expected + ".gz")))));
			} catch (FileNotFoundException e) {
				in2 = new BufferedReader(new FileReader(expected));
			}

			int line = 0;

			String s1 = in1.readLine();
			String s2 = in2.readLine();

			while (s1 != null && s2 != null) {
				assertEquals("line " + ++line, s2.trim(), s1.trim());

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
