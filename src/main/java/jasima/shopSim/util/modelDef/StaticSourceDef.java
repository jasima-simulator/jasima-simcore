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
