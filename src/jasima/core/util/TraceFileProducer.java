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
package jasima.core.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import jasima.core.simulation.SimComponent;
import jasima.core.simulation.SimComponentLifeCycleListener;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimPrintMessage;

/**
 * Produces a detailed log of all trace messages of a {@link Simulation} in a
 * text file. Creating this file is rather slow, so this class is mainly useful
 * for debugging purposes.
 * 
 * @author Torsten Hildebrandt
 */
public class TraceFileProducer implements SimComponentLifeCycleListener {

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
	public void init(SimComponent c) {
		SimComponentLifeCycleListener.super.init(c);

		install(c.getSim());
	}

	public void install(Simulation sim) {
		if (log != null)
			return; // already registered

		createLogFile();

		sim.addPrintListener(this::print);
		sim.setPrintLevel(MsgCategory.TRACE);

		// get notified when simulation finished
		sim.getRootComponent().addListener(this);
	}

	protected void print(SimPrintMessage msg) {
		if (msg.getCategory() == MsgCategory.TRACE) {
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
	public void produceResults(SimComponent c, Map<String, Object> resultMap) {
		SimComponentLifeCycleListener.super.produceResults(c, resultMap);

		if (log != null) {
			log.close();
			log = null;
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
