package exchanges;

import java.util.logging.Logger;

public class Coinbase extends Exchange {
	
	private static final Logger LOGGER = Logger.getLogger( Coinbase.class.getName() );

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

		lastData.clear();
	}

	@Override
	protected void populateListOfPairs() {

	}

	@Override
	protected void updateLastTime() {
		// TODO Auto-generated method stub

	}
}
