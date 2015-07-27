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
package jasima.shopSim.prioRules.upDownStream;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * This class implements the PT+WINQ rule, developed by Holthaus and Rajendran
 * (1997), which is an additive combination of SPT and WINQ.
 * 
 * @author Christoph Pickardt, 2011-11-15
 * @version 
 *          "$Id$"
 */
public class PTPlusWINQ extends PR {

	private static final long serialVersionUID = 3104676770498948728L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		return -(j.getCurrentOperation().procTime + WINQ.winq(j));
	}

}
