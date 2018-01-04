package widgets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import core.Order;
import core.TradeLibrary;
import core.Utils;
import exchanges.Exchange;
import exchanges.ExchangeProvider;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Assets extends VBox {

	public final class Currency {
		private final String currencyName;

		private SimpleDoubleProperty amount;

		private SimpleDoubleProperty asUSD;
		private SimpleDoubleProperty asBTC;
		private SimpleDoubleProperty asETH;

		private SimpleStringProperty marketBTC;
		private SimpleStringProperty marketUSD;
		private SimpleStringProperty marketETH;

		Currency(String name) {
			currencyName = name;

			amount = new SimpleDoubleProperty(Utils.LOADING_VALUE);

			asUSD = new SimpleDoubleProperty(Utils.LOADING_VALUE);
			if (currencyName.equals("USD")) {
				asUSD.bind(amount);
			}
			asBTC = new SimpleDoubleProperty(Utils.LOADING_VALUE);
			if (currencyName.equals("BTC")) {
				asBTC.bind(amount);
			}
			asETH = new SimpleDoubleProperty(Utils.LOADING_VALUE);
			if (currencyName.equals("ETH")) {
				asETH.bind(amount);
			}

			// TODO: Get initial values for markets
			marketUSD = new SimpleStringProperty();
			marketBTC = new SimpleStringProperty();
			marketETH = new SimpleStringProperty();
		}

		public SimpleDoubleProperty getAmountProperty() {
			return amount;
		}

		public SimpleDoubleProperty getAsUSDProperty() {
			return asUSD;
		}

		public SimpleDoubleProperty getAsBTCProperty() {
			return asBTC;
		}

		public SimpleDoubleProperty getAsETHProperty() {
			return asETH;
		}

		public SimpleStringProperty getMarketUSDProperty() {
			return marketUSD;
		}

		public SimpleStringProperty getMarketBTCProperty() {
			return marketBTC;
		}

		public SimpleStringProperty getMarketETHProperty() {
			return marketETH;
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

		public void setAsUSD(double val) {
			this.asUSD.setValue(val);
		}

		public void setAsBTC(double val) {
			this.asBTC.setValue(val);
		}

		public void setAsETH(double val) {
			this.asETH.setValue(val);
		}

		public String getMarketUSD() {
			return marketUSD.get();
		}

		public String getMarketBTC() {
			return marketBTC.get();
		}

		public String getMarketETH() {
			return marketETH.get();
		}

		public void setMarketUSD(String s) {
			this.marketUSD.setValue(s);
		}

		public void setMarketBTC(String s) {
			this.marketBTC.setValue(s);
		}

		public void setMarketETH(String s) {
			this.marketETH.setValue(s);
		}
	}

	private AssetsPieChart pie;
	private StackedAreaChart<String, Number> area;
	private StackedBarChart<String, Number> bar;

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
		pie = new AssetsPieChart();

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

		TableView<Currency> currencyTable = new TableView<Currency>(assets);
		currencyTable.setMinHeight(100);
		currencyTable.setEditable(true);
		currencyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<Currency, String> currency = new TableColumn<>("Currency");
		currency.setCellValueFactory(new PropertyValueFactory<Currency, String>("currencyName"));
		currency.setStyle("-fx-alignment: CENTER;");

		TableColumn<Currency, Number> amountCurrent = new TableColumn<>("Amount");
		amountCurrent.setCellValueFactory(a -> a.getValue().getAmountProperty());
		UIUtils.setNumberCellFactory(amountCurrent);
		amountCurrent.setStyle("-fx-alignment: CENTER;");

		TableColumn<Currency, Number> asUSD = new TableColumn<>("Value");
		asUSD.setCellValueFactory(a -> a.getValue().getAsUSDProperty());
		UIUtils.setNumberCellFactory(asUSD);
		asUSD.setStyle("-fx-alignment: CENTER;");

		TableColumn<Currency, String> USDMarket = new TableColumn<>("Market");
		USDMarket.setCellValueFactory(a -> a.getValue().getMarketUSDProperty());
		UIUtils.setComboCellFactoryForMarket(USDMarket, "USD");
		USDMarket.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Currency, String>>() {
			@Override
			public void handle(CellEditEvent<Currency, String> event) {
				event.getRowValue().setMarketUSD(event.getNewValue());
			}
		});
		USDMarket.setStyle("-fx-alignment: CENTER;");
		USDMarket.setMinWidth(100);
		USDMarket.setMaxWidth(100);

		TableColumn<Currency, Number> USD = new TableColumn<>("USD");
		USD.getColumns().addAll(asUSD, USDMarket);

		TableColumn<Currency, Number> asBTC = new TableColumn<>("Value");
		asBTC.setCellValueFactory(a -> a.getValue().getAsBTCProperty());
		UIUtils.setNumberCellFactory(asBTC);
		asBTC.setStyle("-fx-alignment: CENTER;");

		TableColumn<Currency, String> BTCMarket = new TableColumn<>("Market");
		BTCMarket.setCellValueFactory(a -> a.getValue().getMarketBTCProperty());
		UIUtils.setComboCellFactoryForMarket(BTCMarket, "BTC");
		BTCMarket.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Currency, String>>() {
			@Override
			public void handle(CellEditEvent<Currency, String> event) {
				event.getRowValue().setMarketBTC(event.getNewValue());
			}
		});
		BTCMarket.setStyle("-fx-alignment: CENTER;");
		BTCMarket.setMinWidth(100);
		BTCMarket.setMaxWidth(100);

		TableColumn<Currency, Number> BTC = new TableColumn<>("BTC");
		BTC.getColumns().addAll(asBTC, BTCMarket);

		TableColumn<Currency, Number> asETH = new TableColumn<>("Value");
		asETH.setCellValueFactory(a -> a.getValue().getAsETHProperty());
		UIUtils.setNumberCellFactory(asETH);
		asETH.setStyle("-fx-alignment: CENTER;");

		TableColumn<Currency, String> ETHmarket = new TableColumn<>("Market");
		ETHmarket.setCellValueFactory(a -> a.getValue().getMarketETHProperty());
		UIUtils.setComboCellFactoryForMarket(ETHmarket, "ETH");
		ETHmarket.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Currency, String>>() {
			@Override
			public void handle(CellEditEvent<Currency, String> event) {
				event.getRowValue().setMarketETH(event.getNewValue());
			}
		});
		ETHmarket.setStyle("-fx-alignment: CENTER;");
		ETHmarket.setMinWidth(100);
		ETHmarket.setMaxWidth(100);

		TableColumn<Currency, Number> ETH = new TableColumn<>("ETH");
		ETH.getColumns().addAll(asETH, ETHmarket);

		currencyTable.getColumns().addAll(currency, amountCurrent, USD, BTC, ETH);

		getChildren().addAll(title, tp, currencyTable);
	}

	private void update() {

		Set<Currency> listToRetain = new HashSet<Currency>();

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

			listToRetain.addAll(assets.stream()
					.filter(a -> (a.getCurrencyName().equals(o.getFrom()) || a.getCurrencyName().equals(o.getTo())))
					.collect(Collectors.toSet()));

		}

		assets.retainAll(listToRetain);
		pie.cleanByRetainingOnly(values.keySet());

		for (Entry<String, Double> entr : values.entrySet()) {

			// Don't add currencies which balance is currently 0
			if (entr.getValue() >= 0) {

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

					newCurrency.getMarketUSDProperty().addListener(new ChangeListener<String>() {
						private NumberBinding nb = null;
						private InvalidationListener listener = null;

						@Override
						public void changed(ObservableValue<? extends String> observable, String oldValue,
								String newValue) {
							if (nb != null) {
								nb.removeListener(listener);
							}
							String asUSD = newCurrency.getMarketUSD();
							if ((asUSD != null) && (asUSD.isEmpty() == false)) {

								Exchange e = ExchangeProvider.getMarket(asUSD);
								String pair = e.getPairName(newCurrency.getCurrencyName(), "USD");
								SimpleDoubleProperty pairValue = e.getCurrentData(pair);
								nb = Bindings.multiply(
										e.isBase(pair, "USD") ? pairValue.divide(pairValue.multiply(pairValue))
												: pairValue,
										newCurrency.getAmountProperty());
								if (pairValue.get() != Utils.LOADING_VALUE) {
									newCurrency.setAsUSD(nb.getValue().doubleValue());
								}
								listener = new InvalidationListener() {

									@Override
									public void invalidated(Observable observable) {
										newCurrency.setAsUSD(nb.getValue().doubleValue());
									}
								};
								nb.addListener(listener);
							}
						}
					});

					newCurrency.getMarketBTCProperty().addListener(new ChangeListener<String>() {
						private NumberBinding nb = null;
						private InvalidationListener listener = null;

						@Override
						public void changed(ObservableValue<? extends String> observable, String oldValue,
								String newValue) {
							if (nb != null) {
								nb.removeListener(listener);
							}
							String asBTC = newCurrency.getMarketBTC();
							if ((asBTC != null) && (asBTC.isEmpty() == false)) {

								Exchange e = ExchangeProvider.getMarket(asBTC);
								String pair = e.getPairName(newCurrency.getCurrencyName(), "BTC");
								SimpleDoubleProperty pairValue = e.getCurrentData(pair);
								nb = Bindings.multiply(
										e.isBase(pair, "BTC") ? pairValue.divide(pairValue.multiply(pairValue))
												: pairValue,
										newCurrency.getAmountProperty());
								if (pairValue.get() != Utils.LOADING_VALUE) {
									newCurrency.setAsBTC(nb.getValue().doubleValue());
								}
								listener = new InvalidationListener() {

									@Override
									public void invalidated(Observable observable) {
										newCurrency.setAsBTC(nb.getValue().doubleValue());
									}
								};
								nb.addListener(listener);
							}
						}
					});

					newCurrency.getMarketETHProperty().addListener(new ChangeListener<String>() {
						private NumberBinding nb = null;
						private InvalidationListener listener = null;

						@Override
						public void changed(ObservableValue<? extends String> observable, String oldValue,
								String newValue) {
							if (nb != null) {
								nb.removeListener(listener);
							}
							String asETH = newCurrency.getMarketETH();
							if ((asETH != null) && (asETH.isEmpty() == false)) {

								Exchange e = ExchangeProvider.getMarket(asETH);
								String pair = e.getPairName(newCurrency.getCurrencyName(), "ETH");
								SimpleDoubleProperty pairValue = e.getCurrentData(pair);
								nb = Bindings.multiply(
										e.isBase(pair, "ETH") ? pairValue.divide(pairValue.multiply(pairValue))
												: pairValue,
										newCurrency.getAmountProperty());
								if (pairValue.get() != Utils.LOADING_VALUE) {
									newCurrency.setAsETH(nb.getValue().doubleValue());
								}
								listener = new InvalidationListener() {

									@Override
									public void invalidated(Observable observable) {
										newCurrency.setAsETH(nb.getValue().doubleValue());
									}
								};
								nb.addListener(listener);
							}
						}
					});

					newCurrency.setAmount(entr.getValue());
					assets.add(newCurrency);
					pie.add(newCurrency);
				}
			}
		}
	}
}
