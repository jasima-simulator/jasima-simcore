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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import jasima.core.util.ArgListParser.ParseTree;
import jasima.core.util.ArgListTokenizer.ParseException;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ArgListParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void parseClassAndPropDefTest() {
		String input = "a.b.c.ATC(prop1=abc;prop2=1.23;prop3=Test(abc=xyz;def=123))";
		ParseTree parseRes = ArgListParser
				.parseClassAndPropDef(input);

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
