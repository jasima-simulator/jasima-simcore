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
package jasima.core.util.converter;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jasima.core.util.converter.ArgListTokenizer.ParseException;
import jasima.core.util.converter.ArgListTokenizer.TokenType;

public class ArgListTokenizerTest {

	public static List<String> tokenize(String toParse) {
		ArrayList<String> token = new ArrayList<>();
		ArgListTokenizer t = new ArgListTokenizer();
		t.setInput(toParse);
		while (t.nextToken() != null) {
			String text = t.currTokenText();
			token.add(text);
		}
		return token;
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testEmptyAndNull() {
		assertThat(tokenize(""), is(Collections.EMPTY_LIST));

		thrown.expect(NullPointerException.class);
		tokenize(null);
	}

	@Test
	public void testWellbehaved() {
		List<String> actual = tokenize("ATCS(k1=2.2;k2=1.0 )");
		List<String> expected = asList("ATCS", "(", "k1", "=", "2.2", ";", "k2", "=", "1.0", " ", ")");
		assertThat(actual, is(expected));
	}

	@Test
	public void testSimpleLists() {
		List<String> expected = asList("   ", "abc", "=", "def", " ", ";", "123", ";", "rrr", ";");
		List<String> actual = tokenize("   abc=def ;123;rrr;");
		assertThat(actual, is(expected));
	}

	@Test
	public void testSimpleQuotedLists() {
		List<String> expected = asList("   ", "abc=def ;", "123", ";", "rrr", ";");
		List<String> actual = tokenize("   \"abc=def ;\"123;rrr;");
		assertThat(actual, is(expected));
	}

	@Test
	public void testSimple1() {
		List<String> actual = tokenize("\"\"");
		List<String> expected = asList("");
		assertThat(actual, is(expected));
	}

	@Test
	public void testSimple2() {
		List<String> actual = tokenize("\"\\\"\"");
		List<String> expected = asList("\"");
		assertThat(actual, is(expected));
	}

	@Test
	public void testQuotedUnclosed() {
		thrown.expect(ParseException.class);
		tokenize("a\"()");
	}

	@Test
	public void testEscapeAtEnd() {
		thrown.expect(ParseException.class);
		tokenize("a\\");
	}

	@Test
	public void testUnnecessaryEscapeShouldRaiseException() {
		thrown.expect(ParseException.class);
		tokenize("\\A\\B\\C \\\\XYZ");
	}

	@Test
	public void testUnnecessaryEscapeInQuotedStringShouldRaiseException() {
		thrown.expect(ParseException.class);
		tokenize("\"\\A\\B\\C \\\\XYZ\"");
	}

	@Test
	public void testEscaped() {
		List<String> expected = asList("   ", "abc=def", " ", ";123", ";", "rrr", ";");
		List<String> actual = tokenize("   abc\\=def \\;123;rrr;");
		assertThat(actual, is(expected));
	}

	@Test
	public void testEscapedSpecialChars() {
		List<String> expected = asList(" \\\r\n\t();=");
		List<String> actual = tokenize("\\ \\\\\\r\\n\\t\\(\\)\\;\\=");
		assertThat(actual, is(expected));
	}

	@Test
	public void testPushback1() {
		ArgListTokenizer lt = new ArgListTokenizer("\"\"");
		assertThat(lt.nextToken(), is(TokenType.STRING));
		assertThat(lt.currTokenText(), is(""));

		lt.pushBackToken();
		assertThat(lt.nextToken(), is(TokenType.STRING));
		assertThat(lt.currTokenText(), is(""));

		lt.pushBackToken();
		thrown.expect(IllegalStateException.class);
		lt.pushBackToken();
	}

	@Test
	public void testPushback2() {
		ArgListTokenizer lt = new ArgListTokenizer("\"\";");
		assertThat(lt.nextToken(), is(TokenType.STRING));
		assertThat(lt.currTokenText(), is(""));
		assertThat(lt.nextToken(), is(TokenType.SEMICOLON));
		assertThat(lt.currTokenText(), is(";"));

		lt.pushBackToken();
		assertThat(lt.nextToken(), is(TokenType.SEMICOLON));
		assertThat(lt.currTokenText(), is(";"));

		lt.pushBackToken();
		thrown.expect(IllegalStateException.class);
		lt.currTokenType();
	}

	@Test
	public void testPushback3() {
		ArgListTokenizer lt = new ArgListTokenizer("\"\";");

		assertThat(lt.nextToken(), is(TokenType.STRING));
		assertThat(lt.currTokenType(), is(TokenType.STRING));
		assertThat(lt.currTokenText(), is(""));

		assertThat(lt.nextToken(), is(TokenType.SEMICOLON));
		assertThat(lt.currTokenType(), is(TokenType.SEMICOLON));
		assertThat(lt.currTokenText(), is(";"));

		assertThat(lt.nextToken(), is((TokenType) null));
		assertThat(lt.currTokenType(), is((TokenType) null));
		assertThat(lt.currTokenText(), is((String) null));

		assertThat(lt.nextToken(), is((TokenType) null));
		assertThat(lt.currTokenType(), is((TokenType) null));
		assertThat(lt.currTokenText(), is((String) null));

		lt.pushBackToken();
		assertThat(lt.nextToken(), is((TokenType) null));
		assertThat(lt.currTokenType(), is((TokenType) null));
		assertThat(lt.currTokenText(), is((String) null));

		lt.pushBackToken();
		thrown.expect(IllegalStateException.class);
		lt.currTokenText();
	}

}
