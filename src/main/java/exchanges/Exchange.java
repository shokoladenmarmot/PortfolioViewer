package exchanges;

import java.util.ArrayList;
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
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import Start.Main;
import core.Utils;
import exchanges.Exchange.CoinGraph.Edge;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableNumberValue;
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

		@Override
		public String toString() {

			return "Exchange: " + exchange.exchangeName + " SYMBOL: " + symbol + " Invert: " + invert;
		}

		// TODO: hashCode
	}

	public class CoinGraph {

		public class CoinNode implements Comparable<CoinNode> {
			private Set<Edge> edges;
			private final String name;

			private CoinNode(String n) {
				name = n;
				edges = new TreeSet<Edge>();
			}

			private final boolean addEdge(Edge e) {
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

			@Override
			public String toString() {
				return "Coin: " + name;
			}

			public boolean getPathTo(final CoinNode end, Stack<Edge> result) {

				if ((result.isEmpty() == false) && result.peek().contains(end)) {
					return true;
				} else {
					Optional<Edge> myEdge = edges.parallelStream().filter(a -> (a.from == end) || (a.to == end))
							.findFirst();

					if (myEdge.isPresent()) {
						result.push(myEdge.get());
						return true;
					} else {
						Stack<Edge> bestSoFar = new Stack<Edge>();

						for (Edge e : edges) {

							// Avoid looping
							if (result.contains(e) == false) {
								result.push(e);

								if (e.getOtherEnd(this).getPathTo(end, result)) {
									if (bestSoFar.isEmpty() || (bestSoFar.size() > result.size())) {
										bestSoFar.clear();
										bestSoFar.addAll(result);
									}
								}
								result.pop();
							}
						}

						if (!bestSoFar.isEmpty()) {
							result.clear();
							result.addAll(bestSoFar);
							return true;
						}

						return false;
					}
				}

			}
		}

		public class Edge implements Comparable<Edge> {
			private final CoinNode from;
			private final CoinNode to;
			private final String symbol;

			private Edge(CoinNode f, CoinNode t, String s) {
				from = f;
				to = t;
				symbol = s;
			}

			public CoinNode getOtherEnd(CoinNode n) {
				return (n == from) ? to : from;
			}

			public boolean contains(CoinNode n) {
				return (n == from) || (n == to);
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

			@Override
			public String toString() {
				return "Edge: " + from.name + "->" + to.name + " " + symbol;
			}
		}

		private Map<String, CoinNode> coinMap;
		private Set<Edge> edgeList;

		public CoinGraph() {
			coinMap = new TreeMap<String, CoinNode>();
			edgeList = new TreeSet<Edge>();
		}

		public final boolean addCoin(String nodeName) {
			if ((nodeName == null) || nodeName.isEmpty())
				return false;

			if (coinMap.get(nodeName) == null) {
				coinMap.put(nodeName, new CoinNode(nodeName));
				return true;
			}
			return false;
		}

		public final boolean addEdge(String from, String to, String symbol) {
			if ((from == null) || (to == null) || (symbol == null) || from.isEmpty() || to.isEmpty() || symbol.isEmpty()
					|| from.equals(to))
				return false;

			CoinNode fromNode = coinMap.get(from);
			CoinNode toNode = coinMap.get(to);

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

		public final Collection<String> getAllCoinNames() {
			return coinMap.keySet();
		}

		public final Collection<String> getAllEdgeNames() {
			return edgeList.parallelStream().map(a -> a.symbol).collect(Collectors.toList());
		}

		public final Collection<String> getAllDirectCoinsFromCoin(String coinName) {
			if ((coinName == null) || coinName.isEmpty())
				return Collections.emptyList();

			if (coinMap.containsKey(coinName)) {
				CoinNode cn = coinMap.get(coinName);
				return cn.edges.parallelStream().map(a -> a.from.name.equals(coinName) ? a.to.name : a.from.name)
						.collect(Collectors.toList());
			}
			return Collections.emptyList();
		}

		public final String getPairName(String from, String to) {
			if ((from == null) || from.isEmpty() || (to == null) || to.isEmpty() || from.equals(to))
				return null;

			if (coinMap.containsKey(from) && coinMap.containsKey(to)) {
				CoinNode cn = coinMap.get(from);
				Optional<Edge> e = cn.edges.parallelStream().filter(a -> a.from.name.equals(to) || a.to.name.equals(to))
						.findFirst();

				return e.isPresent() ? e.get().symbol : null;
			}
			return null;
		}

		public Collection<Edge> getPath(final String from, final String to) {
			if (coinMap.containsKey(from) && coinMap.containsKey(to)) {
				final CoinNode start = coinMap.get(from);
				final CoinNode end = coinMap.get(to);

				Stack<Edge> result = new Stack<Edge>();
				start.getPathTo(end, result);

				return result;

			}
			return Collections.emptyList();
		}
	}

	// Current status of the service
	private ObjectProperty<Status> STATUS;

	protected String exchangeName;
	protected Image logo;
	// URL to the exchanges API
	protected String url;

	// The coin graph containing information about available coins on this market
	protected CoinGraph coinGraph;

	// Set containing all available pairs
	protected Set<String> availablePairs;

	// Map storing OHLC data
	private HashMap<String, List<Pair<Integer, ObservableList<PairData>>>> cachedOHLC;

	// Map storing current price
	private HashMap<String, SimpleDoubleProperty> cachedCurrent;

	public Exchange() {
		STATUS = new SimpleObjectProperty<Status>(Status.INIT);
	}

	protected void init() {
		LOGGER.info("Initiate exchange");

		coinGraph = new CoinGraph();
		availablePairs = new TreeSet<String>();
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
			return coinGraph.getAllCoinNames();
		}
		return Collections.emptyList();
	}

	public final Collection<String> getAvailablePairs() {

		if (getStatus() != Status.INIT) {
			return coinGraph.getAllEdgeNames();
		}
		return Collections.emptyList();
	}

	public final Collection<String> getPairsForCurrency(String currency) {
		if (getStatus() != Status.INIT) {
			return coinGraph.getAllDirectCoinsFromCoin(currency);
		}
		return Collections.emptyList();
	}

	public final String getPairName(String from, String to) {
		if (getStatus() != Status.INIT) {
			return coinGraph.getPairName(from, to);
		}
		return null;
	}

	public final ObservableList<PairData> getOHLCData(String pair, int interval) {

		if (getAvailablePairs().contains(pair)) {
			return getFromOHLCCache(pair, interval);
		}
		return FXCollections.observableArrayList();
	}

	public final ObservableList<PairData> getOHLCData(String from, String to, int interval) {
		String pair = getPairName(from, to);
		if (pair == null)
			return FXCollections.observableArrayList();

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

			Main.getInstance().threadExc.execute(() -> {
				updateOLHC(pair, interval);
			});

			return newData.getValue();
		}
	}

	private final SimpleDoubleProperty getCurrentData(final String symbol) {

		if (getAvailablePairs().contains(symbol)) {
			return getFromCurrentCache(symbol);
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

				// Schedule an update every 10 seconds
				Main.getInstance().threadExc.scheduleWithFixedDelay(() -> {
					updateCurrent(pair);
				}, 0, 10, TimeUnit.SECONDS);
				return p;
			}
		}
	}

	public ObservableNumberValue getValue(final String from, final String to) {

		ObservableNumberValue result = null;

		List<RequestPath> path = getPath(from, to);

		for (RequestPath p : path) {
			SimpleDoubleProperty sdp = p.exchange.getCurrentData(p.symbol);
			if (result == null) {
				result = p.invert ? sdp.divide(sdp.multiply(sdp)) : sdp;
			} else {
				result = Bindings.multiply(result, p.invert ? sdp.divide(sdp.multiply(sdp)) : sdp);
			}
		}

		// TODO Implement global search if result is NULL
		return (result != null) ? result : new SimpleDoubleProperty(Utils.LOADING_VALUE);
	}

	private final List<RequestPath> getPath(final String from, final String to) {
		// Local search
		Collection<Edge> pathFromGraph = coinGraph.getPath(from, to);

		if (pathFromGraph.isEmpty())
			return Collections.emptyList();

		List<RequestPath> result = new ArrayList<RequestPath>();

		String currentCurrency = from;
		for (Edge e : pathFromGraph) {
			result.add(new RequestPath(this, e.symbol, isBase(e.symbol, currentCurrency)));
			currentCurrency = currentCurrency.equals(e.from.name) ? e.to.name : e.from.name;
		}
		return result;
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
