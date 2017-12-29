package exchanges;

import Start.Main;

public enum ExchangeProvider {

	KRAKEN(Kraken.class), COINBASE(Coinbase.class), BITTREX(Bittrex.class), BINANCE(Binance.class);

	// private static final Logger LOGGER =
	// Logger.getLogger(ExchangeProvider.class.getName());

	private Exchange inst;
	private Class<? extends Exchange> exchangeClass;

	ExchangeProvider(Class<? extends Exchange> ec) {
		exchangeClass = ec;
		try {
			inst = ec.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			Main.LOGGER.warning(e.getMessage());
			e.printStackTrace();
		}
	}

	public Class<? extends Exchange> getExchangeClass() {
		return exchangeClass;
	}

	public Exchange getInstance() {
		return inst;
	}

	public static Exchange getMarket(String s) {
		for (ExchangeProvider ex : values()) {
			if (ex.getInstance().exchangeName.equals(s)) {
				return ex.getInstance();
			}
		}
		return null;
	}
}
