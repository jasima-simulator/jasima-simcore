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
package jasima.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilTest {

	@Test
	public void repeatTest() {
		assertEquals("abab", StringUtil.repeat("ab", 2));
	}

	@Test(expected = NullPointerException.class)
	public void repeatTestNull() {
		StringUtil.repeat(null, 2);
	}

	@Test
	public void repeatTestSmallN() {
		assertEquals("", StringUtil.repeat("ab", 0));
		assertEquals("", StringUtil.repeat("ab", -1));
	}

	@Test
	public void repeatTestEmptyString() {
		assertEquals("", StringUtil.repeat("", 13));
	}

	@Test
	public void isNullOrEmpty__null__true() {
		assertEquals(true, StringUtil.isNullOrEmpty(null));
	}

	@Test
	public void isNullOrEmpty__emptyString__true() {
		assertEquals(true, StringUtil.isNullOrEmpty(""));
	}

	@Test
	public void isNullOrEmpty__nonEmptyString__false() {
		assertEquals(false, StringUtil.isNullOrEmpty(" "));
	}

	@Test
	public void replaceLineBreaks__shouldWorkForCr() {
		assertEquals(" \\n ", StringUtil.replaceLineBreaks("\r"));
	}

	@Test
	public void replaceLineBreaks__shouldWorkForLf() {
		assertEquals(" \\n ", StringUtil.replaceLineBreaks("\n"));
	}

	@Test
	public void replaceLineBreaks__shouldWorkForCrLf() {
		assertEquals(" \\n ", StringUtil.replaceLineBreaks("\r\n"));
	}

	@Test
	public void replaceLineBreaks__shouldWorkForThreeDifferentLines_emptyLines() {
		assertEquals(" \\n  \\n  \\n ", StringUtil.replaceLineBreaks("\n\r\r\n"));
	}

	@Test
	public void replaceLineBreaks__shouldWorkForThreeDifferentLines_withContents() {
		assertEquals("a \\n b \\n c \\n d", StringUtil.replaceLineBreaks("a\nb\rc\r\nd"));
	}
	
}
