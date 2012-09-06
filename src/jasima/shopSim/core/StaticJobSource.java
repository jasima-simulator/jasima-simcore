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
package jasima.shopSim.core;

public class StaticJobSource extends JobSource {

	public static class JobSpec {
		public final int routeNum;
		public final double releaseDate;
		public final double dueDate;
		public final double weight;
		public final String name;

		public JobSpec(int routeNum, double releaseDate, double dueDate,
				double weight) {
			this(routeNum, releaseDate, dueDate, weight, null);
		}

		public JobSpec(int routeNum, double releaseDate, double dueDate,
				double weight, String name) {
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
		j.setName(js.name);

		nextJob++;
		return j;
	}

}
