package controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import core.Utils;
import core.Order;
import core.XMLFactory;
import core.TradeLibrary;
import exchanges.Exchange;
import exchanges.Exchange.Status;
import exchanges.ExchangeProvider;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class ViewController implements Initializable {

	private static final Logger LOGGER = Logger.getLogger(ViewController.class.getName());

	@FXML
	private VBox operationalLayout;

	@FXML
	private Button addButton;

	@FXML
	private Button clearButton;

	@FXML
	private Button saveButton;

	@FXML
	private Button loadButton;

	private Map<String, TableView> recordsMap;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		recordsMap = new HashMap<String, TableView>();
	}

	public void addNewTrade(ActionEvent ae) {

		// TODO: Move code below to a FXML + Controller file.
		Dialog<Order> dialog = new Dialog<Order>();
		dialog.setTitle("Add New Trade");

		{
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			// grid.setPadding(new Insets(20, 150, 10, 10));

			// Collect all symbols from all markets
			ObservableList<String> unsorderList = FXCollections.observableArrayList();
			SortedList<String> currencies = new SortedList<String>(unsorderList);
			currencies.setComparator(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});

			for (ExchangeProvider ep : ExchangeProvider.values()) {
				new Thread(() -> {
					while (ep.getInstance().getStatus() != Status.READY) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							LOGGER.info("Waiting on \"" + ep.getInstance().getName()
									+ "\" to initialize. Thread.sleep(1sec)");
							e.printStackTrace();
						}
					}
					synchronized (this) {
						unsorderList.addAll(ep.getInstance().getAvailableCurrency());
					}
				}).start();
			}

			ComboBox<String> fromCmb = new ComboBox<String>();
			ComboBox<String> toCmb = new ComboBox<String>();
			ComboBox<String> market = new ComboBox<String>();
			TextField fromAmount = new TextField();
			TextField toAmount = new TextField();
			DatePicker dp = new DatePicker(LocalDate.now());

			fromCmb.valueProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					toCmb.getItems().clear();
					Set<String> toCurrencies = new TreeSet<String>();

					for (ExchangeProvider ep : ExchangeProvider.values()) {
						toCurrencies.addAll(ep.getInstance().getCurrencyFromCurrency(newValue).stream()
								.map(p -> p.getKey()).collect(Collectors.toList()));
					}
					toCmb.getItems().addAll(toCurrencies);
				}

			});

			toCmb.valueProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					market.getItems().clear();

					List<String> pairs = new LinkedList<String>();

					for (ExchangeProvider ep : ExchangeProvider.values()) {
						String symbol = ep.getInstance().getPairName(fromCmb.getValue(), toCmb.getValue());
						if (symbol != null) {
							pairs.add(ep.getInstance().getName());
						}
					}
					market.getItems().addAll(pairs);
				}
			});

			fromAmount.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					if (!newValue.matches(Utils.doubleExpression)) {
						fromAmount.setText(oldValue);
					}
				}
			});

			toAmount.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					if (!newValue.matches(Utils.doubleExpression)) {
						toAmount.setText(oldValue);
					}
				}
			});

			dp.setShowWeekNumbers(false);
			final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
				@Override
				public DateCell call(final DatePicker datePicker) {
					return new DateCell() {
						@Override
						public void updateItem(LocalDate item, boolean empty) {
							super.updateItem(item, empty);

							if (item.isAfter(LocalDate.now())) {
								setDisable(true);
							}
						}
					};
				}
			};
			dp.setDayCellFactory(dayCellFactory);

			// TODO : Editable + autocomplete
			fromCmb.setItems(currencies);

			// Build the layout
			grid.add(new Label("From: "), 0, 0);
			HBox fromLout = new HBox();
			fromLout.getChildren().add(fromCmb);
			grid.add(fromLout, 1, 0);
			grid.add(new Label("To: "), 0, 1);
			grid.add(toCmb, 1, 1);
			grid.add(new Label("Amount Sold: "), 0, 2);
			grid.add(fromAmount, 1, 2);
			grid.add(new Label("Amount Recieved: "), 0, 3);
			grid.add(toAmount, 1, 3);
			grid.add(new Label("Market: "), 0, 4);
			grid.add(market, 1, 4);
			grid.add(new Label("Date: "), 0, 5);
			grid.add(dp, 1, 5);

			dialog.getDialogPane().setContent(grid);

			ButtonType logButtonType = new ButtonType("Add", ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(logButtonType, ButtonType.CANCEL);

			final Button logButton = (Button) dialog.getDialogPane().lookupButton(logButtonType);
			logButton.addEventFilter(ActionEvent.ACTION, event -> {

				String fromVal = fromCmb.getValue();
				String toVal = toCmb.getValue();
				String fromAmountVal = fromAmount.getText();
				String toAmountVal = toAmount.getText();
				String marketVal = market.getValue();
				LocalDate dpbVal = dp.getValue();

				boolean isValid = (Objects.nonNull(fromVal) && !fromVal.isEmpty() && Objects.nonNull(toVal)
						&& !toVal.isEmpty() && Objects.nonNull(fromAmountVal) && !fromAmountVal.isEmpty()
						&& Objects.nonNull(toAmountVal) && !toAmountVal.isEmpty() && Objects.nonNull(marketVal)
						&& !marketVal.isEmpty() && Objects.nonNull(dpbVal));

				if (!isValid) {
					event.consume();
				}
			});

			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == logButtonType) {
					for (ExchangeProvider ep : ExchangeProvider.values()) {
						if (ep.getInstance().getName().equals(market.getValue())) {
							String symbol = ep.getInstance().getPairName(fromCmb.getValue(), toCmb.getValue());
							// NOTE: Divide the time by 1000 to get result in seconds
							return new Order(symbol, market.getValue(), fromCmb.getValue(), toCmb.getValue(),
									Double.parseDouble(fromAmount.getText()), Double.parseDouble(toAmount.getText()),
									java.sql.Date.valueOf(dp.getValue()).getTime() / 1000);
						}
					}
				}
				return null;
			});
		}

		Optional<Order> result = dialog.showAndWait();
		if (result.isPresent()) {
			Order o = result.get();
			TradeLibrary.getInstance().addOrder(o);
			addNewOrder(o);
		}
		ae.consume();
	}

	public void saveTemplate(ActionEvent ae) {

		XMLFactory.saveLibraryToXML();
		ae.consume();
	}

	public void loadTemplate(ActionEvent ae) {

		XMLFactory.loadOrderListFromXML();

		for (Order o : TradeLibrary.getInstance().getOrders()) {
			addNewOrder(o);
		}
		ae.consume();
	}

	public void clearTemplate(ActionEvent ae) {

		TradeLibrary.getInstance().clearLibrary();
		operationalLayout.getChildren().clear();
		recordsMap.clear();
		ae.consume();
	}

	private void addNewOrder(Order newOrder) {

		TableView[] tables = new TableView[2];
		tables[0] = recordsMap.get(newOrder.getFrom());
		tables[1] = recordsMap.get(newOrder.getTo());

		if (tables[0] == null) {
			tables[0] = createNewTableForOrder(newOrder.getFrom(), newOrder);
		}

		if (tables[1] == null) {
			tables[1] = createNewTableForOrder(newOrder.getTo(), newOrder);
		}

		tables[0].getItems().add(newOrder);
		tables[1].getItems().add(newOrder);
	}

	private TableView createNewTableForOrder(String tableOwner, Order o) {
		GridPane grid = new GridPane();
		TableView<Order> table = new TableView<>();

		table.setEditable(false);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		// TODO put hints for headers
		TableColumn<Order, ImageView> orderType = new TableColumn<>("Type");
		orderType.setCellValueFactory(new Callback<CellDataFeatures<Order, ImageView>, ObservableValue<ImageView>>() {
			public ObservableValue<ImageView> call(CellDataFeatures<Order, ImageView> data) {
				if (data.getValue().getFrom().equals(tableOwner)) {
					return new SimpleObjectProperty<ImageView>(new ImageView(
							new Image(getClass().getResourceAsStream("/icons/down.png"), 16, 16, true, true)));
				} else {
					return new SimpleObjectProperty<ImageView>(new ImageView(
							new Image(getClass().getResourceAsStream("/icons/up.png"), 16, 16, true, true)));
				}
			}
		});

		orderType.setStyle("-fx-alignment: CENTER;");
		orderType.setResizable(false);

		TableColumn<Order, String> amountCurrent = new TableColumn<>("Amount");
		amountCurrent.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
				return new SimpleStringProperty(Utils.decimalEightSymbols
						.format((data.getValue().getFrom().equals(tableOwner)) ? data.getValue().getAmountSpend()
								: data.getValue().getAmountRecieved()));
			}
		});
		amountCurrent.setStyle("-fx-alignment: CENTER;");
		amountCurrent.setResizable(false);

		TableColumn<Order, String> symbol = new TableColumn<>("For");
		symbol.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
				return new SimpleStringProperty(data.getValue().getFrom().equals(tableOwner) ? data.getValue().getTo()
						: data.getValue().getFrom());
			}
		});
		symbol.setStyle("-fx-alignment: CENTER;");
		symbol.setResizable(false);

		TableColumn<Order, String> amountSymbol = new TableColumn<>("Amount");
		amountSymbol.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
				return new SimpleStringProperty(Utils.decimalEightSymbols
						.format(data.getValue().getFrom().equals(tableOwner) ? data.getValue().getAmountRecieved()
								: data.getValue().getAmountSpend()));
			}
		});
		amountSymbol.setStyle("-fx-alignment: CENTER;");
		amountSymbol.setResizable(false);

		TableColumn<Order, String> price = new TableColumn<>("Price");
		price.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
				return new SimpleStringProperty(Utils.decimalEightSymbols
						.format((data.getValue().getPrice(data.getValue().getFrom().equals(tableOwner)))));
			}
		});

		price.setStyle("-fx-alignment: CENTER;");
		price.setResizable(false);

		TableColumn<Order, String> exchange = new TableColumn<>("Market");
		exchange.setCellValueFactory(new PropertyValueFactory<Order, String>("market"));
		exchange.setStyle("-fx-alignment: CENTER;");
		exchange.setResizable(false);

		TableColumn<Order, String> date = new TableColumn<>("Date");
		date.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getDateToString()));
		date.setStyle("-fx-alignment: CENTER;");
		date.setResizable(false);

		TableColumn<Order, String> current = new TableColumn<>("Current Price");
		current.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {

			private void evaluate(Order order, Number d, SimpleStringProperty val) {
				Exchange e = ExchangeProvider.getMarket(order.getMarket());
				if (e != null) {
					if (e.isBase(order.getSymbol(), tableOwner)) {
						val.set(Utils.decimalEightSymbols.format(d.doubleValue()));
					} else {
						val.set(Utils.decimalEightSymbols.format(((Double) (1 / d.doubleValue()))));
					}
				}
			}

			public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
				SimpleStringProperty val = new SimpleStringProperty(Exchange.INVALID_VALUE.toString());

				SimpleDoubleProperty d = Arrays.asList(ExchangeProvider.values()).stream()
						.filter(e -> e.getInstance().getName().equals(data.getValue().getMarket())).findFirst().get()
						.getInstance().getCurrentData(data.getValue().getSymbol());

				if (d.getValue().doubleValue() != Exchange.INVALID_VALUE)
					evaluate(data.getValue(), d.getValue(), val);

				d.addListener(new ChangeListener<Number>() {

					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue,
							Number newValue) {
						evaluate(data.getValue(), newValue, val);
					}
				});
				return val;
			}
		});

		current.setCellFactory(tc -> new TableCell<Order, String>() {
			private final ProgressIndicator pi = new ProgressIndicator();

			@Override
			protected void updateItem(final String item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null) {
					setText(null);
					setGraphic(null);
					return;
				}
				if (item.equals(Exchange.INVALID_VALUE.toString())) {
					setText(null);
					pi.setPrefWidth(15);
					pi.setPrefHeight(15);
					setGraphic(pi);
				} else {
					setText(item.toString());
					setGraphic(null);
				}
			}
		});
		current.setStyle("-fx-alignment: CENTER;");
		current.setResizable(false);

		TableColumn<Order, String> current_compared = new TableColumn<>("Initial-Current");
		current_compared.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {

			private final void evaluate(Order order, Number d, SimpleStringProperty val) {
				Exchange e = ExchangeProvider.getMarket(order.getMarket());

				if (e != null) {
					double res = 1;
					if (e.isBase(order.getSymbol(), tableOwner)) {
						res *= d.doubleValue();
					} else {
						res /= d.doubleValue();
					}

					if (order.getFrom().equals(tableOwner)) {
						val.set(Utils.decimalEightSymbols.format(order.getPrice(true) / res));
					} else {
						val.set(Utils.decimalEightSymbols.format(res / order.getPrice(false)));
					}
				}
			};

			public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
				SimpleStringProperty val = new SimpleStringProperty(Exchange.INVALID_VALUE.toString());

				ObservableValue<Number> n = Arrays.asList(ExchangeProvider.values()).stream()
						.filter(e -> e.getInstance().getName().equals(data.getValue().getMarket())).findFirst().get()
						.getInstance().getCurrentData(data.getValue().getSymbol());

				if (n.getValue().doubleValue() != Exchange.INVALID_VALUE)
					evaluate(data.getValue(), n.getValue(), val);

				n.addListener(new ChangeListener<Number>() {

					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue,
							Number newValue) {
						evaluate(data.getValue(), newValue, val);
					}
				});

				return val;
			}
		});

		current_compared.setCellFactory(tc -> new TableCell<Order, String>() {
			private final ProgressIndicator pi = new ProgressIndicator();

			@Override
			protected void updateItem(final String item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null) {
					setText(null);
					setGraphic(null);
					return;
				}
				if (item.equals(Exchange.INVALID_VALUE.toString())) {
					setText(null);
					pi.setPrefWidth(15);
					pi.setPrefHeight(15);
					setGraphic(pi);
				} else {
					double val = 1 - Double.parseDouble(item);
					if (val > 0) {
						setStyle("-fx-text-fill: red;");
					} else {
						setStyle("-fx-text-fill: green;");
					}
					val *= -1;

					setText(Utils.decimalTwoSymbols.format(val * 100) + "%");
					setGraphic(null);
				}
			}
		});
		current_compared.setStyle("-fx-alignment: CENTER;");
		current_compared.setResizable(false);

		table.getColumns().addAll(orderType, amountCurrent, symbol, amountSymbol, price, exchange, date, current,
				current_compared);

		ScrollPane sp = new ScrollPane();
		sp.setFitToWidth(true);
		sp.setContent(table);
		// sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);

		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.add(new Label(tableOwner), 0, 0);
		grid.add(sp, 0, 1);

		operationalLayout.getChildren().add(grid);
		recordsMap.put(tableOwner, table);
		return table;
	}
}
