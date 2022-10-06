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
package jasima.shopSim.prioRules.gp;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;

/**
 * Abstract base class that can be used for rules generated with Genetic
 * Programming. In addition to standard {@code PR} it only defines static
 * methods for some commonly used arithmetic functions.
 * 
 * @author Torsten Hildebrandt
 */
@SuppressWarnings("serial")
public abstract class GPRuleBase extends PR {

	@Override
	public abstract double calcPrio(PrioRuleTarget j);

	public static final double ifte(final double cond, final double ifVal, final double elseVal) {
		if (cond >= 0.0d)
			return ifVal;
		else
			return elseVal;
	}

	public static final double add(final double v1, final double v2) {
		return v1 + v2;
	}

	public static final double mul(final double v1, final double v2) {
		return v1 * v2;
	}

	public static final double div(final double v1, final double v2) {
		return v1 / v2;
	}

	public static final double divProtected(final double v1, final double v2) {
		if (v2 == 0.0)
			return 1.0;
		else
			return v1 / v2;
	}

	public static final double sub(final double v1, final double v2) {
		return v1 - v2;
	}

	public static final double max(final double v1, final double v2) {
		return Math.max(v1, v2);
	}

	public static final double min(final double v1, final double v2) {
		return Math.min(v1, v2);
	}

	public static final double neg(final double v1) {
		return -v1;
	}

	public static final double abs(double v1) {
		return Math.abs(v1);
	}

}
