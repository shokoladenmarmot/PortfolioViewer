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
		setLabelLineLength(10);
		setLegendSide(Side.LEFT);

		// Add an info label which we will be using to display slice information
		info.resizeRelocate(0, 0, 230, 17);
		getChildren().add(info);
	}

	public void add(Currency c) {

		Data newData = new Data(c.getCurrencyName(), 0);
		c.getAsUSDProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Platform.runLater(() -> {

					newData.setPieValue(newValue.doubleValue());
					newData.setName(
							c.getCurrencyName() + " - " + Utils.decimalTwoSymbols.format(newValue.doubleValue()) + "$");

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

				info.setText(Utils.decimalTwoSymbols.format((total / newData.getPieValue()) * 100) + "%");

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
