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
package jasima.core.simulation.generic;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.ArrayDeque;
import java.util.Deque;

import jasima.core.simulation.SimContext;
import jasima.core.simulation.SimProcess;
import jasima.core.simulation.SimProcess.MightBlock;
import jasima.core.util.observer.ObservableValue;

public class Channel<MSG> {

	private Deque<MSG> msgs;
	private ObservableValue<Boolean> hasMsgs;
	private ConditionQueue waitQueue;

	public Channel() {
		super();
		msgs = new ArrayDeque<>();
		hasMsgs = new ObservableValue<>(FALSE);
		waitQueue = new ConditionQueue(hasMsgs);
	}

	public void send(MSG msg) {
		msgs.addLast(msg);
		hasMsgs.set(TRUE);
	}

	public MSG receive() throws MightBlock {
		if (msgs.size()==0) {
			assert !hasMsgs.get();
			// make current process wait until there is a message
			SimProcess<?> currentProcess = SimContext.currentProcess();
			waitQueue.executeWhenTrue(() -> {
				currentProcess.resume();
			});
			currentProcess.suspend();
		} else {
			assert waitQueue.numActions()==0;
		}
		
		// take the first message
		MSG msg = msgs.removeFirst();
		hasMsgs.set(msgs.size() > 0);

		return msg;
	}

}
