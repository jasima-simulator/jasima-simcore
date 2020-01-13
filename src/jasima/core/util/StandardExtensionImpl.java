package jasima.core.util;

import jasima.core.JasimaExtension;
import jasima.core.util.i18n.I18n;

public final class StandardExtensionImpl implements JasimaExtension {

	public StandardExtensionImpl() {
		I18n.registerResourceBundle("jasima.core.util.i18n.Messages");
	}

}
