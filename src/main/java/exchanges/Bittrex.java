package exchanges;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import core.JSONFactory;

public class Bittrex extends Exchange {

	private static final Logger LOGGER = Logger.getLogger(Bittrex.class.getName());

	private final String urlV1 = new String("https://bittrex.com/api/v1.1/public/");

	@Override
	protected void init() {
		super.init();
		exchangeName = "Bittrex";
		logo = null;
		url = "https://bittrex.com/api/v2.0/pub/";
	}

	// Currencies/GetCurrencies
	// Markets/GetMarketSummaries - get the last 24 hour summary of all active
	// exchanges
	// Market/GetMarketSummary - ?market={pair}
	// market/GetTicks -
	// ?marketName={pair}?tickInterval={day,hours,thirtyMinutes,year,month,etc}
	// market/GetLatestTick -
	// ?marketName={pair}?tickInterval={oneMin,fiveMin,hour,thirtyMin,Day,etc}

	@Override
	public void initiate() {

		synchronized (Bittrex.class) {
			if (getStatus() == Status.INIT) {
				init();

				LOGGER.info("Start: Populate list of pairs");

				// Get all currencies
				JSONObject result = JSONFactory.getJSONObject(url + "Currencies/GetCurrencies");

				if (result == null)
					return;

				JSONArray currenciesArray = result.getJSONArray("result");
				if (currenciesArray == null)
					return;

				for (int i = 0; i < currenciesArray.length(); i++) {
					JSONObject currency = currenciesArray.getJSONObject(i);
					String symbol = currency.getString("Currency");
					// String fullName = currency.getString("CurrencyLong");

					coinGraph.addCoin(symbol);
				}

				result = JSONFactory.getJSONObject(urlV1 + "getmarkets");

				if (result == null)
					return;

				JSONArray pairsArray = result.getJSONArray("result");
				if (pairsArray == null)
					return;

				for (int i = 0; i < pairsArray.length(); i++) {
					JSONObject pair = pairsArray.getJSONObject(i);

					String symbolName = pair.getString("MarketName");
					String base = pair.getString("BaseCurrency");
					String quote = pair.getString("MarketCurrency");

					coinGraph.addEdge(base, quote, symbolName, this);
				}

				ExchangeGraph.getInstance().addExchange(this);
				setStatus(Status.READY);
				LOGGER.info("Finish: Populate list of pairs");
			}
		}
	}

	private final String getIntervalFromInt(int i) {

		if (i == 1) {
			return "oneMin";
		}
		if (i == 5) {
			return "fiveMin";
		}
		if (i == 30) {
			return "thirtyMin";
		}
		if (i == 60) {
			return "hour";
		}
		if (i == 1440) {
			return "day";
		}
		// Default "day"
		return "day";
	}

	@Override
	protected boolean updateOLHC(String pair, int interval) {

		LOGGER.info("Update " + pair + ":" + interval);

		// Default interval 1 day
		JSONObject result = JSONFactory.getJSONObject(
				url + "market/GetTicks?marketName=" + pair + "&tickInterval=" + getIntervalFromInt(interval));

		if (result == null)
			return false;

		JSONArray asArray = result.getJSONArray("result");
		if (asArray == null)
			return false;

		if (asArray.length() > 0) {
			List<PairData> dataList = new LinkedList<PairData>();

			for (int i = 0; i < asArray.length(); ++i) {
				try {
					JSONObject content = asArray.getJSONObject(i);

					String time = content.getString("T") + ".00Z";
					Double val = content.getDouble("C");
					Date date = Date.from(Instant.parse(time));

					dataList.add(new PairData(date, val));
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
	protected boolean updateCurrent(String symbol) {
		LOGGER.info("Update symbol:" + symbol);

		// NOTE: V2 is unstable. Often returns data for a different symbol

		JSONObject result = JSONFactory.getJSONObject(urlV1 + "getmarketsummary?market=" + symbol);

		if (result == null)
			return false;

		// Last price for last trade
		double last = result.getJSONArray("result").getJSONObject(0).getDouble("Last");

		addToCurrentCache(symbol, last);

		return true;
	}
	@Override
	protected boolean updateForDate(String symbol, Date date) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBase(String symbol, String from) {
		return symbol.endsWith("-" + from);
	}

}
