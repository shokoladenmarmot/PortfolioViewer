package widgets;

import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Logger;

import com.sun.javafx.charts.Legend;

import core.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import widgets.Assets.Currency;

public class AssetsPieChart extends PieChart {

	private static final Logger LOGGER = Logger.getLogger(AssetsPieChart.class.getName());

	private final Label info = new Label("");
	private final Glow simpleGlow = new Glow();

	public AssetsPieChart() {
		setAnimated(false);

		Legend l = (Legend) getLegend();
		l.setMinWidth(0);
		l.setMinHeight(0);
		l.setMaxWidth(400);
		l.setMaxHeight(50);

		// Add an info label which we will be using to display slice information
		info.resizeRelocate(0, 0, 70, 17);
		info.getStyleClass().add("chart-tooltip-text");
		getChildren().add(info);
	}

	public void add(Currency c) {

		double initialValue = Utils.isLoading(c.getAsUSD()) ? 0 : c.getAsUSD();
		Data newData = new Data(c.getCurrencyName(), initialValue);
		newData.setName(c.getCurrencyName() + " - "
				+ Utils.decimalTwoSymbols.format(initialValue) + " $");

		c.getAsUSDProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(() -> {

					// Note: The chart seems to break when an Infinity is used as a value
					if (Utils.isLoading(newValue) == false) {
						newData.setPieValue(newValue.doubleValue());
						newData.setName(c.getCurrencyName() + " - "
								+ Utils.decimalTwoSymbols.format(newValue.doubleValue()) + " $");
					}
					getData().sort(new Comparator<Data>() {

						@Override
						public int compare(Data o1, Data o2) {
							return (o1.getPieValue() > o2.getPieValue()) ? 0 : 1;
						}
					});
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
		});

	}

	public void cleanByRetainingOnly(Collection<String> s) {
		getData().retainAll(getData().filtered(p -> s.contains(p.getName().substring(0,
				((p.getName().indexOf(' ') == -1) ? p.getName().length() : p.getName().indexOf(' '))))));
	}
}
