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

import java.text.DateFormat;
import java.util.TimeZone;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.Simulation;

@Plugin(name = "jasima", category = StrLookup.CATEGORY)
public class Log4j2SimLookup implements StrLookup {

	public static final String SIM_NAME = "simName";
	public static final String SIM_TIME = "simTime";
	public static final String SIM_TIME_ABS = "simTimeAbs";

	public String lookup(String key) {
		Simulation s = SimContext.currentSimulation();
		if (s == null) {
			return "no_sim";
		}
		if (SIM_NAME.equals(key)) {
			return s.toString();
		} else if (key.startsWith(SIM_TIME_ABS)) {
			return formatSimTimeAbs(key, s);
		} else if (key.equals(SIM_TIME)) {
			return formatSimTime(key, s);
		} else {
			return "UNKNOWN_KEY:" + key;
		}
	}

	private String formatSimTime(String key, Simulation s) {
		return Double.toString(s.simTime());
	}

	private String formatSimTimeAbs(String key, Simulation s) {
		FastDateFormat formatter;
		if (key.length() == SIM_TIME_ABS.length()) {
			formatter = FastDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
					TimeZone.getTimeZone(s.getZoneId()), s.getLocale());
		} else {
			String params = key.substring(SIM_TIME_ABS.length());
			assert params.startsWith(":");
			formatter = FastDateFormat.getInstance(params.substring(1), TimeZone.getTimeZone(s.getZoneId()),
					s.getLocale());
		}
		return formatter.format(s.simTimeAbs().toEpochMilli());
	}

	public String lookup(LogEvent event, String key) {
		assert event.getThreadName() == Thread.currentThread().getName();
		return lookup(key);
	}

}