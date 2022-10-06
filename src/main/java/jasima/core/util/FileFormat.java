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
package jasima.core.util;

public enum FileFormat {
	/**
	 * The default XStream XML format, using a reflection based converter.
	 */
	XSTREAM,

	/**
	 * The jasima XML format, using a converter based on getter and setter
	 * methods.
	 */
	JASIMA_BEAN,

	/**
	 * A bean based converter that does not use references.
	 */
	RESULTS_MAP,

	/**
	 * A json format based on getter and setter methods.
	 */
	JSON,
}
