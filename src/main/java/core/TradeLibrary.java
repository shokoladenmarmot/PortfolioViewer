package core;

import java.util.Collection;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TradeLibrary {

	private static final Logger LOGGER = Logger.getLogger(TradeLibrary.class.getName());

	private static TradeLibrary inst;

	private ObservableList<Order> orders;

	public static TradeLibrary getInstance() {
		if (inst == null) {
			synchronized (TradeLibrary.class) {
				inst = new TradeLibrary();
			}
		}
		return inst;
	}

	private TradeLibrary() {
		orders = FXCollections.observableArrayList();
	}

	public ObservableList<Order> getOrders() {
		synchronized (TradeLibrary.class) {
			return orders;
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
