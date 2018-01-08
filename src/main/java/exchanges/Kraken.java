package exchanges;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import core.JSONFactory;
import exchanges.Exchange.ExchangeGraph;

public class Kraken extends Exchange {

	private static final Logger LOGGER = Logger.getLogger(Kraken.class.getName());

	private HashMap<String, String> symbolAltname = null;

	@Override
	protected void init() {
		super.init();

		exchangeName = "Kraken";
		logo = null;
		url = "https://api.kraken.com/0/public/";
	}

	@Override
	public void initiate() {

		synchronized (Kraken.class) {
			if (getStatus() == Status.INIT) {
				init();

				LOGGER.info("Start: Populate list of pairs");

				// Get all currencies
				JSONObject result = JSONFactory.getJSONObject(url + "Assets");
				if (result == null)
					return;

				Set<String> symbols = result.getJSONObject("result").keySet();

				symbolAltname = new HashMap<String, String>();
				for (String symb : symbols) {

					String altName = result.getJSONObject("result").getJSONObject(symb).getString("altname");
					altName = convertToHumanReadableSymbol(altName);
					symbolAltname.put(symb, altName);

					coinGraph.addCoin(altName);
				}

				// Get all tradable pairs
				result = JSONFactory.getJSONObject(url + "AssetPairs");
				if (result == null)
					return;

				Set<String> pairSet = result.getJSONObject("result").keySet();

				for (String pair : pairSet) {

					// Don't take dark pools
					if (pair.endsWith(".d"))
						continue;

					String quote = symbolAltname
							.get(result.getJSONObject("result").getJSONObject(pair).getString("quote"));
					String base = symbolAltname
							.get(result.getJSONObject("result").getJSONObject(pair).getString("base"));

					coinGraph.addEdge(quote, base, pair, this);
				}
				
				ExchangeGraph.getInstance().addExchange(this);
				setStatus(Status.READY);
				LOGGER.info("Finish: Populate list of pairs");
			}
		}
	}

	private String convertToHumanReadableSymbol(String symbol) {

		switch (symbol) {
		case "XBT":
			return "BTC";
		case "XDG":
			return "DOGE";
		case "XVN":
			return "VEN";
		default:
			return symbol;
		}
	}

	@Override
	public void updateOLHC(String pair, int interval) {

		LOGGER.info("Update " + pair + ":" + interval);

		// Default interval 1 day
		JSONObject result = JSONFactory.getJSONObject(url + "OHLC?pair=" + pair + "&interval=" + interval); // &since=1501545600

		if (result == null)
			return;
		// KRAKEN: array of array entries(<time>, <open>, <high>, <low>, <close>,
		// <vwap>, <volume>, <count>)

		JSONArray asArray = result.getJSONObject("result").getJSONArray(pair);
		if (asArray.length() > 0) {

			List<PairData> dataList = new LinkedList<PairData>();

			for (int i = 0; i < asArray.length(); ++i) {
				JSONArray content = asArray.getJSONArray(i);
				// Time is in seconds so we need to convert to millisec
				dataList.add(
						new PairData(new Date(content.getLong(0) * 1000), Double.parseDouble(content.getString(4))));
			}
			addToOHLCCache(pair, interval, dataList);
		}
	}

	@Override
	protected void updateCurrent(String symbol) {
		LOGGER.info("Update symbol:" + symbol);

		JSONObject result = JSONFactory.getJSONObject(url + "Ticker?pair=" + symbol);

		if (result == null)
			return;

		// Last price for last trade
		addToCurrentCache(symbol, Double
				.parseDouble(result.getJSONObject("result").getJSONObject(symbol).getJSONArray("c").getString(0)));
	}

	@Override
	public boolean isBase(String symbol, String from) {
		for (Entry<String, String> entry : symbolAltname.entrySet()) {
			if (entry.getValue().equals(from)) {
				return symbol.startsWith(entry.getKey());
			}
		}
		return false;
	}
}
