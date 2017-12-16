package exchanges;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import javafx.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import core.JSONFactory;

public class Kraken extends Exchange {

	private static final Logger LOGGER = Logger.getLogger(Kraken.class.getName());
	// Kraken requests:
	// System.out.println(JSONFactory.getJSONObject("https://api.kraken.com/0/public/Ticker?pair=BCHEUR").toString(2));

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
				LOGGER.info("Start: Populate list of pairs");

				// Get all currencies
				JSONObject result = JSONFactory.getJSONObject(url + "Assets");
				if (result == null)
					return;

				Set<String> symbols = ((JSONObject) result.get("result")).keySet();

				HashMap<String, String> symbolAltname = new HashMap<String, String>();
				for (String symb : symbols) {

					String altName = ((JSONObject) result.get("result")).getJSONObject(symb).getString("altname");
					symbolAltname.put(symb, altName);
					List<Pair<String, String>> list = new LinkedList<Pair<String, String>>();
					coinMap.put(altName, list);
				}

				// Get all tradable pairs
				result = JSONFactory.getJSONObject(url + "AssetPairs");
				if (result == null)
					return;

				Set<String> pairSet = ((JSONObject) result.get("result")).keySet();

				for (String pair : pairSet) {

					// Don't take dark pools
					if (pair.endsWith(".d"))
						continue;

					// String altName = ((JSONObject)
					// result.get("result")).getJSONObject(pair).getString("altname");

					String quote = symbolAltname
							.get(((JSONObject) result.get("result")).getJSONObject(pair).getString("quote"));
					String base = symbolAltname
							.get(((JSONObject) result.get("result")).getJSONObject(pair).getString("base"));

					List<Pair<String, String>> quoteList = coinMap.get(quote);
					List<Pair<String, String>> baseList = coinMap.get(base);

					quoteList.add(new Pair<String, String>(base, pair));
					baseList.add(new Pair<String, String>(quote, pair));
				}
				setStatus(Status.READY);
				LOGGER.info("Finish: Populate list of pairs");
			}
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

		JSONArray asArray = ((JSONObject) result.get("result")).getJSONArray(pair);
		if (asArray.length() > 0) {
			final GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

			List<PairData> dataList = new LinkedList<PairData>();

			for (int i = 0; i < asArray.length(); ++i) {
				JSONArray content = asArray.getJSONArray(i);
				// Time is in second so we need to convert to millisec
				gc.setTimeInMillis(content.getLong(0) * 1000);
				dataList.add(new PairData(dateFormat.format(gc.getTime()), Double.parseDouble(content.getString(4))));
			}
			addToCache(pair, interval, dataList);
		}
	}

	@Override
	protected void updateLastTime() {
		LOGGER.info("Update list of time");

		JSONObject result = JSONFactory.getJSONObject(url + "Time");
		if (result == null)
			return;

		lastUpdate.set(Long.parseLong(((JSONObject) result.get("result")).get("unixtime").toString()));
	}
}
