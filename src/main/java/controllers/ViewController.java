package controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

import core.Order;
import core.XMLFactory;
import core.TradeLibrary;
import exchanges.Exchange.Status;
import exchanges.ExchangeProvider;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
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

	private Map<String, GridPane> recordsMap;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		recordsMap = new HashMap<String, GridPane>();
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
			Set<String> allCurrencies = new HashSet<String>();

			// Might need somekind of a listener if a provider fails to return its
			// currencies.
			for (ExchangeProvider ep : ExchangeProvider.values()) {
				if (ep.getInstance().getStatus() != Status.READY) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LOGGER.info(
								"Waiting on \"" + ep.getInstance().getName() + "\" to initialize. Thread.sleep(1sec)");
						e.printStackTrace();
					}
				}
				allCurrencies.addAll(ep.getInstance().getAvailableCurrency());
			}

			Button refreshB = new Button();
			refreshB.setGraphic(
					new ImageView(new Image(getClass().getResourceAsStream("/icons/refresh.png"), 16, 16, true, true)));
			ComboBox<String> fromCmb = new ComboBox<String>();
			ComboBox<String> toCmb = new ComboBox<String>();
			ComboBox<String> market = new ComboBox<String>();
			TextField fromAmount = new TextField();
			TextField toAmount = new TextField();
			DatePicker dp = new DatePicker(LocalDate.now());

			refreshB.setOnAction(ev -> {
				fromCmb.getItems().clear();
				Set<String> currencies = new HashSet<String>();
				for (ExchangeProvider ep : ExchangeProvider.values()) {
					if (ep.getInstance().getStatus() != Status.READY) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							LOGGER.info("Waiting on \"" + ep.getInstance().getName()
									+ "\" to initialize. Thread.sleep(1sec)");
							e.printStackTrace();
						}
					}
					currencies.addAll(ep.getInstance().getAvailableCurrency());
				}
				fromCmb.getItems().addAll(currencies);
			});

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
					if (!newValue.matches("\\d*\\.?\\d*")) {
						fromAmount.setText(oldValue);
					}
				}
			});

			toAmount.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					if (!newValue.matches("\\d*\\.?\\d*")) {
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
			fromCmb.getItems().addAll(allCurrencies);

			// Build the layout
			grid.add(new Label("From: "), 0, 0);
			HBox fromLout = new HBox();
			fromLout.getChildren().add(fromCmb);
			fromLout.getChildren().add(refreshB);
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
		ae.consume();
	}

	private void addNewOrder(Order newOrder) {

		GridPane[] grids = new GridPane[2];
		grids[0] = recordsMap.get(newOrder.getFrom());
		grids[1] = recordsMap.get(newOrder.getTo());

		TableView<Order>[] tables = new TableView[2];

		if (grids[0] == null) {

			grids[0] = new GridPane();
			tables[0] = new TableView<>();

			TableView<Order> table = tables[0];
			table.setEditable(false);
			table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

			// TODO put hints for headers
			TableColumn<Order, ImageView> orderType = new TableColumn<>("Type");
			orderType.setCellValueFactory(
					new Callback<CellDataFeatures<Order, ImageView>, ObservableValue<ImageView>>() {
						public ObservableValue<ImageView> call(CellDataFeatures<Order, ImageView> data) {
							if (data.getValue().getFrom().equals(newOrder.getFrom())) {
								return new SimpleObjectProperty<ImageView>(new ImageView(new Image(
										getClass().getResourceAsStream("/icons/down.png"), 16, 16, true, true)));
							} else {
								return new SimpleObjectProperty<ImageView>(new ImageView(new Image(
										getClass().getResourceAsStream("/icons/up.png"), 16, 16, true, true)));
							}
						}
					});
			orderType.setStyle("-fx-alignment: CENTER;");
			orderType.setResizable(false);

			TableColumn<Order, Double> amountCurrent = new TableColumn<>("Amount");
			amountCurrent.setCellValueFactory(new Callback<CellDataFeatures<Order, Double>, ObservableValue<Double>>() {
				public ObservableValue<Double> call(CellDataFeatures<Order, Double> data) {

					if (data.getValue().getFrom().equals(newOrder.getFrom())) {
						return new SimpleDoubleProperty(data.getValue().getAmountSpend()).asObject();
					} else {
						return new SimpleDoubleProperty(data.getValue().getAmountRecieved()).asObject();
					}
				}
			});
			amountCurrent.setStyle("-fx-alignment: CENTER;");
			amountCurrent.setResizable(false);

			TableColumn<Order, String> symbol = new TableColumn<>("For");
			symbol.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
				public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
					if (data.getValue().getFrom().equals(newOrder.getFrom())) {
						return new SimpleStringProperty(data.getValue().getTo());
					} else {
						return new SimpleStringProperty(data.getValue().getFrom());
					}
				}
			});
			symbol.setStyle("-fx-alignment: CENTER;");
			symbol.setResizable(false);

			TableColumn<Order, Double> amountSymbol = new TableColumn<>("Amount");
			amountSymbol.setCellValueFactory(new Callback<CellDataFeatures<Order, Double>, ObservableValue<Double>>() {
				public ObservableValue<Double> call(CellDataFeatures<Order, Double> data) {
					if (data.getValue().getFrom().equals(newOrder.getFrom())) {
						return new SimpleDoubleProperty(data.getValue().getAmountRecieved()).asObject();
					} else {
						return new SimpleDoubleProperty(data.getValue().getAmountSpend()).asObject();
					}
				}
			});
			amountSymbol.setStyle("-fx-alignment: CENTER;");
			amountSymbol.setResizable(false);

			TableColumn<Order, String> price = new TableColumn<>("Price");
			price.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
				public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
					if (data.getValue().getFrom().equals(newOrder.getFrom())) {
						return Bindings.format("%.5f",
								(data.getValue().getAmountRecieved() / data.getValue().getAmountSpend()));
					} else {
						return Bindings.format("%.5f",
								(data.getValue().getAmountSpend() / data.getValue().getAmountRecieved()));
					}
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
				public ObservableValue<String> call(CellDataFeatures<Order, String> data) {

					Double val = Arrays.asList(ExchangeProvider.values()).stream()
							.filter(e -> e.getInstance().getName().equals(data.getValue().getMarket())).findFirst()
							.get().getInstance().getCurrentData(data.getValue().getSymbol());

					return Bindings.format("%.7f", val);
				}
			});
			current.setStyle("-fx-alignment: CENTER;");
			current.setResizable(false);

			table.getColumns().addAll(orderType, amountCurrent, symbol, amountSymbol, price, exchange, date, current);

			ScrollPane sp = new ScrollPane();
			sp.setFitToWidth(true);
			sp.setContent(table);
			sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);

			grids[0].setHgap(10);
			grids[0].setVgap(10);
			grids[0].setPadding(new Insets(10, 10, 10, 10));
			grids[0].add(new Label(newOrder.getFrom()), 0, 0);
			grids[0].add(sp, 0, 1);

			operationalLayout.getChildren().add(grids[0]);
			recordsMap.put(newOrder.getFrom(), grids[0]);
		} else {
			tables[0] = (TableView) grids[0].getChildren().get(1);
		}

		if (grids[1] == null) {

			grids[1] = new GridPane();
			tables[1] = new TableView<>();

			TableView<Order> table = tables[1];
			table.setEditable(false);
			table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

			// TODO put hints for headers
			TableColumn<Order, ImageView> orderType = new TableColumn<>("Type");
			orderType.setCellValueFactory(
					new Callback<CellDataFeatures<Order, ImageView>, ObservableValue<ImageView>>() {
						public ObservableValue<ImageView> call(CellDataFeatures<Order, ImageView> data) {
							if (data.getValue().getFrom().equals(newOrder.getFrom())) {
								return new SimpleObjectProperty<ImageView>(new ImageView(new Image(
										getClass().getResourceAsStream("/icons/up.png"), 16, 16, true, true)));
							} else {
								return new SimpleObjectProperty<ImageView>(new ImageView(new Image(
										getClass().getResourceAsStream("/icons/down.png"), 16, 16, true, true)));
							}
						}
					});
			orderType.setStyle("-fx-alignment: CENTER;");
			orderType.setResizable(false);

			TableColumn<Order, Double> amountCurrent = new TableColumn<>("Amount");
			amountCurrent.setCellValueFactory(new Callback<CellDataFeatures<Order, Double>, ObservableValue<Double>>() {
				public ObservableValue<Double> call(CellDataFeatures<Order, Double> data) {

					if (data.getValue().getFrom().equals(newOrder.getFrom())) {
						return new SimpleDoubleProperty(data.getValue().getAmountRecieved()).asObject();
					} else {
						return new SimpleDoubleProperty(data.getValue().getAmountSpend()).asObject();
					}
				}
			});
			amountCurrent.setStyle("-fx-alignment: CENTER;");
			amountCurrent.setResizable(false);

			TableColumn<Order, String> symbol = new TableColumn<>("For");
			symbol.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
				public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
					if (data.getValue().getFrom().equals(newOrder.getFrom())) {
						return new SimpleStringProperty(data.getValue().getFrom());
					} else {
						return new SimpleStringProperty(data.getValue().getTo());
					}
				}
			});
			symbol.setStyle("-fx-alignment: CENTER;");
			symbol.setResizable(false);

			TableColumn<Order, Double> amountSymbol = new TableColumn<>("Amount");
			amountSymbol.setCellValueFactory(new Callback<CellDataFeatures<Order, Double>, ObservableValue<Double>>() {
				public ObservableValue<Double> call(CellDataFeatures<Order, Double> data) {
					if (data.getValue().getFrom().equals(newOrder.getFrom())) {
						return new SimpleDoubleProperty(data.getValue().getAmountSpend()).asObject();
					} else {
						return new SimpleDoubleProperty(data.getValue().getAmountRecieved()).asObject();
					}
				}
			});

			TableColumn<Order, String> price = new TableColumn<>("Price");
			price.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
				public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
					if (data.getValue().getFrom().equals(newOrder.getFrom())) {
						return Bindings.format("%.5f",
								(data.getValue().getAmountSpend() / data.getValue().getAmountRecieved()));
					} else {
						return Bindings.format("%.5f",
								(data.getValue().getAmountRecieved() / data.getValue().getAmountSpend()));
					}
				}
			});
			price.setStyle("-fx-alignment: CENTER;");
			price.setResizable(false);

			amountSymbol.setStyle("-fx-alignment: CENTER;");
			amountSymbol.setResizable(false);

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
				public ObservableValue<String> call(CellDataFeatures<Order, String> data) {

					Double val = Arrays.asList(ExchangeProvider.values()).stream()
							.filter(e -> e.getInstance().getName().equals(data.getValue().getMarket())).findFirst()
							.get().getInstance().getCurrentData(data.getValue().getSymbol());

					return Bindings.format("%.7f", val);
				}
			});
			current.setStyle("-fx-alignment: CENTER;");
			current.setResizable(false);

			table.getColumns().addAll(orderType, amountCurrent, symbol, amountSymbol, price, exchange, date, current);

			ScrollPane sp = new ScrollPane();
			sp.setFitToWidth(true);
			sp.setContent(table);
			sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);

			grids[1].setHgap(10);
			grids[1].setVgap(10);
			grids[1].setPadding(new Insets(10, 10, 10, 10));

			grids[1].add(new Label(newOrder.getTo()), 0, 0);
			grids[1].add(sp, 0, 1);

			operationalLayout.getChildren().add(grids[1]);
			recordsMap.put(newOrder.getTo(), grids[1]);
		} else {
			tables[1] = (TableView<Order>) grids[1].getChildren().get(1);
		}
		tables[0].getItems().add(newOrder);
		tables[1].getItems().add(newOrder);
	}
}
