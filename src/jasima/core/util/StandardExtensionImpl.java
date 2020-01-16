package jasima.core.util;

import jasima.core.JasimaExtension;
import jasima.core.util.i18n.I18n;

public final class StandardExtensionImpl implements JasimaExtension {

	public static final String JASIMA_CORE_RES_BUNDLE = "jasima.core.util.i18n.Messages";

	public StandardExtensionImpl() {
		I18n.registerResourceBundle(JASIMA_CORE_RES_BUNDLE);
	}

}
