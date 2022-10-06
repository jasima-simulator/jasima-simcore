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
package jasima.shopSim.core;

/**
 * A job source which generates jobs from a static set of job descriptions (
 * {@link JobSpec}).
 * 
 * @author Torsten Hildebrandt
 */
public class StaticJobSource extends JobSource {

	public static class JobSpec {
		public final int routeNum;
		public final double releaseDate;
		public final double dueDate;
		public final double weight;
		public final String name;

		public JobSpec(int routeNum, double releaseDate, double dueDate, double weight) {
			this(routeNum, releaseDate, dueDate, weight, null);
		}

		public JobSpec(int routeNum, double releaseDate, double dueDate, double weight, String name) {
			super();
			this.routeNum = routeNum;
			this.releaseDate = releaseDate;
			this.dueDate = dueDate;
			this.weight = weight;
			this.name = name;
		}
	}

	public int nextJob;
	public JobSpec[] jobs;

	@Override
	public void init() {
		nextJob = 0;

		super.init();
	}

	@Override
	public Job createNextJob() {
		if (nextJob >= jobs.length)
			return null;

		Job j = newJobInstance();

		JobSpec js = jobs[nextJob];
		j.setRelDate(js.releaseDate);
		j.setDueDate(js.dueDate);
		j.setWeight(js.weight);
		j.setJobType(js.routeNum);
		j.setOps(getShop().routes[js.routeNum].ops());
		j.setRoute(getShop().routes[js.routeNum]);
		j.setName(js.name);

		nextJob++;
		return j;
	}

}
