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
package jasima.core.run;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import jasima.core.experiment.Experiment;
import jasima.core.experiment.ExperimentListener;
import jasima.core.util.AbstractResultSaver;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.MsgCategory;
import jasima.core.util.Pair;
import jasima.core.util.TypeUtil;
import jasima.core.util.Util;

/**
 * Base class for experiment runner utility classes.
 * 
 * @see ConsoleRunner
 * @see ExcelExperimentRunner
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt
 */
public abstract class AbstractExperimentRunner {

	protected Map<Object, ExperimentListener> listeners;
	protected String experimentFileName = null;
	protected String[] packageSearchPath = Util.DEF_CLASS_SEARCH_PATH;
	protected ArrayList<Pair<String, Object>> manualProps;

	protected Experiment expToRun = null;

	public AbstractExperimentRunner() {
		super();
		listeners = new HashMap<>();
		listeners.put(ConsolePrinter.class, new ConsolePrinter(MsgCategory.INFO));
		manualProps = new ArrayList<>();
	}

	protected void configureExperiment() {
		requireNonNull(expToRun);

		String resultFileNameHint = getResultFileNameHint();

		for (ExperimentListener lstnr : listeners.values()) {
			if (resultFileNameHint != null && lstnr instanceof AbstractResultSaver) {
				((AbstractResultSaver) lstnr).setFileNameHint(resultFileNameHint);
			}

			expToRun.addListener(lstnr);
		}

		setProperties();
	}

	public @Nullable Map<String, Object> run() {
		requireNonNull(expToRun);

		return expToRun.runExperiment();
//		ExperimentCompletableFuture ef = ExperimentExecutor.runExperimentAsync(expToRun, null);
//		Map<String, Object> res = ef.joinIgnoreExceptions();
//
//		String msg = (String) res.get(Experiment.EXCEPTION_MESSAGE);
//		String exc = (String) res.get(Experiment.EXCEPTION);
//		if (msg != null || exc != null) {
//			throw new RuntimeException(msg + "; detailed error: " + exc);
//		}
//
//		return res;
	}

	private void setProperties() {
		requireNonNull(expToRun);

		// sort props by number of segments so we set parent properties first
		Collections.sort(manualProps, Comparator.comparingInt(p -> numSegments(p.a)));

		// try to set each property
		for (Pair<String, Object> p : manualProps) {
			String name = p.a;
			Object value = p.b;

			TypeUtil.setPropertyValue(expToRun, name, value, getClass().getClassLoader(), packageSearchPath);
		}
	}

	protected String getResultFileNameHint() {
		return experimentFileName;
	}

	/**
	 * Counts the number of dots '.' in a String.
	 */
	private static int numSegments(String a) {
		if (a == null || a.length() == 0)
			return 0;

		int res = 1;
		int from = -1;
		while ((from = a.indexOf('.', from + 1)) >= 0) {
			res++;
		}

		return res;
	}

}