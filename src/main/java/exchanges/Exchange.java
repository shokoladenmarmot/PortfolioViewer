package exchanges;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import Start.Main;
import core.Utils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.util.Pair;

public abstract class Exchange {

	public enum Status {
		READY, BUSY, INIT;
	}

	private static final Logger LOGGER = Logger.getLogger(Exchange.class.getName());

	public class PairData {

		private final Date date;
		private final Double value;

		public PairData(Date d, Double v) {
			date = d;
			value = v;
		}

		public Date getDate() {
			return date;
		}

		public Double getValue() {
			return value;
		}
	}

	public class RequestPath {
		public final Exchange exchange;
		public final String symbol;
		public final boolean invert;

		private RequestPath(Exchange e, String s, boolean i) {
			exchange = e;
			symbol = s;
			invert = i;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if ((o instanceof RequestPath) == false)
				return false;

			return ((RequestPath) o).exchange.equals(exchange) && ((RequestPath) o).symbol.equals(symbol)
					&& (((RequestPath) o).invert == invert);
		}

		// TODO: hashCode
	}

	public class CoinGraph {

		public class CoinNode implements Comparable<CoinNode> {
			public Set<Edge> edges;
			public final String name;

			public CoinNode(String n) {
				name = n;
				edges = new TreeSet<Edge>();
			}

			public boolean addEdge(Edge e) {
				return edges.add(e);
			}

			@Override
			public int compareTo(CoinNode o) {
				return name.compareTo(o.name);
			}

			@Override
			public int hashCode() {
				return name.hashCode();
			}

			@Override
			public boolean equals(Object o) {
				if (o == this)
					return true;
				if ((o instanceof CoinNode) == false)
					return false;

				return ((CoinNode) o).name.equals(name);
			}

		}

		public class Edge implements Comparable<Edge> {
			public final CoinNode from;
			public final CoinNode to;

			public final String symbol;

			public Edge(CoinNode f, CoinNode t, String s) {
				from = f;
				to = t;
				symbol = s;
			}

			@Override
			public boolean equals(Object o) {
				if (o == this)
					return true;
				if ((o instanceof Edge) == false)
					return false;

				return ((Edge) o).from.equals(from) && ((Edge) o).to.equals(to) && ((Edge) o).symbol.equals(symbol);
			}

			@Override
			public int hashCode() {
				return symbol.hashCode();
			}

			@Override
			public int compareTo(Edge o) {
				return symbol.compareTo(o.symbol);
			}
		}

		public Map<String, CoinNode> coinList;
		public Set<Edge> edgeList;

		public CoinGraph() {
			coinList = new TreeMap<String, CoinNode>();
			edgeList = new TreeSet<Edge>();
		}

		public boolean addCoin(String nodeName) {
			if (coinList.get(nodeName) == null) {
				coinList.put(nodeName, new CoinNode(nodeName));
				return true;
			}
			return false;
		}

		public boolean addEdge(String from, String to, String symbol) {

			CoinNode fromNode = coinList.get(from);
			CoinNode toNode = coinList.get(to);

			if ((fromNode == null) || (toNode == null)) {
				return false;
			}

			Edge newEdge = new Edge(fromNode, toNode, symbol);

			if (edgeList.contains(newEdge)) {
				return false;
			}

			fromNode.addEdge(newEdge);
			toNode.addEdge(newEdge);

			return edgeList.add(newEdge);
		}

		public Collection<String> getAllCoinNames() {
			return coinList.keySet();
		}

		public Collection<String> getAllEdgeNames() {
			return edgeList.parallelStream().map(a -> a.symbol).collect(Collectors.toList());
		}

		public Collection<String> getAllDirectCoinsFromCoin(String coinName) {
			if (coinList.containsKey(coinName)) {
				CoinNode cn = coinList.get(coinName);
				return cn.edges.parallelStream().map(a -> a.from.name.equals(coinName) ? a.to.name : a.from.name)
						.collect(Collectors.toList());
			}
			return Collections.emptyList();
		}

		public String getPairName(String from, String to) {
			if (coinList.containsKey(from) && coinList.containsKey(to)) {
				CoinNode cn = coinList.get(from);
				Optional<Edge> e = cn.edges.parallelStream().filter(a -> a.from.name.equals(to) || a.to.name.equals(to))
						.findFirst();

				return e.isPresent() ? e.get().symbol : null;
			}
			return null;
		}
	}

	// Current status of the service
	private ObjectProperty<Status> STATUS;

	protected String exchangeName;
	protected Image logo;
	// URL to the exchanges API
	protected String url;

	protected CoinGraph coinGraph;

	// Coin name -> {[Coin name, Pair]}
	protected Map<String, List<Pair<String, String>>> coinMap;

