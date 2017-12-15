package exchanges;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Logger;

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

		populateListOfPairs();
	}

	@Override
	protected void populateListOfPairs() {

		LOGGER.info("Populate list of pairs");
		new Thread(() -> {

			// Get all tradable pairs
			JSONObject result = JSONFactory.getJSONObject(url + "AssetPairs");
			if (result == null)
				return;

			Set<String> pairSet = ((JSONObject) result.get("result")).keySet();

			// Get all currencies
			result = JSONFactory.getJSONObject(url + "Assets");
			if (result == null)
				return;

			Set<String> symbols = new TreeSet<String>();
			symbols = ((JSONObject) result.get("result")).keySet();

			for (String symb : symbols) {

				String altName = ((JSONObject) result.get("result")).getJSONObject(symb).getString("altname");
				List<String> pairsForSymbol;
				for (String p : pairSet) {

					String pair = p;
					if (pair.startsWith(symb)) {

					} else if (pair.endsWith(symb)) {

					}
				}
				coinMap.put(altName, pairsForSymbol);
				// ((JSONObject)result.get("result")).getJSONObject(symb).get("altname"));
				// System.out.println((((JSONObject)
				// result.get("result")).getJSONObject(symb).get("altname")).toString(2));
			}

		}).start();
	}

	@Override
	public void updateOLHC(String pair, int interval) {
		if (availablePairList.contains(pair) == false) {
			return;
		}
		super.updateOLHC(pair, interval);

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

			lastData.remove(pair);

			// Note: A request to lastData at this moment will result in an empty map,
			// therefore we need a cached version to be used on request.
			List<PairData> newList = new LinkedList<PairData>();

			for (int i = 0; i < asArray.length(); ++i) {
				JSONArray content = asArray.getJSONArray(i);
				// Time is in second so we need to convert to millisec
				gc.setTimeInMillis(content.getLong(0) * 1000);
				newList.add(new PairData(dateFormat.format(gc.getTime()), Double.parseDouble(content.getString(4))));
			}

			lastData.put(pair, newList);
			cached.put(pair, newList);
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
