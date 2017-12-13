package exchanges;

public enum ExchangeProvider {
	KRAKEN(Kraken.class), COINBASE(Coinbase.class);

	private Exchange inst;
	private Class<? extends Exchange> exchangeClass;

	ExchangeProvider(Class<? extends Exchange> ec) {
		exchangeClass = ec;
		try {
			inst = ec.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public Class<? extends Exchange> getExchangeClass() {
		return exchangeClass;
	}

	public Exchange getInstance() {
		return inst;
	}
}
