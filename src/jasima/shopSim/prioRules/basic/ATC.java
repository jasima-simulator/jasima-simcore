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
package jasima.shopSim.prioRules.basic;

import jasima.shopSim.prioRules.setup.ATCS;

/**
 * This class implements the Apparent Tardiness Costs rule by Vepsalainen and
 * Morton (1987).
 * 
 * @author Torsten Hildebrandt
 * @version $Id$
 */
public class ATC extends ATCS {

	public ATC(double k) {
		super(k, 0.0d);
	}

	@Override
	public String getName() {
		return "ATC(k=" + getK1() + ")";
	}

}
