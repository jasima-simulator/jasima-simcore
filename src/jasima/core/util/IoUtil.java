package jasima.core.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class IoUtil {

	private static final Charset CHARSET = StandardCharsets.UTF_8;

	public static String fileAsString(String fileName) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName), CHARSET))) {
			return reader.lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String[] lines(String fileName) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName), CHARSET))) {
			return lines(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String[] lines(BufferedReader br) throws IOException {
		return br.lines().toArray(String[]::new);
	}

}
