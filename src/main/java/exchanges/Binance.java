package exchanges;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import core.JSONFactory;

public class Binance extends Exchange {

	private static final Logger LOGGER = Logger.getLogger(Binance.class.getName());

	@Override
	protected void init() {
		super.init();
		exchangeName = "Binance";
		logo = null;
		url = "https://api.binance.com/api/v1/";
	}

	@Override
	public void initiate() {
		// exchangeInfo
		// exchange/public/product -
		synchronized (Binance.class) {
			if (getStatus() == Status.INIT) {
				init();

				LOGGER.info("Start: Populate list of pairs");

				// Get all currencies
				JSONObject result = JSONFactory.getJSONObject(url + "exchangeInfo");

				if (result == null)
					return;

				JSONArray currenciesArray = result.getJSONArray("symbols");
				if (currenciesArray == null)
					return;

				for (int i = 0; i < currenciesArray.length(); i++) {
					JSONObject currency = currenciesArray.getJSONObject(i);
					String symbol = currency.getString("symbol");
					String base = currency.getString("baseAsset");
					String quote = currency.getString("quoteAsset");

					coinGraph.addCoin(base);
					coinGraph.addCoin(quote);

					coinGraph.addEdge(base, quote, symbol, this);
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
		if (i == 3) {
			return "3m";
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
		if (i == 120) {
			return "2h";
		}
		if (i == 240) {
			return "4h";
		}
		if (i == 360) {
			return "6h";
		}
		if (i == 480) {
			return "8h";
		}
		if (i == 720) {
			return "12h";
		}
		if (i == 1440) {
			return "1d";
		}
		if (i == 4320) {
			return "3d";
		}
		if (i == 43800) {
			return "1M";
		}

		return "1d";
	}

	@Override
	protected boolean updateOLHC(String pair, int interval) {
		// v1/klines - symbol = symbol (startTime/endTime)

		JSONArray result = JSONFactory
				.getJSONArray(url + "klines?symbol=" + pair + "&interval=" + getIntervalFromInt(interval));

		if (result == null)
			return false;

		if (result.length() > 0) {
			List<PairData> dataList = new LinkedList<PairData>();

			for (int i = 0; i < result.length(); ++i) {
				JSONArray content = result.getJSONArray(i);

				try {
					long time = content.getLong(0);
					Double val = Double.parseDouble(content.getString(4));

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
	protected boolean updateCurrent(String symbol) {
		// NOTE: So far there hasnt been any problems with this but maybe I should use
		// API "v3" instead "v1"

		JSONObject result = JSONFactory.getJSONObject(url + "ticker/price?symbol=" + symbol);
		if (result == null)
			return false;

		try {
			double last = Double.parseDouble(result.getString("price"));
			addToCurrentCache(symbol, last);

			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
			return false;
		}
	}
	
	@Override
	protected boolean updateForDate(String symbol, Date date) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBase(String symbol, String from) {
		return symbol.startsWith(from);
	}

}
