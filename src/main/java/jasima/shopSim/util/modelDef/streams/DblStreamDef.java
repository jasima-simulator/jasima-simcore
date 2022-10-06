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
package jasima.shopSim.util.modelDef.streams;

import static jasima.core.util.i18n.I18n.defFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import jasima.core.random.continuous.DblSequence;
import jasima.shopSim.util.modelDef.PropertySupport;

public abstract class DblStreamDef extends PropertySupport {

	private static final long serialVersionUID = -3013662965159027666L;

	public interface StreamDefFact {

		public String getTypeString();

		public DblStreamDef stringToStreamDef(String params, List<String> errors);

		public DblStreamDef streamToStreamDef(DblSequence stream);

	}

	public DblStreamDef() {
		super();
	}

	public abstract DblSequence createStream();

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
			errors.add(defFormat("Invalid stream type '%s'. Supported types are: '%s'.", type,
					streamFactoryReg.keySet().toString().replaceAll("[\\[\\]]", "")));
			return null;
		}

		DblStreamDef res = fact.stringToStreamDef(parms, errors);
		return res;
	}

	public static DblStreamDef createStreamDefFromStream(DblSequence stream) {
		for (StreamDefFact fact : streamFactoryReg.values()) {
			DblStreamDef sd = fact.streamToStreamDef(stream);
			if (sd != null)
				return sd;
		}
		return null;
	}

	@Override
	public DblStreamDef clone() {
		return (DblStreamDef) super.clone();
	}

	private static HashMap<String, StreamDefFact> streamFactoryReg;

	public static void registerStreamFactory(StreamDefFact fact) {
		streamFactoryReg.put(fact.getTypeString(), fact);
	}

	static {
		streamFactoryReg = new HashMap<String, StreamDefFact>();

		// trigger class load, so sub-classes can register themselves
		new DblConstDef();
		new DblExponentialDef();
		new DblUniformDef();
		new DblTriangularDef();
		new IntUniformDef();
		new IntEmpDef();
		new IntConstDef();
	}

}
