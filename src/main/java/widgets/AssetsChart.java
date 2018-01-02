package widgets;

import java.util.HashMap;

import core.Order;
import core.TradeLibrary;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AssetsChart extends VBox {

	public final class Currency {
		private final String currencyName;

		private SimpleStringProperty amount;
		private SimpleStringProperty asUSD;
		private SimpleStringProperty asBTC;
		private SimpleStringProperty asETH;

		Currency(String name) {
			currencyName = name;

			amount = new SimpleStringProperty();
			asUSD = new SimpleStringProperty();
			asBTC = new SimpleStringProperty();
			asETH = new SimpleStringProperty();
		}

		public String getAmount() {
			return amount.get();
		}

		public String getAsUSD() {
			return asUSD.get();
		}

		public String getAsBTC() {
			return asBTC.get();
		}

		public String getAsETH() {
			return asETH.get();
		}

		public String getCurrencyName() {
			return currencyName;
		}

		public void setAmount(String amount) {
			this.amount.setValue(amount);
		}

		public void setAsUSD(String asUSD) {
			this.asUSD.setValue(asUSD);
		}

		public void setAsBTC(String asBTC) {
			this.asBTC.setValue(asBTC);
		}

		public void setAsETH(String asETH) {
			this.asETH.setValue(asETH);
		}
	}

	private PieChart pie;
	private StackedAreaChart<String, Number> area;
	private StackedBarChart<String, Number> bar;

	private ObservableList<Currency> assets;

	public AssetsChart() {
		assets = FXCollections.observableArrayList();

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

		HBox tp = new HBox(40);
		tp.setAlignment(Pos.CENTER);
		tp.setPadding(new Insets(10, 0, 0, 0));
		tp.getChildren().addAll(pie, bar, area);

		TableView<Currency> currencyTable = new TableView<Currency>(assets);
		currencyTable.setEditable(false);
		currencyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<Currency, String> currency = new TableColumn<>("Currency");
		currency.setCellValueFactory(new PropertyValueFactory<Currency, String>("currencyName"));
		currency.setStyle("-fx-alignment: CENTER;");
		currency.setResizable(false);

		TableColumn<Currency, String> amountCurrent = new TableColumn<>("Amount");
		amountCurrent.setCellValueFactory(new PropertyValueFactory<Currency, String>("amount"));
		amountCurrent.setStyle("-fx-alignment: CENTER;");
		amountCurrent.setResizable(false);

		TableColumn<Currency, String> asUSD = new TableColumn<>("USD");
		asUSD.setCellValueFactory(new PropertyValueFactory<Currency, String>("asUSD"));
		asUSD.setStyle("-fx-alignment: CENTER;");
		asUSD.setResizable(false);

		TableColumn<Currency, String> asBTC = new TableColumn<>("BTC");
		asBTC.setCellValueFactory(new PropertyValueFactory<Currency, String>("asBTC"));
		asBTC.setStyle("-fx-alignment: CENTER;");
		asBTC.setResizable(false);

		TableColumn<Currency, String> asETH = new TableColumn<>("ETH");
		asETH.setCellValueFactory(new PropertyValueFactory<Currency, String>("asETH"));
		asETH.setStyle("-fx-alignment: CENTER;");
		asETH.setResizable(false);

		currencyTable.getColumns().addAll(currency, amountCurrent, asUSD, asBTC, asETH);

		getChildren().addAll(tp, currencyTable);
	}

	private void update() {
		HashMap<String, Double> values = new HashMap<String, Double>();

		for (Order o : TradeLibrary.getInstance().getOrders()) {
			if (values.containsKey(o.getFrom())) {
				Double v = values.get(o.getFrom());
				values.put(o.getFrom(), v - o.getAmountSpend());
			} else {
				values.put(o.getFrom(), -o.getAmountSpend());
			}
			if (values.containsKey(o.getTo())) {
				Double v = values.get(o.getTo());
				values.put(o.getTo(), v + o.getAmountRecieved());
			} else {
				values.put(o.getTo(), o.getAmountRecieved());
			}
		}

		for (String currency : values.keySet()) {
			boolean exists = false;
			for (Currency asCurrency : assets) {
				if (asCurrency.getCurrencyName().equals(currency)) {
					asCurrency.setAmount(values.get(currency).toString());
					exists = true;
					break;
				}
			}
			if (!exists) {
				Currency newCurrency = new Currency(currency);
				newCurrency.setAmount(values.get(currency).toString());
				assets.add(newCurrency);
			}
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
