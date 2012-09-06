package util;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class FileChecker {
	private FileChecker() {
	}

	public static void checkFiles(String file1, String file2) {
		try {
			BufferedReader in1 = new BufferedReader(new FileReader(file1));
			BufferedReader in2 = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(new BufferedInputStream(
							new FileInputStream(file2 + ".gz")))));
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
