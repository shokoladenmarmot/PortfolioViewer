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
	}

	@Override
	public void updateOLHC(String pair, int interval) {

		LOGGER.info("Update " + pair + ":" + interval);

	}

	@Override
	public void initiate() {
		synchronized (Coinbase.class) {
			if (getStatus() == Status.INIT) {
				LOGGER.info("Start: Populate list of pairs");
				setStatus(Status.READY);
				LOGGER.info("Finish: Populate list of pairs");
			}
		}
	}

	@Override
	protected void updateLastTime() {
		LOGGER.info("Update list of time");
	}

	@Override
	protected void updateCurrent(String symbol) {
		LOGGER.info("Update symbol:" + symbol);
	}

	@Override
	public boolean isBase(String symbol, String from) {
		return false;
	}
}
