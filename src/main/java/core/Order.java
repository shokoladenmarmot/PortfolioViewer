package core;

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class Order {

	public enum OrderType {
		INPUT, OUTPUT;

		public String toString() {
			return (this == OUTPUT) ? "Output" : "Input";
		}

		public static OrderType get(String t) {
			return t.equalsIgnoreCase("Input") ? INPUT : OUTPUT;
		}
	}

	private static final Logger LOGGER = Logger.getLogger(Order.class.getName());

	// The pair symbol
	public final String symbol;

	// Amount send/received
	public final Double amount;

	// Type of order INPUT/OUTPUT
	public final OrderType type;

	// Date in unix time stamp ( seconds )
	public final long date;

	public Order(String s, Double a, OrderType t, long d) {
		symbol = s;
		amount = a;
		type = t;
		date = d;
	}

	public long getDate() {
		return date;
	}

	public Double getAmount() {
		return amount;
	}

	public String getSymbol() {
		return symbol;
	}

	public OrderType getType() {
		return type;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("\nSymbol: ");
		sb.append(symbol);
		sb.append("\nType: ");
		sb.append(type.toString());
		sb.append("\nAmount: ");
		sb.append(amount);
		sb.append("\nDate: ");
		sb.append(new SimpleDateFormat("dd-MMM-yyyy").format(date * 1000).toString());

		return sb.toString();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append("<order>\n");
		sb.append("<symbol>");
		sb.append(symbol);
		sb.append("<\\symbol>\n");
		sb.append("<amount>");
		sb.append(amount);
		sb.append("<\\amount>\n");
		sb.append("<type>");
		sb.append(type.toString());
		sb.append("<\\type>\n");
		sb.append("<date>");
		sb.append(date);
		sb.append("<\\date>\n");
		sb.append("<\\order>");

		return sb.toString();
	}
}
