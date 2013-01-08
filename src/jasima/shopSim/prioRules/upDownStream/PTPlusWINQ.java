/*******************************************************************************
 * Copyright (c) 2010-2013 Torsten Hildebrandt and jasima contributors
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
 *
 * $Id$
 *******************************************************************************/
package jasima.shopSim.prioRules.upDownStream;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the PT+WINQ rule, developed by Holthaus and Rajendran
 * (1997), which is an additive combination of SPT and WINQ.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version $Id$
 */
public class PTPlusWINQ extends PR {

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return -(j.getCurrentOperation().procTime + WINQ.winq(j));
	}

}
