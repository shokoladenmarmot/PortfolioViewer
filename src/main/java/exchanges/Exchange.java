package exchanges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import exchanges.Exchange.CoinGraph.CoinNode;
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

	public static class RequestPath {
		public final Edge edge;
		public final boolean invert;

		private RequestPath(Edge e, boolean i) {
			edge = e;
			invert = i;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if ((o instanceof RequestPath) == false)
				return false;

			return ((RequestPath) o).edge.equals(edge) && (((RequestPath) o).invert == invert);
		}

		@Override
		public String toString() {

			return "Exchange: " + edge.exchange.exchangeName + " SYMBOL: " + edge.symbol + " Invert: " + invert;
		}

		// TODO: hashCode
	}

	protected static class ExchangeGraph {
		private static ExchangeGraph instance;

		private Map<String, List<Edge>> edgeListPerCoinMap;

		protected static ExchangeGraph getInstance() {
			if (instance == null) {
				synchronized (ExchangeGraph.class) {
					if (instance == null) {
						instance = new ExchangeGraph();
					}
				}
			}
			return instance;
		}

		private ExchangeGraph() {
			edgeListPerCoinMap = new HashMap<String, List<Edge>>();
		}

		protected boolean addExchange(Exchange e) {

			synchronized (ExchangeGraph.class) {

				for (CoinNode cn : e.coinGraph.coinMap.values()) {
					List<Edge> edgeList = edgeListPerCoinMap.get(cn.name);

					if (edgeList == null) {
						edgeList = new LinkedList<Edge>();
						edgeListPerCoinMap.put(cn.name, edgeList);
					}
					edgeList.addAll(cn.edges);
				}

				return false;
			}
		}

		private final List<RequestPath> getPath(final CoinNode from, final String to) {
			synchronized (ExchangeGraph.class) {

				if ((edgeListPerCoinMap.containsKey(to) == false) || from.name.equals(to))
					return Collections.emptyList();

				// Get the path in nodes
				Stack<CoinNode> resultCoinStack = new Stack<CoinNode>();

				if (getPathTo(from, to, resultCoinStack) == false)
					return Collections.emptyList();

				// Convert nodes to edges ( as minimum market flips as possible )
				Stack<Edge> resultStack = new Stack<Edge>();
				Iterator<CoinNode> itr = resultCoinStack.iterator();

				CoinNode last = itr.next();
				while (itr.hasNext()) {
					CoinNode first = last;
					CoinNode second = itr.next();

					Exchange exch = first.edges.stream().findFirst().get().exchange;

					Edge edge = exch.coinGraph.getEdge(first.name, second.name);
					if (edge != null) {
						resultStack.push(edge);
					} else {
						exch = second.edges.stream().findFirst().get().exchange;
						edge = exch.coinGraph.getEdge(first.name, second.name);

						if (edge != null) {
							resultStack.push(edge);
						} else {
							// Go through the rest of the exchanges and find a pair
							resultStack.push(
									edgeListPerCoinMap.values().stream().flatMap(Collection::stream).findFirst().get());
						}
					}
					last = second;
				}

				String currentCurrency = from.name;
				List<RequestPath> result = new ArrayList<RequestPath>();

				for (Edge e : resultStack) {
					result.add(new RequestPath(e, e.exchange.isBase(e.symbol, currentCurrency)));
					currentCurrency = currentCurrency.equals(e.from.name) ? e.to.name : e.from.name;
				}

				return result;
			}
		}

		private final boolean getPathTo(final CoinNode current, final String to, Stack<CoinNode> result) {

			result.push(current);

			Set<Edge> allEdges = new HashSet<Edge>();

			// Push personal set fist
			allEdges.addAll(current.edges);
			allEdges.addAll(edgeListPerCoinMap.get(current.name));

			Optional<Edge> myEdge = allEdges.stream().filter(a -> a.contains(to)).findFirst();

			if (myEdge.isPresent()) {
				result.push(myEdge.get().getOtherEnd(current.name));
				return true;
			} else {
				Stack<CoinNode> bestSoFar = new Stack<CoinNode>();

				for (Edge e : allEdges) {

					// Avoid inner loops
					if (result.stream().anyMatch(a -> a.name.equals(e.getOtherEnd(current.name).name)) == false) {

						if (getPathTo(e.getOtherEnd(current.name), to, result)) {
							if (bestSoFar.isEmpty() || (bestSoFar.size() > result.size())) {
								bestSoFar.clear();
								bestSoFar.addAll(result);
							}
							result.pop();
						}
					}
				}

				if (!bestSoFar.isEmpty()) {
					result.clear();
					result.addAll(bestSoFar);
					return true;
				}

				result.pop();
				return false;
			}
		}

		private final List<RequestPath> getPathV1(final CoinNode from, final String to) {
			synchronized (ExchangeGraph.class) {

				if ((edgeListPerCoinMap.containsKey(to) == false) || from.name.equals(to))
					return Collections.emptyList();

				Stack<Edge> resultStack = new Stack<Edge>();

				if (getPathToV1(from, to, resultStack) == false)
					return Collections.emptyList();

				List<RequestPath> result = new ArrayList<RequestPath>();

				String currentCurrency = from.name;
				for (Edge e : resultStack) {
					result.add(new RequestPath(e, e.exchange.isBase(e.symbol, currentCurrency)));
					currentCurrency = currentCurrency.equals(e.from.name) ? e.to.name : e.from.name;
				}

				return result;
			}
		}

		private final boolean getPathToV1(final CoinNode current, final String to, Stack<Edge> result) {
			if ((result.isEmpty() == false) && result.peek().contains(to)) {
				return true;
			} else {

				Set<Edge> allEdges = new HashSet<Edge>();

				// Push personal set fist
				allEdges.addAll(current.edges);
				allEdges.addAll(edgeListPerCoinMap.get(current.name));

				Optional<Edge> myEdge = allEdges.stream().filter(a -> a.contains(to)).findFirst();

				if (myEdge.isPresent()) {
					result.push(myEdge.get());
					return true;
				} else {
					Stack<Edge> bestSoFar = new Stack<Edge>();

					for (Edge e : allEdges) {

						// Avoid inner loops
						if (result.stream().anyMatch(a -> a.contains(e.getOtherEnd(current).name)) == false) {
							result.push(e);

							if (getPathToV1(e.getOtherEnd(current), to, result)) {
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

				return ((CoinNode) o).name.equals(name) && ((CoinNode) o).edges.equals(edges);
			}

			@Override
			public String toString() {
				return "Coin: " + name;
			}

			public boolean getPathTo(final CoinNode end, Stack<Edge> result) {

				if ((result.isEmpty() == false) && result.peek().contains(end)) {
					return true;
				} else {
					Optional<Edge> myEdge = edges.parallelStream().filter(a -> a.contains(end)).findFirst();

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
			private final Exchange exchange;

			private Edge(CoinNode f, CoinNode t, String s, Exchange e) {
				from = f;
				to = t;
				symbol = s;
				exchange = e;
			}

			public Exchange getExchange() {
				return exchange;
			}

			public CoinNode getOtherEnd(CoinNode n) {
				return (n == from) ? to : from;
			}

			public CoinNode getOtherEnd(String n) {
				return (n.equals(from.name)) ? to : from;
			}

			public boolean contains(CoinNode n) {
				return (n == from) || (n == to);
			}

			public boolean contains(String n) {
				return (from.name.equals(n)) || (to.name.equals(n));
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

		public final boolean addEdge(String from, String to, String symbol, Exchange e) {
			if ((from == null) || (to == null) || (symbol == null) || from.isEmpty() || to.isEmpty() || symbol.isEmpty()
					|| from.equals(to) || (e == null))
				return false;

			CoinNode fromNode = coinMap.get(from);
			CoinNode toNode = coinMap.get(to);

			if ((fromNode == null) || (toNode == null)) {
				return false;
			}

			Edge newEdge = new Edge(fromNode, toNode, symbol, e);

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
			return edgeList.parallelStream().map(a -> a.symbol).collect(Collectors.toSet());
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

		private final Edge getEdge(final String n1, final String n2) {
			for (Edge e : edgeList) {
				if (e.contains(n1) && e.contains(n2)) {
					return e;
				}
			}
			return null;
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

		public List<RequestPath> getPath(final String from, final String to) {
			if ((coinMap.containsKey(from) == false) || (coinMap.containsKey(to) == false))
				return Collections.emptyList();

			final CoinNode start = coinMap.get(from);
			final CoinNode end = coinMap.get(to);

			Stack<Edge> resultStack = new Stack<Edge>();

			if (start.getPathTo(end, resultStack) == false)
				return Collections.emptyList();

			List<RequestPath> result = new ArrayList<RequestPath>();

			String currentCurrency = from;
			for (Edge e : resultStack) {
				result.add(new RequestPath(e, e.exchange.isBase(e.symbol, currentCurrency)));
				currentCurrency = currentCurrency.equals(e.from.name) ? e.to.name : e.from.name;
			}
			return result;
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

		// Local search
		List<RequestPath> path = coinGraph.getPath(from, to);

		if (path.isEmpty() && coinGraph.coinMap.containsKey(from)) {
			// Global search
			// NOTE: V1 ( search through edges ) seems to be significantly faster.
			path = ExchangeGraph.getInstance().getPathV1(coinGraph.coinMap.get(from), to);
		}

		for (RequestPath p : path) {
			SimpleDoubleProperty sdp = p.edge.exchange.getCurrentData(p.edge.symbol);
			if (result == null) {
				result = p.invert ? sdp.divide(sdp.multiply(sdp)) : sdp;
			} else {
				result = Bindings.multiply(result, p.invert ? sdp.divide(sdp.multiply(sdp)) : sdp);
			}
		}

		return (result != null) ? result : new SimpleDoubleProperty(Utils.LOADING_VALUE);
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
