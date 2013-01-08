package jasima.core.util;

import jasima.core.experiment.ExperimentListenerBase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base class for result saver.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public abstract class AbstractResultSaver extends ExperimentListenerBase {

	private static final long serialVersionUID = -1643003626220225101L;
	private String resultFileName = null;
	private String resultFileNameHint = "runResults";
	private String actualResultFileName = null;

	public void setFileNameHint(String resultFileNameHint) {
		this.resultFileNameHint = resultFileNameHint;
	}

	public void setResultFileName(String resultFileName) {
		resultFileName = resultFileName.trim();
		this.resultFileName = resultFileName.isEmpty() ? null : resultFileName;
	}

	public static String findFreeFile(final String baseName,
			final String extension) {
		int index = 0;
		String retVal = baseName;
		while (new File(retVal + extension).exists()) {
			retVal = baseName + "_" + ++index + extension;
		}
		return retVal;
	}

	public String findFreeFile(final String baseName) {
		String retVal = baseName;
		int index = 0;
		while (!checkBaseName(retVal)) {
			retVal = baseName + "_" + ++index;
		}
		return retVal;
	}

	public abstract boolean checkBaseName(String base);

	public String getActualResultBaseName() {
		if (actualResultFileName == null) {
			if (resultFileName == null) {
				actualResultFileName = findFreeFile(resultFileNameHint
						.replaceFirst("\\..*?$", "")
						+ new SimpleDateFormat("_yyyyMMdd_HHmm")
								.format(new Date()));
			} else {
				actualResultFileName = findFreeFile(resultFileName
						.replaceFirst("\\..*?$", ""));
			}
		}
		return actualResultFileName;
	}
}
