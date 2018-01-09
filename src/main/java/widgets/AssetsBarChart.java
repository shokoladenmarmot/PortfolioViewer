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
		XYChart.Series<String, Number> newSeries =
	            new XYChart.Series<String, Number>();
		
		newSeries.setName(value);
		
		// Create newData if it`s not already in the series
		XYChart.Data<String, Number> newData = new XYChart.Data<String,Number>();
		
		// Add listener to all currencies available in the last month.
		
		/*
		Data newData = new Data(c.getCurrencyName(), Utils.isLoading(c.getAsUSD()) ? 0 : c.getAsUSD());

		c.getAsUSDProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(() -> {

					// Note: The chart seems to break when an Infinity is used as a value
					if (Utils.isLoading(c.getAsUSD()) == false) {
						newData.setPieValue(newValue.doubleValue());
						newData.setName(c.getCurrencyName() + " - "
								+ Utils.decimalTwoSymbols.format(newValue.doubleValue()) + "$");
					}

				});
			}
		});

		getData().add(newData);

		newData.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				double total = 0;
				for (Data d : getData()) {
					total += d.getPieValue();
				}
				Point2D point = sceneToLocal(event.getSceneX(), event.getSceneY(), true);

				info.setText(Utils.decimalTwoSymbols.format((newData.getPieValue() / total) * 100) + "%");

				info.setTranslateX(point.getX() - com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader()
						.computeStringWidth(info.getText(), info.getFont()) / 2);
				info.setTranslateY(point.getY() - com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader()
						.getFontMetrics(info.getFont()).getLineHeight());

				newData.getNode().setEffect(simpleGlow);
				info.setVisible(true);
			}
		});
		newData.getNode().addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				newData.getNode().setEffect(null);
				info.setVisible(false);
			}
		});*/
	}

	public void cleanByRetainingOnly(Collection<String> s) {
		// TODO: Filter out columns ( dates ) and data ( currencies within columns )
//		getData().retainAll(getData().filtered(p -> s.contains(p.getName().substring(0,
//				((p.getName().indexOf(' ') == -1) ? p.getName().length() : p.getName().indexOf(' '))))));
	}
}
