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
