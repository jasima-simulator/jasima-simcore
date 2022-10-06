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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jasima.core.util.converter.ArgListParser.ParseTree;
import jasima.core.util.converter.ArgListTokenizer.ParseException;

public class ArgListParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void parseClassAndPropDefTest() {
		String input = "a.b.c.ATC(prop1=abc;prop2=1.23;prop3=Test(abc=xyz;def=123))";
		ParseTree parseRes = ArgListParser.parseClassAndPropDef(input);

		assertThat(parseRes.getClassOrXmlName(), is("a.b.c.ATC"));

		Map<String, ParseTree> map1 = parseRes.getParams();
		assertThat(map1.size(), is(3));

		ParseTree p1 = map1.get("prop1");
		assertThat(p1.getClassOrXmlName(), is("abc"));
		assertNull(p1.getParams());

		ParseTree p2 = map1.get("prop2");
		assertThat(p2.getClassOrXmlName(), is("1.23"));
		assertNull(p2.getParams());

		ParseTree p3 = map1.get("prop3");
		assertThat(p3.getClassOrXmlName(), is("Test"));

		Map<String, ParseTree> map2 = p3.getParams();
		assertThat(map2.size(), is(2));

		ParseTree s1 = map2.get("abc");
		assertThat(s1.getClassOrXmlName(), is("xyz"));
		assertNull(s1.getParams());

		ParseTree s2 = map2.get("def");
		assertThat(s2.getClassOrXmlName(), is("123"));
		assertNull(s2.getParams());
	}

	@Test
	public void parseClassAndPropDefTest2() {
		String input = "a.b.c.ATC(prop1=abc;";
		thrown.expect(ParseException.class);
		ArgListParser.parseClassAndPropDef(input);
		assert false;
	}

	@Test
	public void parseClassAndPropDefTest3() {
		String input = "a.b.c.ATC;def";
		thrown.expect(ParseException.class);
		ArgListParser.parseClassAndPropDef(input);
		assert false;
	}
}
