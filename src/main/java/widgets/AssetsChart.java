package widgets;

import java.util.HashMap;

import javax.naming.InitialContext;

import core.Order;
import core.TradeLibrary;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class AssetsChart extends VBox {

	private final class Currency {
		final String currencyName;

		private SimpleStringProperty amount;
		private SimpleStringProperty asUSD;
		private SimpleStringProperty asBTC;
		private SimpleStringProperty asETH;

		Currency(String name) {
			currencyName = name;

			asUSD = new SimpleStringProperty();
			asBTC = new SimpleStringProperty();
			asETH = new SimpleStringProperty();
		}
	}

	private PieChart pie;
	private StackedAreaChart<X, Y> stacked;
	private BarChart<X, Y> bar;
	private GridPane currencyGrid;

	private HashMap<String, Currency> assets;

	public AssetsChart() {
		assets = new HashMap<String, Currency>();

		this.setAlignment(Pos.CENTER);
		this.setPadding(new Insets(10, 0, 0, 0));

		init();
	}

	private void init() {

		bar = new BarChart<>(xAxis, yAxis);
		pie = new PieChart();
		stacked = new StackedBarChart<>(xAxis, yAxis);

		currencyGrid = new GridPane();
		currencyGrid.setHgap(10);
		currencyGrid.setVgap(10);
		currencyGrid.setAlignment(Pos.CENTER);

		TradeLibrary.getInstance().getOrders().addListener(new ListChangeListener<Order>() {

			@Override
			public void onChanged(Change<? extends Order> c) {
				update();
			}
		});

		TilePane tp = new TilePane(Orientation.HORIZONTAL);
		tp.setPadding(new Insets(20, 10, 20, 0));
		tp.setHgap(10.0);
		tp.getChildren().addAll(pie, bar, stacked);

		currencyGrid.chiadd(new Label("Currency"), 0, 1);
		currencyGrid.add(new Label("Amount"), 1, 1);
		currencyGrid.add(new Label("USD"), 2, 1);
		currencyGrid.add(new Label("BTC"), 3, 1);
		currencyGrid.add(new Label("ETH"), 4, 1);

		getChildren().addAll(tp, currencyGrid);
	}

	private void update() {

	}

	public BarChart<X, Y> getBar() {
		return bar;
	}

	public PieChart getPie() {
		return pie;
	}

	public StackedAreaChart<X, Y> getStacked() {
		return stacked;
	}
}
