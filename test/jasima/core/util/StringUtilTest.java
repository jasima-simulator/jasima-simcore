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

}
