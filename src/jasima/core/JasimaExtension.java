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
