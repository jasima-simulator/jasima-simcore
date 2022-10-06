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
package jasima.core;

import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jasima.core.simulation.Simulation;

public abstract class JasimaExtension {

	private static final Logger logger = LogManager.getLogger(Simulation.class);

	public static void requireExtensionsLoaded() {
		// calling this method will ensure class is loaded which ensures
		// loadJasimaExtensions() being called exactly once
	}

	static void loadJasimaExtensions() {
		// class load will trigger registrations
		for (JasimaExtension ext : ServiceLoader.load(JasimaExtension.class)) {
			logger.debug("loaded jasima extension {}.", ext.getClass().getName());
		}
	}

	static {
		loadJasimaExtensions();
	}

}
