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
package jasima.core.util;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import jasima.core.util.ArgListTokenizer.ParseException;
import jasima.core.util.ArgListTokenizer.TokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
		List<String> expected = asList("ATCS", "(", "k1", "=", "2.2", ";",
				"k2", "=", "1.0", " ", ")");
		assertThat(actual, is(expected));
	}

	@Test
	public void testSimpleLists() {
		List<String> expected = asList("   ", "abc", "=", "def", " ", ";",
				"123", ";", "rrr", ";");
		List<String> actual = tokenize("   abc=def ;123;rrr;");
		assertThat(actual, is(expected));
	}

	@Test
	public void testSimpleQuotedLists() {
		List<String> expected = asList("   ", "abc=def ;", "123", ";", "rrr",
				";");
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
	public void testEscaped() {
		List<String> expected = asList("   ", "abc=def", " ", ";123", ";",
				"rrr", ";");
		List<String> actual = tokenize("   abc\\=def \\;123;rrr;");
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
