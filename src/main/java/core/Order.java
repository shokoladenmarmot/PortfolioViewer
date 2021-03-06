package core;

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class Order {

	// public enum OrderType {
	// INPUT, OUTPUT;
	//
	// public String toString() {
	// return (this == OUTPUT) ? "OUTPUT" : "INPUT";
	// }
	//
	// public static OrderType get(String t) {
	// return t.equalsIgnoreCase("INPUT") ? INPUT : (t.equalsIgnoreCase("OUTPUT")) ?
	// OUTPUT : null;
	// }
	// }

	private static final Logger LOGGER = Logger.getLogger(Order.class.getName());

	// The market symbol
	private final String symbol;

	// Name of the exchange
	private final String market;

	// The from currency
	private final String from;

	// The to currency
	private final String to;

	// Amount spend
	private final Double amountSpend;

	// Amount spend
	private final Double amountRecieved;

	// Date in unix time stamp ( seconds )
	private final long date;

	public Order(String s, String m, String f, String t, Double as, Double ar, long d) {
		symbol = s;
		market = m;
		from = f;
		to = t;
		amountSpend = as;
		amountRecieved = ar;
		date = d;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public long getDate() {
		return date;
	}

	public Double getAmountSpend() {
		return amountSpend;
	}

	public Double getAmountRecieved() {
		return amountRecieved;
	}

	public String getSymbol() {
		return symbol;
	}

	public Double getPrice(boolean fromPointOfView) {
		return (fromPointOfView) ? (amountRecieved / amountSpend) : (amountSpend / amountRecieved);
	}

	public String getMarket() {
		return market;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("\nSymbol: ");
		sb.append(symbol);

		sb.append("\nMarket: ");
		sb.append(market);

		sb.append("\nFrom: ");
		sb.append(from);

		sb.append("\nTo: ");
		sb.append(to);

		sb.append("\nAmount Spend: ");
		sb.append(amountSpend);

		sb.append("\nAmount Recieved: ");
		sb.append(amountRecieved);

		sb.append("\nDate: ");
		sb.append(getDateToString());

		return sb.toString();
	}

	public String getDateToString() {
		return new SimpleDateFormat("dd-MMM-yyyy").format(date * 1000).toString();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append("<order>\n");
		sb.append("<symbol>");
		sb.append(symbol);
		sb.append("</symbol>\n");
		sb.append("<market>");
		sb.append(market);
		sb.append("</market>\n");
		sb.append("<from>");
		sb.append(from);
		sb.append("</from>\n");
		sb.append("<to>");
		sb.append(to);
		sb.append("</to>\n");
		sb.append("<amountspend>");
		sb.append(amountSpend);
		sb.append("</amountspend>\n");
		sb.append("<amountrecieved>");
		sb.append(amountRecieved);
		sb.append("</amountrecieved>\n");
		sb.append("<date>");
		sb.append(date);
		sb.append("</date>\n");
		sb.append("</order>\n");

		return sb.toString();
	}
}
