package widgets;

import core.Order;
import core.TradeLibrary;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class AssetsChart extends VBox {

	private final class Currency extends HBox {
		final String currencyName;

		private SimpleStringProperty amount;
		private SimpleStringProperty asUSD;
		private SimpleStringProperty asBTC;
		private SimpleStringProperty asETH;

		Currency(String name) {
			setAlignment(Pos.CENTER);
			currencyName = name;

			amount = new SimpleStringProperty();
			asUSD = new SimpleStringProperty();
			asBTC = new SimpleStringProperty();
			asETH = new SimpleStringProperty();

			Label amountL = new Label();
			amountL.textProperty().bind(amount);
			Label usdL = new Label();
			usdL.textProperty().bind(asUSD);
			Label btcL = new Label();
			btcL.textProperty().bind(asBTC);
			Label ethL = new Label();
			ethL.textProperty().bind(asETH);

			getChildren().addAll(new Label(currencyName), amountL, usdL, btcL, ethL);
		}

		public SimpleStringProperty getAmount() {
			return amount;
		}

		public SimpleStringProperty getAsUSD() {
			return asUSD;
		}

		public SimpleStringProperty getAsBTC() {
			return asBTC;
		}

		public SimpleStringProperty getAsETH() {
			return asETH;
		}

		public String getCurrencyName() {
			return currencyName;
		}
	}

	private PieChart pie;
	private StackedAreaChart<String, Number> area;
	private StackedBarChart<String, Number> bar;

	private ObservableList<Node> assets;

	public AssetsChart() {
		assets = FXCollections.observableArrayList();

		this.setAlignment(Pos.CENTER);
		// this.setPadding(new Insets(0, 0, 0, 0));

		init();
	}

	private void init() {
		NumberAxis na = new NumberAxis();
		na.setLabel("Value");
		NumberAxis na2 = new NumberAxis();
		na2.setLabel("Value");

		bar = new StackedBarChart<String, Number>(new CategoryAxis(), na);
		area = new StackedAreaChart<String, Number>(new CategoryAxis(), na2);
		pie = new PieChart();

		TradeLibrary.getInstance().getOrders().addListener(new ListChangeListener<Order>() {

			@Override
			public void onChanged(Change<? extends Order> c) {
				update();
			}
		});

		TilePane tp = new TilePane(Orientation.HORIZONTAL);
		tp.setPadding(new Insets(20, 10, 20, 0));
		tp.setHgap(10.0);
		tp.getChildren().addAll(pie, bar, area);

		HBox headers = new HBox();
		headers.setPadding(new Insets(0, 0, 10, 0));
		headers.setAlignment(Pos.CENTER);
		headers.getChildren().addAll(new Label("Currency"), new Label("Amount"), new Label("USD"), new Label("BTC"),
				new Label("ETH"));

		VBox table = new VBox();
		table.setAlignment(Pos.CENTER);

		assets = table.getChildren();

		getChildren().addAll(tp, headers, table);
	}

	private void update() {
		for (Node n : assets) {
			// TODO: compare class type and name of the currency
			// Create an asset for every currency stored in the TradeLibrary and update their total value
		}
	}

	public StackedBarChart<String, Number> getBar() {
		return bar;
	}

	public PieChart getPie() {
		return pie;
	}

	public StackedAreaChart<String, Number> getArea() {
		return area;
	}
}
