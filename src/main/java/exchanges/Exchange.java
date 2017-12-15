package exchanges;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javafx.scene.image.Image;
import javafx.util.Pair;

public abstract class Exchange {

	public enum Status {
		READY, BUSY, STOP;
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
	protected Status STATUS;
	
	// Last update time on the server
	protected AtomicLong lastUpdate;
	
	protected String exchangeName;
	protected Image logo;
	// URL to the exchanges API
	protected String url;

	protected ConcurrentHashMap<String, List<PairData>> lastData;
	protected ConcurrentHashMap<String, List<PairData>> cached;

	// Coin name -> {[Coin name, Pair]}
	protected Map<String, List<Pair<String, String>>> coinMap;

	
	public Exchange() {
		init();
	}

	protected void init() {
		STATUS = Status.STOP;

		coinMap = new HashMap<String, List<Pair<String, String>>>();
		lastUpdate = new AtomicLong();

		lastData = new ConcurrentHashMap<String, List<PairData>>();
		cached = new ConcurrentHashMap<String, List<PairData>>();

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

	public final ConcurrentHashMap<String, List<PairData>> getDate() {
		return new ConcurrentHashMap<String, List<PairData>>(cached);
	}

	public void updateOLHC(String pair, int interval) {
		synchronized (this.getClass()) {
			defaultPairList.add(pair);
		}
	}

	public final void updateOLHC(String pair) {
		updateOLHC(pair, 1440);
	}

	public final void update(int interval) {
		LOGGER.info("Update all symbols for: " + interval);

		Set<String> copyOfdefaults = new HashSet<String>(defaultPairList);

		copyOfdefaults.parallelStream().forEach(s -> {
			updateOLHC(s, interval);
		});
		updateLastTime();
	}

	public final void update() {
		update(1440);
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

	abstract protected void updateLastTime();

	abstract protected void populateListOfPairs();
}
