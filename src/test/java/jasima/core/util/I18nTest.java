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

import java.util.Locale;

import org.junit.Test;

import jasima.core.util.i18n.I18n;

public class I18nTest {

	static {
		I18n.requireResourceBundle("jasima.core.util.Test");
	}

	@Test
	public void testTranslation() {
		Locale.setDefault(Locale.GERMANY);

		assertEquals("jasima default language is " + I18n.DEF_LOCALE, "DefLanguageCheck", I18n.message("TestKey"));
		assertEquals("other language1", "French version", I18n.message(Locale.FRANCE, "TestKey"));
		assertEquals("other language2", "German version", I18n.message(Locale.GERMANY, "TestKey"));
		assertEquals("unknown1", "Default message", I18n.message(Locale.CHINA, "TestKey"));
		assertEquals("unknown2", "Default message", I18n.message(Locale.ENGLISH, "TestKey"));
	}

}
