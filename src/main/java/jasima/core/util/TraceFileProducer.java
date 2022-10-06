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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import jasima.core.simulation.SimComponent;
import jasima.core.simulation.SimComponentLifecycleListener;
import jasima.core.simulation.SimPrintMessage;
import jasima.core.simulation.Simulation;

/**
 * Produces a detailed log of all trace messages of a {@link Simulation} in a
 * text file. Creating this file is rather slow, so this class is mainly useful
 * for debugging purposes.
 * <p>
 * This class can either be added as a listener to a {@link SimComponent}
 * (usually the root component) as it implements
 * {@link SimComponentLifecycleListener}. Alternatively it can be directly added
 * as a print listener of a {@link Simulation} (it therefore implements
 * {@code Consumer<SimPrintMessage>}).
 * 
 * @author Torsten Hildebrandt
 */
public class TraceFileProducer implements SimComponentLifecycleListener, Consumer<SimPrintMessage> {

	// parameters

	private String fileName;

	// used during run

	private PrintWriter log;
	private String name;

	public TraceFileProducer() {
		super();
	}

	public TraceFileProducer(String fileName) {
		this();

		setFileName(fileName);
	}

	@Override
	public void init(SimComponent sc) {
		Simulation sim = sc.getSim();
		sim.addPrintListener(this);
		sim.setPrintLevel(MsgCategory.TRACE);
	}

	@Override
	public void done(SimComponent sc) {
		if (log != null) {
			log.close();
			log = null;
		}
	}

	@Override
	public void accept(SimPrintMessage msg) {
		if (msg.getCategory() == MsgCategory.TRACE) {
			if (log == null) {
				createLogFile();
			}

			log.println(msg.getMessage());
		}
	}

	private void createLogFile() {
		try {
			name = getFileName();
			if (name == null) {
				// create some default name
				name = "jasimaTrace" + new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date());
				// don't overwrite existing
				name = AbstractResultSaver.findFreeFile(name, ".txt") + ".txt";
			}
			log = new PrintWriter(new BufferedWriter(new FileWriter(name)), true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		String n = getFileName();
		if (n == null)
			n = name;
		return getClass().getSimpleName() + "(" + n + ")";
	}

	// getter/setter for parameter below

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
