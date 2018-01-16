package exchanges;

import java.util.Date;
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
	public boolean updateOLHC(String pair, int interval) {

		LOGGER.info("Update " + pair + ":" + interval);
		return true;

	}

	@Override
	public void initiate() {
		synchronized (Coinbase.class) {
			if (getStatus() == Status.INIT) {
				init();
				LOGGER.info("Start: Populate list of pairs");
				setStatus(Status.READY);
				LOGGER.info("Finish: Populate list of pairs");
			}
		}
	}

	@Override
	protected boolean updateCurrent(String symbol) {
		LOGGER.info("Update symbol:" + symbol);
		return false;
	}

	@Override
	protected boolean updateForDate(String symbol, Date date) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBase(String symbol, String from) {
		return false;
	}
}
