package exchanges;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import core.JSONFactory;

public class Poloniex extends Exchange {

	private static final Logger LOGGER = Logger.getLogger(Poloniex.class.getName());

	@Override
	protected void init() {
		super.init();
		exchangeName = "Poloniex";
		logo = null;
		url = "https://poloniex.com/public/?command=";
	}

	@Override
	public void initiate() {

		synchronized (Poloniex.class) {
			if (getStatus() == Status.INIT) {
				init();

				LOGGER.info("Start: Populate list of pairs");

				// NOTE: Get all currencies "returnCurrencies"
				// Get all currencies
				JSONObject result = JSONFactory.getJSONObject(url + "returnTicker");

				if (result == null)
					return;

				for (String pair : result.keySet()) {

					int underIndex = pair.indexOf('_');
					String base = pair.substring(0, underIndex);
					String quote = pair.substring(underIndex + 1);

					coinGraph.addCoin(base);
					coinGraph.addCoin(quote);

					coinGraph.addEdge(base, quote, pair);
				}

				setStatus(Status.READY);
				LOGGER.info("Finish: Populate list of pairs");
			}
		}
	}

	private int getIntervalFromInt(int interval) {
		return interval * 60;
	}

	@Override
	protected void updateOLHC(String pair, int interval) {
		// https://poloniex.com/public?command=returnChartData&currencyPair=BTC_XMR&start=1405699200&end=9999999999&period=14400

		JSONArray result = JSONFactory.getJSONArray(
				url + "returnChartData&currencyPair=" + pair + "&period=" + getIntervalFromInt(interval) + "&start=0");

		if (result == null)
			return;

		if (result.length() > 0) {
			List<PairData> dataList = new LinkedList<PairData>();

			for (int i = 0; i < result.length(); ++i) {
				JSONObject content = result.getJSONObject(i);

				long time = content.getLong("date");
				Double val = content.getDouble("close");

				// Time is in seconds so we need to convert to millisec
				dataList.add(new PairData(new Date(time * 1000), val));
			}
			addToOHLCCache(pair, interval, dataList);
		}

	}

	@Override
	protected void updateCurrent(String symbol) {

		JSONObject result = JSONFactory.getJSONObject(url + "returnTicker");
		if (result == null)
			return;

		result = result.getJSONObject(symbol);
		if (result == null)
			return;

		double last = Double.parseDouble(result.getString("last"));

		addToCurrentCache(symbol, last);
	}

	@Override
	public boolean isBase(String symbol, String from) {
		return symbol.endsWith("_" + from);
	}

}
