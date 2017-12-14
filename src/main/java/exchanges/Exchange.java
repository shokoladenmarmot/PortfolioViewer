package exchanges;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javafx.scene.image.Image;

public abstract class Exchange {

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

	protected AtomicLong lastUpdate;
	protected ConcurrentHashMap<String, List<PairData>> lastData;
	protected ConcurrentHashMap<String, List<PairData>> cached;
	protected Set<String> defaultPairList;
	protected Set<String> availablePairList;
	protected String exchangeName;
	protected Image logo;
	protected String url;

	public Exchange() {
		init();
	}

	protected void init() {
		lastUpdate = new AtomicLong();
		lastData = new ConcurrentHashMap<String, List<PairData>>();
		cached = new ConcurrentHashMap<String, List<PairData>>();
		defaultPairList = new HashSet<String>();
		availablePairList = new HashSet<String>();
	}

	public final Set<String> getAvailablePairList() {
		return availablePairList;
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

	abstract protected void updateLastTime();

	abstract protected void populateListOfPairs();
}
