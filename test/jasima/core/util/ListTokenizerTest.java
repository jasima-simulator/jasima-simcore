package jasima.core.util;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import jasima.core.util.ListTokenizer.ParseException;
import jasima.core.util.ListTokenizer.TokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ListTokenizerTest {

	public static List<String> tokenize(String toParse) {
		ArrayList<String> token = new ArrayList<>();
		ListTokenizer t = new ListTokenizer();
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
		ListTokenizer lt = new ListTokenizer("\"\"");
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
		ListTokenizer lt = new ListTokenizer("\"\";");
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
		ListTokenizer lt = new ListTokenizer("\"\";");

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

	@Test
	public void parseClassAndPropDefTest() {
		String input = "a.b.c.ATC(prop1=abc;prop2=1.23;prop3=Test(abc=xyz;def=123))";
		Pair<String, Map<String, Object>> parseRes = ListTokenizer
				.parseClassAndPropDef(input);

		assertThat(parseRes.a, is("a.b.c.ATC"));

		Map<String, Object> map1 = parseRes.b;

		assertThat((String) map1.get("prop1"), is("abc"));
		assertThat((String) map1.get("prop2"), is("1.23"));

		@SuppressWarnings("unchecked")
		Pair<String, Map<String, Object>> sub = (Pair<String, Map<String, Object>>) map1
				.get("prop3");
		assertThat(sub.a, is("Test"));

		Map<String, Object> map2 = sub.b;

		assertThat((String) map2.get("abc"), is("xyz"));
		assertThat((String) map2.get("def"), is("123"));
	}

	@Test
	public void parseClassAndPropDefTest2() {
		String input = "a.b.c.ATC(prop1=abc;";
		thrown.expect(ParseException.class);
		ListTokenizer.parseClassAndPropDef(input);
		assert false;
	}

	@Test
	public void parseClassAndPropDefTest3() {
		String input = "a.b.c.ATC;def";
		thrown.expect(ParseException.class);
		ListTokenizer.parseClassAndPropDef(input);
		assert false;
	}
}
