package exchanges;

public class Coinbase extends Exchange {

	@Override
	protected void init() {
		super.init();

		exchangeName = "Coinbase";
		logo = null;
		url = "";

		populateListOfPairs();
	}

	@Override
	public void updateOLHC(String pair) {
		super.updateOLHC(pair);

		lastData.clear();
	}

	@Override
	protected void populateListOfPairs() {
		
	}
}
