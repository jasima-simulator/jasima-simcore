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
package jasima.shopSim.util.modelDef;

import jasima.core.util.TypeUtil;

public class StaticSourceDef extends SourceDef {

	private static final long serialVersionUID = -8115921805433488275L;

	public static final String PROP_JOB_SPECS = "jobSpecs";

	private JobDef[] jobSpecs;

	public JobDef[] getJobSpecs() {
		return jobSpecs;
	}

	public void setJobSpecs(JobDef[] jobSpecs) {
		firePropertyChange(PROP_JOB_SPECS, this.jobSpecs, this.jobSpecs = jobSpecs);
	}

	@Override
	public StaticSourceDef clone() {
		StaticSourceDef c = (StaticSourceDef) super.clone();

		c.jobSpecs = TypeUtil.deepCloneArrayIfPossible(jobSpecs);

		return c;
	}

}
