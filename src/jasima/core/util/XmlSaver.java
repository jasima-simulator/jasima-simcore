package jasima.core.util;

import jasima.core.experiment.Experiment;

import java.io.File;
import java.util.Map;

/**
 * Saves (final) experiment results in an XML file.
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt, 2013-01-08
 * @version "$Id$"
 */
public class XmlSaver extends AbstractResultSaver {

	private static final long serialVersionUID = -7598515231019675606L;

	@Override
	protected void finished(Experiment e, Map<String, Object> results) {
		XmlUtil.saveXML(results, new File(getActualResultBaseName() + ".xml"));
	}

	@Override
	public boolean checkBaseName(String base) {
		if (new File(base + ".xml").exists())
			return false;
		return true;
	}

}
