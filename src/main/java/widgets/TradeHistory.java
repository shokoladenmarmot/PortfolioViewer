package widgets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import core.Order;
import core.TradeLibrary;
import core.Utils;
import exchanges.Exchange;
import exchanges.ExchangeProvider;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class TradeHistory extends ScrollPane {

	private static final Logger LOGGER = Logger.getLogger(TradeHistory.class.getName());

	private VBox operationalVbox;
	private Map<String, TableView<Order>> recordsMap;

	public TradeHistory() {

		recordsMap = new HashMap<String, TableView<Order>>();

		TradeLibrary.getInstance().getOrders().addListener(new ListChangeListener<Order>() {

			@Override
			public void onChanged(Change<? extends Order> c) {
				c.next();
				update(c.getAddedSubList(), c.getRemoved());
			}
		});

		operationalVbox = new VBox(20);
		operationalVbox.setAlignment(Pos.CENTER);
		operationalVbox.setPadding(new Insets(10, 5, 10, 5));

		setContent(operationalVbox);
		setFitToWidth(true);
	}

	private void update(List<? extends Order> added, List<? extends Order> removed) {

		HashMap<String, Double> values = new HashMap<String, Double>();

		if (TradeLibrary.getInstance().getOrders().isEmpty()) {
			recordsMap.clear();
			operationalVbox.getChildren().clear();
		} else if (added.size() > 0) {
			for (Order o : added) {
				addNewOrder(o);
			}
		} else if (removed.size() > 0) {
			for (Order o : added) {
				removeOrder(o);
			}
		}
	}

	private void addNewOrder(Order order) {

		if (order.getFrom().equals(order.getTo())) {
			return;
		}

		TableView[] tables = new TableView[2];
		tables[0] = recordsMap.get(order.getFrom());
		tables[1] = recordsMap.get(order.getTo());

		if (tables[0] == null) {
			tables[0] = createNewTableForOrder(order.getFrom(), order);
		}

		if (tables[1] == null) {
			tables[1] = createNewTableForOrder(order.getTo(), order);
		}

		tables[0].getItems().add(order);
		tables[1].getItems().add(order);
	}

	private void removeOrder(Order order) {
		TableView[] tables = new TableView[2];
		tables[0] = recordsMap.get(order.getFrom());
		tables[1] = recordsMap.get(order.getTo());

		if (tables[0] != null) {
			tables[0].getItems().remove(order);

			if (tables[0].getItems().isEmpty()) {
				recordsMap.remove(order.getFrom());
				operationalVbox.getChildren().remove(tables[0].getParent());
			}
		}

		if (tables[1] != null) {
			tables[1].getItems().remove(order);

			if (tables[1].getItems().isEmpty()) {
				recordsMap.remove(order.getTo());
				operationalVbox.getChildren().remove(tables[1].getParent());
			}
		}
	}

	private TableView createNewTableForOrder(String tableOwner, Order o) {
		VBox lout = new VBox();
		lout.setAlignment(Pos.CENTER);

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

		TableColumn<Order, String> amountCurrent = new TableColumn<>("Amount");
		amountCurrent.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
				return new SimpleStringProperty(Utils.decimalEightSymbols
						.format((data.getValue().getFrom().equals(tableOwner)) ? data.getValue().getAmountSpend()
								: data.getValue().getAmountRecieved()));
			}
		});
		amountCurrent.setStyle("-fx-alignment: CENTER;");

		TableColumn<Order, String> symbol = new TableColumn<>("Currency");
		symbol.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
				return new SimpleStringProperty(data.getValue().getFrom().equals(tableOwner) ? data.getValue().getTo()
						: data.getValue().getFrom());
			}
		});
		symbol.setStyle("-fx-alignment: CENTER;");

		TableColumn<Order, String> amountSymbol = new TableColumn<>("Amount");
		amountSymbol.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
				return new SimpleStringProperty(Utils.decimalEightSymbols
						.format(data.getValue().getFrom().equals(tableOwner) ? data.getValue().getAmountRecieved()
								: data.getValue().getAmountSpend()));
			}
		});
		amountSymbol.setStyle("-fx-alignment: CENTER;");

		TableColumn<Order, String> exchanged = new TableColumn<>("Exchanged From/To");
		exchanged.getColumns().addAll(symbol, amountSymbol);

		TableColumn<Order, String> atDate = new TableColumn<>("Trade Price");
		atDate.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Order, String> data) {
				return new SimpleStringProperty(Utils.decimalEightSymbols
						.format((data.getValue().getPrice(data.getValue().getFrom().equals(tableOwner)))));
			}
		});
		atDate.setStyle("-fx-alignment: CENTER;");

		TableColumn<Order, String> exchange = new TableColumn<>("Market");
		exchange.setCellValueFactory(new PropertyValueFactory<Order, String>("market"));
		exchange.setStyle("-fx-alignment: CENTER;");

		TableColumn<Order, String> date = new TableColumn<>("Date");
		date.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getDateToString()));
		date.setStyle("-fx-alignment: CENTER;");

		TableColumn<Order, Number> current = new TableColumn<>("Current Price");
		current.setCellValueFactory(new Callback<CellDataFeatures<Order, Number>, ObservableValue<Number>>() {

			public ObservableValue<Number> call(CellDataFeatures<Order, Number> data) {
				SimpleDoubleProperty val = new SimpleDoubleProperty(Utils.LOADING_VALUE);

				Exchange e = ExchangeProvider.getMarket(data.getValue().getMarket());
				if (e != null) {
					e.invokeWhenStatusIsReady(new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							Order o = data.getValue();
							if (tableOwner.equals(o.getTo())) {
								val.bind(e.getValue(o.getFrom(), o.getTo()));
							} else {
								val.bind(e.getValue(o.getTo(), o.getFrom()));
							}
							return null;
						}
					});
				}
				return val;
			}
		});
		UIUtils.setNumberCellFactory(current);
		current.setStyle("-fx-alignment: CENTER;");

		TableColumn<Order, Number> current_compared = new TableColumn<>("Difference");
		current_compared.setCellValueFactory(new Callback<CellDataFeatures<Order, Number>, ObservableValue<Number>>() {

			public ObservableValue<Number> call(CellDataFeatures<Order, Number> data) {
				SimpleDoubleProperty val = new SimpleDoubleProperty(Utils.LOADING_VALUE);
				Exchange e = ExchangeProvider.getMarket(data.getValue().getMarket());

				if (e != null) {
					e.invokeWhenStatusIsReady(new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							Order o = data.getValue();
							ObservableNumberValue exchangeValue = null;

							if (tableOwner.equals(o.getTo())) {
								exchangeValue = e.getValue(o.getFrom(), o.getTo());
								exchangeValue = Bindings.divide(exchangeValue, o.getPrice(false));
							} else {
								exchangeValue = e.getValue(o.getTo(), o.getFrom());
								exchangeValue = Bindings.divide(o.getPrice(true), exchangeValue);
							}

							val.bind(exchangeValue);
							return null;
						}
					});
				}
				return val;
			}
		});
		UIUtils.setPercentCellFactory(current_compared);
		current_compared.setStyle("-fx-alignment: CENTER;");

		TableColumn<Order, String> price = new TableColumn<>("Price For 1 " + tableOwner);
		price.getColumns().addAll(atDate, current, current_compared);

		table.getColumns().addAll(orderType, amountCurrent, exchanged, date, exchange, price);

		Label tableLabel = new Label(tableOwner);
		tableLabel.getStyleClass().add("trade-history-table-label");

		lout.getChildren().addAll(tableLabel, table);
		operationalVbox.getChildren().add(lout);

		recordsMap.put(tableOwner, table);
		return table;
	}
}
