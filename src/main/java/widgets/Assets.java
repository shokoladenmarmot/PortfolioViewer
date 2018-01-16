package widgets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import core.Order;
import core.TradeLibrary;
import core.Utils;
import exchanges.Exchange;
import exchanges.ExchangeProvider;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class Assets extends VBox {

	private static final Logger LOGGER = Logger.getLogger(Assets.class.getName());

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

			marketUSD = new SimpleStringProperty();
			marketBTC = new SimpleStringProperty();
			marketETH = new SimpleStringProperty();

			marketUSD.addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

					String asUSD = getMarketUSD();
					if ((asUSD != null) && (asUSD.isEmpty() == false)) {

						Exchange e = ExchangeProvider.getMarket(asUSD);
						getAsUSDProperty().unbind();
						setAsUSD(Utils.LOADING_VALUE);
						ObservableNumberValue pairValue = e.getValue("USD", name);
						pairValue = Bindings.multiply(pairValue, getAmountProperty());
						getAsUSDProperty().bind(pairValue);
					}
				}
			});

			marketBTC.addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					String asBTC = getMarketBTC();
					if ((asBTC != null) && (asBTC.isEmpty() == false)) {

						Exchange e = ExchangeProvider.getMarket(asBTC);
						getAsBTCProperty().unbind();
						setAsBTC(Utils.LOADING_VALUE);
						ObservableNumberValue pairValue = e.getValue("BTC", name);
						pairValue = Bindings.multiply(pairValue, getAmountProperty());
						getAsBTCProperty().bind(pairValue);
					}
				}
			});

			marketETH.addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

					String asETH = getMarketETH();
					if ((asETH != null) && (asETH.isEmpty() == false)) {

						Exchange e = ExchangeProvider.getMarket(asETH);
						getAsETHProperty().unbind();
						setAsETH(Utils.LOADING_VALUE);
						ObservableNumberValue pairValue = e.getValue("ETH", name);
						pairValue = Bindings.multiply(pairValue, getAmountProperty());
						getAsETHProperty().bind(pairValue);
					}
				}
			});

			// Get initial values for markets
			new Thread(() -> {

				for (ExchangeProvider ep : ExchangeProvider.values()) {
					Exchange e = ep.getInstance();
					e.invokeWhenStatusIsReady(new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							if ((currencyName.equals("USD") == false) && marketUSD.get() == null) {
								if (e.getAvailableCurrency().contains("USD")) {
									marketUSD.setValue(e.getName());
								}
							}
							if ((currencyName.equals("BTC") == false) && marketBTC.get() == null) {
								if (e.getAvailableCurrency().contains("BTC")) {
									marketBTC.setValue(e.getName());
								}
							}
							if ((currencyName.equals("ETH") == false) && marketETH.get() == null) {
								if (e.getAvailableCurrency().contains("ETH")) {
									marketETH.setValue(e.getName());
								}
							}
							return null;
						}
					});
				}
			}).start();
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

		@Override
		public String toString() {
			return "Currency: " + currencyName + "\nAmount: " + amount.getValue();
		}
	}

	private AssetsPieChart pie;

	private Label totalUSD;
	private Label totalBTC;
	private Label totalETH;

	private ObservableList<Currency> assets;

	public Assets() {
		assets = FXCollections.observableArrayList();
		setAlignment(Pos.CENTER);
		setPadding(new Insets(10, 5, 10, 5));

		init();
	}

	private void init() {
		pie = new AssetsPieChart();
		totalUSD = new Label();
		totalBTC = new Label();
		totalETH = new Label();

		TradeLibrary.getInstance().getOrders().addListener(new ListChangeListener<Order>() {

			@Override
			public void onChanged(Change<? extends Order> c) {
				update();
			}
		});

		TableView<Currency> currencyTable = new TableView<Currency>(assets);
		currencyTable.setMinHeight(100);
		currencyTable.setEditable(true);
		currencyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		currencyTable.setPlaceholder(new Label(""));

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

		GridPane tottalLout = new GridPane();
		tottalLout.setHgap(25);
		tottalLout.setVgap(10);

		tottalLout.add(new Label("Total USD:"), 0, 0);
		tottalLout.add(totalUSD, 1, 0);
		tottalLout.add(new Label("Total BTC:"), 0, 1);
		tottalLout.add(totalBTC, 1, 1);
		tottalLout.add(new Label("Total ETH:"), 0, 2);
		tottalLout.add(totalETH, 1, 2);

		for (Node n : tottalLout.getChildren()) {
			n.getStyleClass().add("total-assets-label");
		}

		HBox tp = new HBox(40);
		tp.setAlignment(Pos.BOTTOM_RIGHT);
		tp.setPadding(new Insets(25, 0, 0, 0));
		tp.getChildren().addAll(tottalLout, pie/* , bar, area */);

		HBox.setHgrow(pie, Priority.ALWAYS);
		VBox.setVgrow(tp, Priority.ALWAYS);

		getChildren().addAll(tp, currencyTable);
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

		// assets.retainAll(listToRetain.stream().filter(a -> a.getAmount() >
		// 0).collect(Collectors.toSet()));
		assets.retainAll(listToRetain);
		pie.cleanByRetainingOnly(values.keySet());

		for (Entry<String, Double> entr : values.entrySet()) {

			// Don't add currencies which balance is currently 0
			if (entr.getValue() > 0) {

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
					pie.add(newCurrency);
				}
			}
		}

		Callable<Void> eval = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				Platform.runLater(() -> {
					double usdAmount = 0;
					double btcAmount = 0;
					double ethAmount = 0;

					for (Currency c : assets) {
						usdAmount += Math.max(c.getAsUSD(), 0);
						btcAmount += Math.max(c.getAsBTC(), 0);
						ethAmount += Math.max(c.getAsETH(), 0);
					}

					totalUSD.setText(Utils.decimalTwoSymbols.format(usdAmount));
					totalBTC.setText(Utils.decimalEightSymbols.format(btcAmount));
					totalETH.setText(Utils.decimalEightSymbols.format(ethAmount));
				});
				return null;
			}

		};
		for (Currency c : assets) {
			c.getAsUSDProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					try {
						eval.call();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			c.getAsETHProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					try {
						eval.call();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			c.getAsBTCProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					try {
						eval.call();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		}
	}
}