	// Set containing all available pairs
	protected Set<String> availablePairs;

	// Map storing OHLC data
	private HashMap<String, List<Pair<Integer, ObservableList<PairData>>>> cachedOHLC;

	// Map storing current price
	private HashMap<String, SimpleDoubleProperty> cachedCurrent;

	// Map to store scheduled calls for current data updates
	private HashMap<String, ScheduledFuture<?>> futureCurrentData;

	// Map to store scheduled calls for updates
	private Set<Pair<Integer, String>> futureOHLC;

	public Exchange() {
		STATUS = new SimpleObjectProperty<Status>(Status.INIT);
	}

	protected void init() {
		LOGGER.info("Initiate exchange");

		coinGraph = new CoinGraph();
		coinMap = new HashMap<String, List<Pair<String, String>>>();
		// NOTE: Change to HashSet for performance
		availablePairs = new TreeSet<String>();
		cachedOHLC = new HashMap<String, List<Pair<Integer, ObservableList<PairData>>>>();
		cachedCurrent = new HashMap<String, SimpleDoubleProperty>();
		futureCurrentData = new HashMap<String, ScheduledFuture<?>>();
		futureOHLC = new HashSet<Pair<Integer, String>>();
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

	public final Collection<String> getAvailablePairs() {

		if (getStatus() != Status.INIT) {
			if (availablePairs.isEmpty()) {
				for (List<Pair<String, String>> l : coinMap.values()) {
					for (Pair<String, String> pair : l) {
						availablePairs.add(pair.getValue());
					}
				}
			}
			return availablePairs;
		}
		return Collections.emptyList();
	}

	public final List<Pair<String, String>> getPairsForCurrency(String currency) {
		if (getStatus() != Status.INIT) {
			List<Pair<String, String>> result = coinMap.get(currency);
			return (result != null) ? result : Collections.emptyList();
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

	public final ObservableList<PairData> getOHLCData(String pair, int interval) {

		if (getAvailablePairs().contains(pair)) {
			ObservableList<PairData> result = getFromOHLCCache(pair, interval);
			if (result.isEmpty()) {

				synchronized (futureOHLC) {
					Pair<Integer, String> p = new Pair<Integer, String>(interval, pair);

					if (futureOHLC.contains(p) == false) {
						Main.getInstance().threadExc.execute(() -> {
							updateOLHC(pair, interval);
						});

						futureOHLC.add(p);
					}
				}
			}
			return result;
		}
		return FXCollections.observableArrayList();
	}

	public final ObservableList<PairData> getOHLCData(String from, String to, int interval) {
		String pair = getPairName(from, to);
		return getOHLCData(pair, interval);
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

	public final SimpleDoubleProperty getCurrentData(String symbol) {
		if (getAvailablePairs().contains(symbol)) {
			SimpleDoubleProperty result = getFromCurrentCache(symbol);
			if (result.get() == Utils.LOADING_VALUE) {

				synchronized (futureCurrentData) {
					if (futureCurrentData.containsKey(symbol) == false) {
						ScheduledFuture<?> future = Main.getInstance().threadExc.scheduleWithFixedDelay(() -> {
							updateCurrent(symbol);
						}, 0, 10, TimeUnit.SECONDS);

						futureCurrentData.put(symbol, future);
					}
				}
			}
			return result;
		}
		return new SimpleDoubleProperty(Utils.LOADING_VALUE);
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
				SimpleDoubleProperty p = new SimpleDoubleProperty(Utils.LOADING_VALUE);
				cachedCurrent.put(pair, p);
				return p;
			}
		}
	}

	public final ObjectProperty<Status> getStatuProperty() {
		return STATUS;
	}

	public final void invokeWhenStatusIsReady(final Callable<Void> v) {
		synchronized (this) {
			if (STATUS.get() == Status.READY) {
				try {
					v.call();
				} catch (Exception e) {
					e.printStackTrace();
					LOGGER.info(e.getMessage());
				}
			} else {
				STATUS.addListener(new ChangeListener<Status>() {

					@Override
					public void changed(ObservableValue<? extends Status> observable, Status oldValue,
							Status newValue) {

						if (newValue == Status.READY) {
							try {
								v.call();
								STATUS.removeListener(this);
							} catch (Exception e) {
								e.printStackTrace();
								LOGGER.info(e.getMessage());
							}
						}
					}
				});
			}
		}
	}

	public final Status getStatus() {

		synchronized (this) {
			return STATUS.get();
		}
	}

	public final void setStatus(Status s) {
		synchronized (this) {
			STATUS.set(s);
		}
	}

	abstract public void initiate();

	abstract protected void updateOLHC(String pair, int interval);

	abstract protected void updateCurrent(String symbol);

	abstract public boolean isBase(String symbol, String from);
}
