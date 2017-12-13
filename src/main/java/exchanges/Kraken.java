package exchanges;

import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import core.JSONFactory;

public class Kraken extends Exchange {

	@Override
	protected void init() {
		super.init();

		exchangeName = "Kraken";
		logo = null;
		url = "https://api.kraken.com/0/public/";

		defaultPairList.add("BCHUSD");
		availablePairList.addAll(defaultPairList);
		populateListOfPairs();
	}

	@Override
	protected void populateListOfPairs() {
		new Thread(() -> {
			JSONObject result = JSONFactory.getJSONObject(url + "AssetPairs");
			availablePairList.addAll(((JSONObject) result.get("result")).keySet());

		}).start();
	}

	@Override
	public void updateOLHC(String pair) {
		if (availablePairList.contains(pair) == false) {
			return;
		}
		super.updateOLHC(pair);

		// Default interval 1 day
		JSONObject result = JSONFactory.getJSONObject(url + "OHLC?pair=" + pair + "&interval=1440"); // &since=1501545600

		// KRAKEN: array of array entries(<time>, <open>, <high>, <low>, <close>,
		// <vwap>, <volume>, <count>)

		JSONArray asArray = ((JSONObject) result.get("result")).getJSONArray(pair);

		final GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));

		lastData.remove(pair);

		List<PairData> newList = new LinkedList<PairData>();

		for (int i = 0; i < asArray.length(); ++i) {
			JSONArray content = asArray.getJSONArray(i);
			gc.setTimeInMillis(content.getLong(0) * 1000);
			newList.add(new PairData(gc.getTime().toString(), Double.parseDouble(content.getString(4))));
		}

		lastData.put(pair, newList);
	}
}
