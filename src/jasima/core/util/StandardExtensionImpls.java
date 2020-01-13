package jasima.core.util;

import jasima.core.JasimaExtension;
import jasima.core.util.i18n.I18n;

public final class StandardExtensionImpls implements JasimaExtension {
	
	public StandardExtensionImpls() {
		System.out.println("registering standard extensions");
		I18n.registerResourceBundle("jasima.core.util.i18n.Messages");
	}

}
