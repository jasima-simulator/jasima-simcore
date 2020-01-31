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
