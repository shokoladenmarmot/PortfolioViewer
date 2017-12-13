package core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Order {

	public enum OrderType {
		INPUT, OUTPUT;

		public String toString() {
			return (this == OUTPUT) ? "Output" : "Input";
		}
	}

	private static final Logger LOGGER = Logger.getLogger(Order.class.getName());

	public final String symbol;
	public final Double amount;
	public final OrderType type;
	public final Date date;

	public Order(String s, Double a, OrderType t, Date d) {
		symbol = s;
		amount = a;
		type = t;
		date = d;
	}

	public Date getDate() {
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
		sb.append(new SimpleDateFormat("dd-MMM-yyyy").format(date).toString());

		return sb.toString();
	}
}
