package widgets;

import java.util.HashMap;
import java.util.Map.Entry;

import core.Order;
import core.TradeLibrary;
import core.Utils;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class Assets extends VBox {

	public final class Currency {
		private final String currencyName;

		private SimpleDoubleProperty amount;
		private SimpleDoubleProperty asUSD;
		private SimpleDoubleProperty asBTC;
		private SimpleDoubleProperty asETH;

		Currency(String name) {
			currencyName = name;

			amount = new SimpleDoubleProperty(Utils.INVALID_VALUE);
			asUSD = new SimpleDoubleProperty(Utils.INVALID_VALUE);
			asBTC = new SimpleDoubleProperty(Utils.INVALID_VALUE);
			asETH = new SimpleDoubleProperty(Utils.INVALID_VALUE);
		}

		public double getAmount() {
			return amount.get();
		}

		public double getAsUSD() {
			return asUSD.get();
		}

		public double getAsBTC() {
			return asBTC.get();
		}

		public double getAsETH() {
			return asETH.get();
		}

		public String getCurrencyName() {
			return currencyName;
		}

		public void setAmount(double amount) {
			this.amount.setValue(amount);
		}

		public void setAsUSD(double asUSD) {
			this.asUSD.setValue(asUSD);
		}

		public void setAsBTC(double asBTC) {
			this.asBTC.setValue(asBTC);
		}

		public void setAsETH(double asETH) {
			this.asETH.setValue(asETH);
		}
	}

	private PieChart pie;
	private StackedAreaChart<String, Number> area;
	private StackedBarChart<String, Number> bar;

	private TableView<Currency> currencyTable;

	private ObservableList<Currency> assets;

	public Assets() {
		assets = FXCollections.observableArrayList();
		setAlignment(Pos.CENTER);
		setPadding(new Insets(10, 0, 0, 0));

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

		Label title = new Label("Assets");
		title.setAlignment(Pos.CENTER);

		HBox tp = new HBox(40);
		tp.setAlignment(Pos.CENTER);
		tp.setPadding(new Insets(10, 0, 0, 0));
		tp.getChildren().addAll(pie, bar, area);

		currencyTable = new TableView<Currency>(assets);
		currencyTable.setMinHeight(100);
		currencyTable.setEditable(false);
		currencyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<Currency, String> currency = new TableColumn<>("Currency");
		currency.setCellValueFactory(new PropertyValueFactory<Currency, String>("currencyName"));
		currency.setStyle("-fx-alignment: CENTER;");

		TableColumn<Currency, Number> amountCurrent = new TableColumn<>("Amount");
		amountCurrent.setCellValueFactory(new PropertyValueFactory<Currency, Number>("amount"));
		UIUtils.setNumberCellFactory(amountCurrent);
		amountCurrent.setStyle("-fx-alignment: CENTER;");

		TableColumn<Currency, Number> asUSD = new TableColumn<>("USD");
		asUSD.setCellValueFactory(new PropertyValueFactory<Currency, Number>("asUSD"));
		UIUtils.setNumberCellFactory(asUSD);
		asUSD.setStyle("-fx-alignment: CENTER;");

		TableColumn<Currency, Number> asBTC = new TableColumn<>("BTC");
		asBTC.setCellValueFactory(new PropertyValueFactory<Currency, Number>("asBTC"));
		UIUtils.setNumberCellFactory(asBTC);
		asBTC.setStyle("-fx-alignment: CENTER;");

		TableColumn<Currency, Number> asETH = new TableColumn<>("ETH");
		asETH.setCellValueFactory(new PropertyValueFactory<Currency, Number>("asETH"));
		UIUtils.setNumberCellFactory(asETH);
		asETH.setStyle("-fx-alignment: CENTER;");

		currencyTable.getColumns().addAll(currency, amountCurrent, asUSD, asBTC, asETH);

		getChildren().addAll(title, tp, currencyTable);
	}

	private void update() {

		// Clear the whole table and re-populate it.
		assets.clear();

		HashMap<String, Double> values = new HashMap<String, Double>();

		for (Order o : TradeLibrary.getInstance().getOrders()) {
			if (values.containsKey(o.getFrom())) {
				Double v = values.get(o.getFrom());
				values.put(o.getFrom(), v - o.getAmountSpend());
			} else {
				values.put(o.getFrom(), 0 - o.getAmountSpend());
			}
			if (values.containsKey(o.getTo())) {
				Double v = values.get(o.getTo());
				values.put(o.getTo(), v + o.getAmountRecieved());
			} else {
				values.put(o.getTo(), o.getAmountRecieved());
			}
		}

		for (Entry<String, Double> entr : values.entrySet()) {

			// Don't add currencies which balance is currently 0
			if (entr.getValue() != 0) {

				boolean exists = false;
				for (Currency asCurrency : assets) {
					if (asCurrency.getCurrencyName().equals(entr.getKey())) {
						asCurrency.setAmount(entr.getValue());
						exists = true;
						break;
					}
				}
				if (!exists) {
					Currency newCurrency = new Currency(entr.getKey());
					newCurrency.setAmount(entr.getValue());
					assets.add(newCurrency);
				}
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
