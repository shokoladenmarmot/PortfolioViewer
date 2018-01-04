package core;

import java.text.DecimalFormat;

public class Utils {

	public static final Double LOADING_VALUE = Double.NEGATIVE_INFINITY;

	public static final DecimalFormat decimalEightSymbols = new DecimalFormat("0.00000000");
	public static final DecimalFormat decimalFiveSymbols = new DecimalFormat("0.00000");
	public static final DecimalFormat decimalTwoSymbols = new DecimalFormat("0.00");

	public static final String doubleExpression = new String("\\d*\\.?\\d*");
}
