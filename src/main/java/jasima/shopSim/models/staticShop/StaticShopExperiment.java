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
package jasima.shopSim.models.staticShop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import jasima.shopSim.core.Operation;
import jasima.shopSim.core.Route;
import jasima.shopSim.core.ShopExperiment;
import jasima.shopSim.core.StaticJobSource;
import jasima.shopSim.core.StaticJobSource.JobSpec;
import jasima.shopSim.util.TextFileReader;
import jasima.shopSim.util.modelDef.ShopDef;

/**
 * Experiment which loads a scheduling instance from a file or URL/URI. Shop and
 * job data has to be described in this file.
 */
public class StaticShopExperiment extends ShopExperiment {

	private static final long serialVersionUID = 3907065922245526545L;

	// parameters
	private String instFileName;
	private URI instURI;
	private ShopDef shopDef;
	private double dueDateTightness = Double.NaN;
	private RoundingMode roundingMode = RoundingMode.NONE;

	public static enum RoundingMode {
		NONE, FLOOR, CEIL, ROUND;
	}

	@Override
	public StaticShopExperiment clone() {
		StaticShopExperiment c = (StaticShopExperiment) super.clone();

		if (shopDef!=null) {
			c.shopDef = shopDef.clone();
		}
		
		return c;
	}

	@Override
	protected void createShop() {
		super.createShop();

		if (getInstFileName() == null && getInstURI() == null && getShopDef() == null)
			throw new IllegalArgumentException("Either 'instFileName', 'instURI' or 'shopDef' have to be specified.");

		// ShopDef given explicitly?
		ShopDef def = getShopDef();
		if (def == null) {
			// if not, load with TextFileReader from URI
			def = loadWithTextFileReader();
		}

		// configure shop using ShopDef
		def.getShopConfigurator().configureSimulation(sim);
		def.getShopConfigurator().configureMdl(shop);
	}

	@Override
	protected void configureShop() {
		super.configureShop();

		// overwrite JobSpec data with new due dates
		if (!Double.isNaN(getDueDateTightness())) {
			StaticJobSource src = (StaticJobSource) shop.sources().getChild(0);
			JobSpec[] jobs = src.jobs;

			for (int i = 0, n = src.jobs.length; i < n; i++) {
				JobSpec orig = jobs[i];

				Route route = shop.routes[orig.routeNum];
				double procSum = 0.0;
				for (Operation o : route.ops()) {
					procSum += o.getProcTime();
				}

				double dd = getDueDateTightness() * procSum;
				if (getRoundingMode() == RoundingMode.FLOOR) {
					dd = Math.floor(dd);
				} else if (getRoundingMode() == RoundingMode.CEIL) {
					dd = Math.ceil(dd);
				} else if (getRoundingMode() == RoundingMode.ROUND) {
					dd = Math.round(dd);
				}

				jobs[i] = new JobSpec(orig.routeNum, orig.releaseDate, dd, orig.weight, orig.name);
			}
		}
	}

	/**
	 * Loads a ShopDef from either a file or a resource.
	 */
	protected ShopDef loadWithTextFileReader() {
		URI uri = null;

		if (getInstFileName() != null) {
			File f = new File(getInstFileName());
			if (f.exists()) {
				// ordinary file
				uri = f.toURI();
			} else {
				// try to load as a resource
				URL u = this.getClass().getResource(getInstFileName());
				if (u == null)
					u = Thread.currentThread().getContextClassLoader().getResource(getInstFileName());
				if (u != null)
					try {
						uri = u.toURI();
					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
			}
		}

		if (uri == null) {
			// URI given directly?
			uri = getInstURI();
		}

		// error getting the URI?
		if (uri == null) {
			Object src = getInstURI();
			if (src == null)
				src = getInstFileName();
			throw new IllegalArgumentException(
					"Could not load model from '" + src.toString() + "'. Perhaps file is not accessible.");
		}

		// open stream and produce a shopDef
		try {
			InputStream inp = uri.toURL().openStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inp));

			TextFileReader r = new TextFileReader();
			r.readData(in);
			in.close();

			return r.getShopDef();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

	public ShopDef getShopDef() {
		return shopDef;
	}

	/**
	 * Sets the {@link ShopDef}-object to use. Setting a {@code shopDef} takes
	 * precedence over setting a {@link #setInstFileName(String)} and
	 * {@link #setInstURI(URI)}.
	 */
	public void setShopDef(ShopDef shopDef) {
		this.shopDef = shopDef;
	}

}
