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
import jasima.shopSim.util.TextFileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Experiment which loads a scheduling instance from a file or URL/URI. Shop and
 * job data has to be described in this file.
 *
 * @version $Id$
 */
public class StaticShopExperiment extends JobShopExperiment {

	private static Map<URI, TextFileReader> readerCache = new WeakHashMap<URI, TextFileReader>();

	private String instFileName;
	private URI instURI;
	private boolean useInstanceCache = false;

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

	public void configureShopFromFile(JobShop js) {
		URI uri = getInstURI();

		if (getInstFileName() != null)
			uri = new File(getInstFileName()).toURI();

		if (uri == null)
			throw new IllegalArgumentException(
					"Either 'instFileName' or 'instURI' have to be specified.");

		configureShopFromFile(uri, js, isUseInstanceCache());
	}

	//
	//
	// static utility methods
	//
	//

	public static void configureShopFromFile(URI res, JobShop js,
			boolean useInstanceCache) {
		TextFileReader r = getReader(res, useInstanceCache);
		r.configureMdl(js);
	}

	static TextFileReader getReader(URI uri, boolean useInstanceCache) {
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

	public void setInstFileName(String instFileName) {
		this.instFileName = instFileName;
	}

	public URI getInstURI() {
		return instURI;
	}

	public void setInstURI(URI instURI) {
		this.instURI = instURI;
	}

	public boolean isUseInstanceCache() {
		return useInstanceCache;
	}

	public void setUseInstanceCache(boolean useInstanceCache) {
		this.useInstanceCache = useInstanceCache;
	}

}
