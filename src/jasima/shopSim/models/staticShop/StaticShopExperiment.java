/*******************************************************************************
 * Copyright 2011, 2012 Torsten Hildebrandt and BIBA - Bremer Institut für Produktion und Logistik GmbH
 *
 * This file is part of jasima, v1.0.
 *
 * jasima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jasima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jasima.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.shopSim.models.staticShop;

import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.JobShopExperiment;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.Route;
import jasima.shopSim.core.StaticJobSource;
import jasima.shopSim.core.StaticJobSource.JobSpec;
import jasima.shopSim.util.TextFileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Experiment which loads a scheduling instance from a file or URL/URI. Shop and
 * job data has to be described in this file.
 * 
 * @version "$Id$"
 */
public class StaticShopExperiment extends JobShopExperiment {

	private static final long serialVersionUID = 3907065922245526545L;

	private static Map<URI, TextFileReader> readerCache = new WeakHashMap<URI, TextFileReader>();

	// parameters
	private String instFileName;
	private URI instURI;
	private boolean useInstanceCache = true;
	private double dueDateTightness = Double.NaN;
	private RoundingMode roundingMode = RoundingMode.NONE;

	public static enum RoundingMode {
		NONE, FLOOR, CEIL, ROUND;
	}

	@Override
	protected void createShop() {
		super.createShop();

		configureShopFromFile(shop);
	}

	@Override
	public StaticShopExperiment clone() throws CloneNotSupportedException {
		StaticShopExperiment c = (StaticShopExperiment) super.clone();

		return c;
	}

	protected void configureShopFromFile(JobShop js) {
		URI uri = null;

		if (getInstFileName() != null) {
			File f = new File(getInstFileName());
			if (f.exists())
				uri = f.toURI();
			else {
				// try to load as a resource
				URL u = this.getClass().getResource(getInstFileName());
				if (u == null)
					u = Thread.currentThread().getContextClassLoader()
							.getResource(getInstFileName());
				if (u != null)
					try {
						uri = u.toURI();
					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
			}
		}

		if (uri == null)
			uri = getInstURI();

		if (uri == null)
			throw new IllegalArgumentException(
					"Either 'instFileName' or 'instURI' have to be specified.");

		TextFileReader r = getReader(uri, useInstanceCache);
		r.configureMdl(js);

		// overwrite JobSpec data with new due dates
		if (!Double.isNaN(getDueDateTightness())) {
			StaticJobSource src = (StaticJobSource) js.sources[0];
			JobSpec[] jobs = src.jobs;

			for (int i = 0, n = src.jobs.length; i < n; i++) {
				JobSpec orig = jobs[i];

				Route route = js.routes[orig.routeNum];
				double procSum = 0.0;
				for (Operation o : route.ops()) {
					procSum += o.procTime;
				}

				double dd = getDueDateTightness() * procSum;
				if (getRoundingMode() == RoundingMode.FLOOR) {
					dd = Math.floor(dd);
				} else if (getRoundingMode() == RoundingMode.CEIL) {
					dd = Math.ceil(dd);
				} else if (getRoundingMode() == RoundingMode.ROUND) {
					dd = Math.round(dd);
				}

				jobs[i] = new JobSpec(orig.routeNum, orig.releaseDate, dd,
						orig.weight, orig.name);
			}
		}
	}

	//
	//
	// static utility methods
	//
	//

	protected static TextFileReader getReader(URI uri, boolean useInstanceCache) {
		TextFileReader r = null;
		if (useInstanceCache)
			synchronized (readerCache) {
				r = readerCache.get(uri);
			}

		if (r == null) {
			// construct new reader
			try {
				InputStream inp = uri.toURL().openStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						inp));
				r = new TextFileReader();
				r.readData(in);
				in.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// double-check before updating readerCache to be thread-safe
			if (useInstanceCache)
				synchronized (readerCache) {
					if (readerCache.get(uri) == null)
						readerCache.put(uri, r);
				}
		}
		return r;
	}

	//
	//
	// boring getters and setters for parameters below
	//
	//

	public String getInstFileName() {
		return instFileName;
	}

	/**
	 * Sets the name of a file which is used to load the static problem
	 * instance. If no file with the given name is found, an attempt is made to
	 * load a resource with the given name.
	 * 
	 * @see #setInstURI(URI)
	 */
	public void setInstFileName(String instFileName) {
		this.instFileName = instFileName;
	}

	public URI getInstURI() {
		return instURI;
	}

	/**
	 * Sets an arbitrary URI which is used to locate the problem instance. This
	 * URI takes precedence over a file name set using
	 * {@link #setInstFileName(String)}.
	 */
	public void setInstURI(URI instURI) {
		this.instURI = instURI;
	}

	public boolean isUseInstanceCache() {
		return useInstanceCache;
	}

	public void setUseInstanceCache(boolean useInstanceCache) {
		this.useInstanceCache = useInstanceCache;
	}

	public double getDueDateTightness() {
		return dueDateTightness;
	}

	/**
	 * Overwrites/sets the due dates of all jobs, if set to a value different
	 * from NaN (Not A Number).
	 * 
	 * @see #setRoundingMode(RoundingMode)
	 */
	public void setDueDateTightness(double dueDateTightness) {
		this.dueDateTightness = dueDateTightness;
	}

	public RoundingMode getRoundingMode() {
		return roundingMode;
	}

	/**
	 * Sets the rounding mode when computing a new due date. This is only used
	 * if dueDateTightness is set to a value!=null. Rounds the result to an
	 * integer value.
	 * 
	 * @see #setDueDateTightness(double)
	 */
	public void setRoundingMode(RoundingMode roundingMode) {
		this.roundingMode = roundingMode;
	}

}
