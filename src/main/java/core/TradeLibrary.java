package core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

public class TradeLibrary {

	private static final Logger LOGGER = Logger.getLogger(TradeLibrary.class.getName());

	private static TradeLibrary inst;
	
	// Note: It is probably better to use a concurrent queue
	private Collection<Order> orders;

	public static TradeLibrary getInstance() {
		if (inst == null) {
			synchronized (TradeLibrary.class) {
				inst = new TradeLibrary();
			}
		}
		return inst;
	}

	private TradeLibrary() {
		orders = new LinkedList<Order>();
	}

	public Collection<Order> getOrders() {
		synchronized (TradeLibrary.class) {
			return new LinkedList<Order>(orders);
		}
	}

	public boolean addOrder(Order o) {
		synchronized (TradeLibrary.class) {
			orders.add(o);
		}
		return true;
	}

	public boolean addOrders(Collection<Order> o) {
		synchronized (TradeLibrary.class) {
			orders.addAll(o);
		}
		return true;
	}

	public void clearLibrary() {
		synchronized (TradeLibrary.class) {
			orders.clear();
		}
	}
}
