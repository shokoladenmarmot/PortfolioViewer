package exchanges;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import Start.Main;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.image.Image;
import javafx.util.Pair;

public abstract class Exchange {

	public enum Status {
		READY, BUSY, INIT;
	}

	private static final Logger LOGGER = Logger.getLogger(Exchange.class.getName());
	public static final Double INVALID_VALUE = new Double(-1);

	public class PairData {

		private final String date;
		private final Double value;

		public PairData(String d, Double v) {
			date = d;
			value = v;
		}

		public String getDate() {
			return date;
		}

		public Double getValue() {
			return value;
		}
	}

	// Current status of the service
	private Status STATUS;

	// Last update time on the server
	protected AtomicLong lastUpdate;

	protected String exchangeName;
	protected Image logo;
	// URL to the exchanges API
	protected String url;

	// Coin name -> {[Coin name, Pair]}
	protected Map<String, List<Pair<String, String>>> coinMap;

	// Map storing OHLC data
	private HashMap<String, List<Pair<Integer, ObservableList<PairData>>>> cachedOHLC;

	// Map storing current price
	private HashMap<String, SimpleDoubleProperty> cachedCurrent;

	public Exchange() {
		init();
	}

	protected void init() {
		STATUS = Status.INIT;

		lastUpdate = new AtomicLong();
		coinMap = new HashMap<String, List<Pair<String, String>>>();
		cachedOHLC = new HashMap<String, List<Pair<Integer, ObservableList<PairData>>>>();
		cachedCurrent = new HashMap<String, SimpleDoubleProperty>();
	}

	public final String getName() {
		return exchangeName;
	}

	public final Image getLogo() {
		return logo;
	}

	public final String getAPIURL() {
		return url;
	}

	public final Collection<String> getAvailableCurrency() {

		if (getStatus() != Status.INIT) {
			return coinMap.keySet();
		}
		return Collections.emptyList();
	}

	public final List<Pair<String, String>> getCurrencyFromCurrency(String currency) {
		if (getStatus() != Status.INIT) {
			List<Pair<String, String>> result = coinMap.get(currency);
			return (result != null) ? result : Collections.emptyList();
		}
		return Collections.emptyList();
	}

	public final List<Pair<String, String>> getPairsForCurrency(String currency) {
		if (getStatus() != Status.INIT) {
			return coinMap.get(currency);
		}
		return Collections.emptyList();
	}

	public final String getPairName(String from, String to) {
		if (getStatus() != Status.INIT) {
			if (coinMap.containsKey(from)) {
				for (Pair<String, String> pair : coinMap.get(from)) {
					if (pair.getKey().equals(to)) {
						return pair.getValue();
					}
				}
			}
		}
		return null;
	}

	public final ObservableList<PairData> getOHLCData(String from, String to, int interval) {

		String pair = getPairName(from, to);
		if (pair != null) {
			ObservableList<PairData> result = getFromOHLCCache(pair, interval);
			if (result.isEmpty()) {
				Main.getInstance().threadExc.execute(() -> {
					updateOLHC(pair, interval);
				});
			}
			return result;
		}
		return FXCollections.observableArrayList();
	}

	protected final void addToOHLCCache(String pair, int interval, Collection<PairData> data) {
		synchronized (cachedOHLC) {
			List<Pair<Integer, ObservableList<PairData>>> currentPairRecord = cachedOHLC.get(pair);

			if (currentPairRecord != null) {
				for (Pair<Integer, ObservableList<PairData>> p : currentPairRecord) {
					if (p.getKey() == interval) {
						p.getValue().setAll(data);
						return;
					}
				}
			} else {
				currentPairRecord = new LinkedList<Pair<Integer, ObservableList<PairData>>>();
				cachedOHLC.put(pair, currentPairRecord);
			}

			Pair<Integer, ObservableList<PairData>> newData = new Pair<Integer, ObservableList<PairData>>(interval,
					FXCollections.observableArrayList(data));
			currentPairRecord.add(newData);
		}
	}

	private final ObservableList<PairData> getFromOHLCCache(String pair, int interval) {
		synchronized (cachedOHLC) {
			List<Pair<Integer, ObservableList<PairData>>> currentPairRecord = cachedOHLC.get(pair);

			if (currentPairRecord != null) {
				for (Pair<Integer, ObservableList<PairData>> data : currentPairRecord) {
					if (data.getKey() == interval) {
						return data.getValue();
					}
				}
			} else {
				currentPairRecord = new LinkedList<Pair<Integer, ObservableList<PairData>>>();
				cachedOHLC.put(pair, currentPairRecord);
			}

			Pair<Integer, ObservableList<PairData>> newData = new Pair<Integer, ObservableList<PairData>>(interval,
					FXCollections.observableArrayList());
			currentPairRecord.add(newData);

			return newData.getValue();
		}
	}

	public final SimpleDoubleProperty getCurrentData(String from, String to) {

		String pair = getPairName(from, to);
		if (pair != null) {
			return getCurrentData(pair);
		}
		return new SimpleDoubleProperty(INVALID_VALUE);
	}

	public final SimpleDoubleProperty getCurrentData(String symbol) {
		SimpleDoubleProperty result = getFromCurrentCache(symbol);
		if (result.get() == INVALID_VALUE) {

			// TODO Maybe schedule ? ?
			Main.getInstance().threadExc.execute(() -> {
				updateCurrent(symbol);
			});
		}
		return result;
	}

	protected final void addToCurrentCache(String pair, Double data) {
		synchronized (cachedCurrent) {
			if (cachedCurrent.containsKey(pair)) {
				cachedCurrent.get(pair).set(data);
			} else {
				cachedCurrent.put(pair, new SimpleDoubleProperty(data));
			}
		}
	}

	private final SimpleDoubleProperty getFromCurrentCache(String pair) {
		synchronized (cachedCurrent) {
			if (cachedCurrent.containsKey(pair)) {
				return cachedCurrent.get(pair);
			} else {
				SimpleDoubleProperty p = new SimpleDoubleProperty(INVALID_VALUE);
				cachedCurrent.put(pair, p);
				return p;
			}
		}
	}

	public final long getLastUpdate() {
		return lastUpdate.get();
	}

	public final Status getStatus() {

		synchronized (this) {
			return STATUS;
		}
	}

	public final void setStatus(Status s) {
		synchronized (this) {
			STATUS = s;
		}
	}

	abstract public void initiate();

	abstract protected void updateOLHC(String pair, int interval);

	abstract protected void updateCurrent(String symbol);

	abstract protected void updateLastTime();

	abstract public boolean isBase(String symbol, String from);
}
