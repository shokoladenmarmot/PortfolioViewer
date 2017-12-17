package exchanges;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.scene.image.Image;
import javafx.util.Pair;

public abstract class Exchange {

	public enum Status {
		READY, BUSY, INIT;
	}

	private static final Logger LOGGER = Logger.getLogger(Exchange.class.getName());

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

	private HashMap<String, List<Pair<Integer, Collection<PairData>>>> cachedData;

	public Exchange() {
		init();
	}

	protected void init() {
		STATUS = Status.INIT;

		lastUpdate = new AtomicLong();
		coinMap = new HashMap<String, List<Pair<String, String>>>();
		cachedData = new HashMap<String, List<Pair<Integer, Collection<PairData>>>>();
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

	public final Collection<PairData> getData(String from, String to, int interval) {

		String pair = getPairName(from, to);
		if (pair != null) {
			Collection<PairData> result = getFromCache(pair, interval);
			if (result.isEmpty()) {
				updateOLHC(pair, interval);
			}
			return getFromCache(pair, interval);
		}
		return Collections.emptyList();
	}

	protected final void addToCache(String pair, int interval, Collection<PairData> data) {
		synchronized (cachedData) {

			Pair<Integer, Collection<PairData>> newData = new Pair<Integer, Collection<PairData>>(interval, data);
			List<Pair<Integer, Collection<PairData>>> currentPairRecord = cachedData.get(pair);

			if (currentPairRecord == null) {
				currentPairRecord = new LinkedList<Pair<Integer, Collection<PairData>>>();
			} else {
				currentPairRecord = currentPairRecord.stream().filter(p -> p.getKey() != interval)
						.collect(Collectors.toList());
			}
			currentPairRecord.add(newData);
			cachedData.put(pair, currentPairRecord);
		}
	}

	private final Collection<PairData> getFromCache(String pair, int interval) {
		synchronized (cachedData) {
			if (cachedData.containsKey(pair)) {
				for (Pair<Integer, Collection<PairData>> data : cachedData.get(pair)) {
					if (data.getKey() == interval) {
						return new LinkedList<PairData>(data.getValue());
					}
				}
			}
			return Collections.emptyList();
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

	abstract protected void updateLastTime();
}
