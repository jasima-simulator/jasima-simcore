package jasima.core.util.observer;

import java.util.ArrayList;
import java.util.function.Predicate;

public class NotifierService {

	private ArrayList<Subscription> subscriptions;
	private ArrayList<Subscription> subsToAdd;
	private boolean iterating;

	public NotifierService() {
		super();

		subscriptions = new ArrayList<>();
		subsToAdd = new ArrayList<>();
	}

	public void publish(Object publisher, Object event) {
//		System.out.println(String.valueOf(publisher) + "\t" + String.valueOf(event));

		iterating = true;
		for (Subscription s : subscriptions) {
			if (s.eventType.isAssignableFrom(event.getClass()) && (s.evtFilter == null || s.evtFilter.test(event))
					&& (s.pubFilter == null || s.pubFilter.test(publisher)))
				s.subscriber.inform(publisher, event);
		}
		iterating = false;

		if (subsToAdd.size() > 0) {
			for (Subscription s : subsToAdd) {
				subscriptions.add(s);
			}
			subsToAdd.clear();
		}
	}

	public void addSubscription(Class<?> eventClass, Subscriber s) {
		addSubscription(eventClass, null, null, s);
	}

	public void addSubscription(Class<?> eventClass, Predicate<Object> pubFilter, Predicate<Object> evtFilter,
			Subscriber s) {
		Subscription sc = new Subscription(eventClass, pubFilter, evtFilter, s);
		if (iterating) {
			subsToAdd.add(sc);
		} else {
			subscriptions.add(sc);
		}
	}

	public void removeSubscriber(Subscriber remove) {
		assert !iterating;
		subscriptions.removeIf(s -> s.subscriber == remove);
	}

	/** Stores information about a single subscription. */
	public static class Subscription {
		public Subscription(Class<?> anEventType, Predicate<Object> aFilter, Predicate<Object> evtFilter,
				Subscriber aSubscriber) {
			eventType = anEventType;
			pubFilter = aFilter;
			this.evtFilter = evtFilter;
			subscriber = aSubscriber;
		}

		public final Class<?> eventType;
		public final Predicate<Object> evtFilter;
		public final Predicate<Object> pubFilter;
		public final Subscriber subscriber;
	}

}
