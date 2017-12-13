package exchanges;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.image.Image;

public abstract class Exchange {

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

	protected ConcurrentHashMap<String, List<PairData>> lastData;
	protected Set<String> defaultPairList;
	protected Set<String> availablePairList;
	protected String exchangeName;
	protected Image logo;
	protected String url;

	public Exchange() {
		init();
	}

	protected void init() {
		lastData = new ConcurrentHashMap<String, List<PairData>>();
		defaultPairList = new HashSet<String>();
		availablePairList = new HashSet<String>();
	}

	public String getName() {
		return exchangeName;
	}

	public Image getLogo() {
		return logo;
	}

	public String getAPIURL() {
		return url;
	}

	public ConcurrentHashMap<String, List<PairData>> getDate() {
		return new ConcurrentHashMap<String, List<PairData>>(lastData);
	}

	public void updateOLHC(String pair) {
		synchronized (this.getClass()) {
			defaultPairList.add(pair);
		}
	}

	public void update() {
		Set<String> copyOfdefaults = new HashSet<String>(defaultPairList);

		copyOfdefaults.parallelStream().forEach(s -> {
			updateOLHC(s);
		});
	}

	abstract protected void populateListOfPairs();
}
