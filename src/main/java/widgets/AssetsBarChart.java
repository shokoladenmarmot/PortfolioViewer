package widgets;

import java.util.Collection;
import java.util.logging.Logger;

import core.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.input.MouseEvent;
import widgets.Assets.Currency;

public class AssetsBarChart extends StackedBarChart<String, Number> {

	private static final Logger LOGGER = Logger.getLogger(AssetsBarChart.class.getName());

	private final CategoryAxis xAxis;
	private final NumberAxis yAxis;

	public AssetsBarChart() {
		super(new CategoryAxis(), new NumberAxis());
		xAxis = (CategoryAxis) getXAxis();
		yAxis = (NumberAxis) getYAxis();

		xAxis.setLabel("Date");
		yAxis.setLabel("Value");

		setLegendSide(Side.BOTTOM);

		// TODO Info Label
		// Add an info label which we will be using to display slice information
		// info.resizeRelocate(0, 0, 230, 17);
		// getChildren().add(info);
	}

	public void update() {
		
		// Create new series if there isnt one for the particular month
//		XYChart.Series<String, Number> newSeries =
//	            new XYChart.Series<String, Number>();
		
//		newSeries.setName(value);
		
		// Create newData if it`s not already in the series
		XYChart.Data<String, Number> newData = new XYChart.Data<String,Number>();
		
		// Add listener to all currencies available in the last month.
		
	}

	public void cleanByRetainingOnly(Collection<String> s) {
		// TODO: Filter out columns ( dates ) and data ( currencies within columns )
//		getData().retainAll(getData().filtered(p -> s.contains(p.getName().substring(0,
//				((p.getName().indexOf(' ') == -1) ? p.getName().length() : p.getName().indexOf(' '))))));
	}
}
