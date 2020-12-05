package jasima.core.util;

import jasima.core.JasimaExtension;
import jasima.core.util.converter.TypeConverterBasicTypes;
import jasima.core.util.converter.TypeConverterDateTime;
import jasima.core.util.converter.TypeConverterJavaBean;
import jasima.core.util.converter.TypeToStringConverter;
import jasima.core.util.i18n.I18n;

public final class StandardExtensionImpl extends JasimaExtension {

	public static final String JASIMA_CORE_RES_BUNDLE = "jasima.core.util.i18n.Messages";

	public StandardExtensionImpl() {
		I18n.registerResourceBundle(JASIMA_CORE_RES_BUNDLE);

		TypeToStringConverter.registerConverter(new TypeConverterBasicTypes());
		TypeToStringConverter.registerConverter(new TypeConverterJavaBean());
		TypeToStringConverter.registerConverter(new TypeConverterDateTime());
	}

}
