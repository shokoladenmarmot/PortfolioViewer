package exchanges;

import java.util.logging.Logger;

public class Coinbase extends Exchange {

	private static final Logger LOGGER = Logger.getLogger(Coinbase.class.getName());

	@Override
	protected void init() {
		super.init();

		exchangeName = "Coinbase";
		logo = null;
		url = "";

		populateListOfPairs();
	}

	@Override
	public void updateOLHC(String pair, int interval) {
		super.updateOLHC(pair, interval);

		LOGGER.info("Update " + pair + ":" + interval);

		lastData.clear();
	}

	@Override
	protected void populateListOfPairs() {
		LOGGER.info("Populate list of pairs");
	}

	@Override
	protected void updateLastTime() {
		LOGGER.info("Update list of time");
	}
}
