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

import static jasima.core.util.i18n.I18n.defFormat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jasima.core.experiment.ExperimentListener;

/**
 * Base class for result saver.
 * 
 * @author Torsten Hildebrandt
 */
public abstract class AbstractResultSaver implements ExperimentListener {

	private String resultFileName = null;
	private String resultFileNameHint = "runResults";
	private String actualResultFileName = null;

	public void setFileNameHint(String resultFileNameHint) {
		this.resultFileNameHint = resultFileNameHint;
	}

	public String getFileNameHint() {
		return this.resultFileNameHint;
	}

	public void setResultFileName(String resultFileName) {
		resultFileName = resultFileName.trim();
		this.resultFileName = resultFileName.isEmpty() ? null : resultFileName;
	}

	public static String findFreeFile(final String baseName, final String extension) {
		int index = 0;
		String retVal = baseName;

		while (true) {
			try {
				boolean createRes = new File(retVal + extension).createNewFile();
				if (createRes)
					break; // while
			} catch (IOException e) {
			}

			retVal = baseName + "_" + ++index;

			if (index > 1000) {
				// give up
				throw new RuntimeException(
						defFormat("Cant't create new file (baseName=%s,ext=%s).", baseName, extension));
			}
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
			String filename;
			if (resultFileName == null) {
				filename = removeFileExtension(resultFileNameHint)
						+ new SimpleDateFormat("_yyyyMMdd_HHmm").format(new Date());
			} else {
				filename = removeFileExtension(resultFileName);
			}
			actualResultFileName = findFreeFile(filename);
		}
		return actualResultFileName;
	}

	private static String removeFileExtension(String filename) {
		final File f = new File(filename);

		int dotPos = f.getName().lastIndexOf('.');
		if (dotPos <= 0) {
			return filename;
		}

		return new File(f.getParent(), f.getName().substring(0, dotPos)).getPath();
	}
}
