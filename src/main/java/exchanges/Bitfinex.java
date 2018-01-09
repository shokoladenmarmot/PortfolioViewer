package exchanges;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;

import core.JSONFactory;

public class Bitfinex extends Exchange {

	private static final Logger LOGGER = Logger.getLogger(Bitfinex.class.getName());
	private static final String urlV1 = new String("https://api.bitfinex.com/v1/");

	@Override
	protected void init() {
		super.init();
		exchangeName = "Bitfinex";
		logo = null;
		url = "https://api.bitfinex.com/v2/";
	}

	@Override
	public void initiate() {

		synchronized (Bitfinex.class) {
			if (getStatus() == Status.INIT) {
				init();

				LOGGER.info("Start: Populate list of pairs");

				// Get all pairs
				JSONArray result = JSONFactory.getJSONArray(urlV1 + "symbols");

				if (result == null)
					return;

				for (int i = 0; i < result.length(); i++) {
					String symbol = result.getString(i);
					String base = symbol.substring(0, 3).toUpperCase();
					String quote = symbol.substring(3, symbol.length()).toUpperCase();

					coinGraph.addCoin(base);
					coinGraph.addCoin(quote);

					// NOTE: This API is case sensitive. It works with UPPER CASE SYMBOLS ONLY!
					coinGraph.addEdge(base, quote, symbol.toUpperCase(), this);
				}

				ExchangeGraph.getInstance().addExchange(this);
				setStatus(Status.READY);
				LOGGER.info("Finish: Populate list of pairs");
			}
		}

	}

	private final String getIntervalFromInt(int i) {

		if (i == 1) {
			return "1m";
		}
		if (i == 5) {
			return "5m";
		}
		if (i == 15) {
			return "15m";
		}
		if (i == 30) {
			return "30m";
		}
		if (i == 60) {
			return "1h";
		}
		if (i == 180) {
			return "3h";
		}
		if (i == 360) {
			return "6h";
		}
		if (i == 720) {
			return "12h";
		}
		if (i == 1440) {
			return "1D";
		}
		if (i == 10080) {
			return "7D";
		}
		if (i == 20160) {
			return "14D";
		}
		if (i == 43800) {
			return "1M";
		}

		return "1d";
	}

	@Override
	protected boolean updateOLHC(String pair, int interval) {

		// Note: The default limit is 100. Range seems to be from 1-1000. Use 500
		// Note: Start and end time also available
		// Note: By default it returns an array from NEW to OLD. "sort=1" will return
		// the array OLD to NEW

		JSONArray result = JSONFactory.getJSONArray(
				url + "candles/trade:" + getIntervalFromInt(interval) + ":t" + pair + "/hist?limit=" + 500 + "&sort=1");

		if (result == null)
			return false;

		if (result.length() > 0) {
			List<PairData> dataList = new LinkedList<PairData>();

			for (int i = 0; i < result.length(); ++i) {
				try {
					JSONArray content = result.getJSONArray(i);

					long time = content.getLong(0);
					Double val = content.getDouble(2);

					dataList.add(new PairData(new Date(time), val));
				} catch (JSONException e) {
					e.printStackTrace();
					LOGGER.info(e.getMessage());
					return false;
				}
			}
			addToOHLCCache(pair, interval, dataList);
			return true;
		}
		return false;
	}

	@Override
	protected void updateCurrent(String symbol) {

		JSONArray result = JSONFactory.getJSONArray(url + "ticker/t" + symbol);
		if (result == null)
			return;

		if (result.length() > 6) {
			double last = result.getDouble(6);

			addToCurrentCache(symbol, last);
		} else {
			LOGGER.info("Invalid: Request: " + url + "ticker/t" + symbol);
		}
	}

	@Override
	public boolean isBase(String symbol, String from) {
		return symbol.startsWith(from);
	}
}
