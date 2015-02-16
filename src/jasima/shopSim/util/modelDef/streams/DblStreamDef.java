/*******************************************************************************
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This file is part of jasima, v1.2.
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
package jasima.shopSim.util.modelDef.streams;

import jasima.core.random.continuous.DblStream;
import jasima.core.util.Util;
import jasima.shopSim.util.modelDef.PropertySupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public abstract class DblStreamDef extends PropertySupport implements Cloneable {

	private static final long serialVersionUID = -3013662965159027666L;

	public interface StreamDefFact {

		public String getTypeString();

		public DblStreamDef stringToStreamDef(String params, List<String> errors);

		public DblStreamDef streamToStreamDef(DblStream stream);

	}

	public DblStreamDef() {
		super();
	}

	public abstract DblStream createStream();

	public static DblStreamDef parseDblStream(String s, List<String> errors) {
		StringTokenizer sst = new StringTokenizer(s, "()", false);
		ArrayList<String> ss = new ArrayList<String>();
		while (sst.hasMoreTokens()) {
			ss.add(sst.nextToken().trim());
		}
		if (ss.size() != 2) {
			errors.add("invalid stream configuration '" + s + "'");
			return null;
		}

		String type = ss.get(0);
		String parms = ss.get(1);

		StreamDefFact fact = streamFactoryReg.get(type);
		if (fact == null) {
			errors.add(String.format(
					Util.DEF_LOCALE,
					"Invalid stream type '%s'. Supported types are: '%s'.",
					type,
					streamFactoryReg.keySet().toString()
							.replaceAll("[\\[\\]]", "")));
			return null;
		}

		DblStreamDef res = fact.stringToStreamDef(parms, errors);
		return res;
	}

	public static DblStreamDef createStreamDefFromStream(DblStream stream) {
		for (StreamDefFact fact : streamFactoryReg.values()) {
			DblStreamDef sd = fact.streamToStreamDef(stream);
			if (sd != null)
				return sd;
		}
		return null;
	}

	@Override
	public DblStreamDef clone() throws CloneNotSupportedException {
		return (DblStreamDef) super.clone();
	}

	private static HashMap<String, StreamDefFact> streamFactoryReg;

	public static void registerStreamFactory(StreamDefFact fact) {
		streamFactoryReg.put(fact.getTypeString(), fact);
	}

	static {
		streamFactoryReg = new HashMap<String, StreamDefFact>();

		@SuppressWarnings("unused")
		Class<?> c;

		// trigger class load, so sub-classes can register themselves
		c = DblConstDef.class;
		c = DblExponentialDef.class;
		c = DblUniformDef.class;
		c = DblTriangularDef.class;
		c = IntUniformDef.class;
		c = IntEmpDef.class;
		c = IntConstDef.class;
	}

}
