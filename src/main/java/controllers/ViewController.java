package controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import Start.Main;
import core.Utils;
import core.Order;
import core.XMLFactory;
import core.TradeLibrary;
import fxml.UIPage;
import exchanges.Exchange;
import exchanges.ExchangeProvider;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class ViewController implements Initializable {

	private static final Logger LOGGER = Logger.getLogger(ViewController.class.getName());

	@Override
	public void initialize(URL location, ResourceBundle resources) {
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
			ObservableList<String> unsortedList = FXCollections.observableArrayList();
			SortedList<String> sortedVersion = new SortedList<>(unsortedList, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}

			});
			Set<String> tempSet = new HashSet<String>();

			for (ExchangeProvider ep : ExchangeProvider.values()) {
				Exchange e = ep.getInstance();
				e.invokeWhenStatusIsReady(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						Platform.runLater(() -> {
							synchronized (unsortedList) {
								tempSet.addAll(ep.getInstance().getAvailableCurrency());
								unsortedList.setAll(tempSet);
							}
						});
						return null;
					}
				});
			}

			ComboBox<String> fromCmb = new ComboBox<String>();
			fromCmb.setEditable(true);
			ComboBox<String> toCmb = new ComboBox<String>();
			toCmb.setEditable(true);
			ComboBox<String> market = new ComboBox<String>();
			market.setEditable(true);
			TextField fromAmount = new TextField();
			TextField toAmount = new TextField();
			DatePicker dp = new DatePicker(LocalDate.now());

			fromCmb.valueProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					toCmb.getItems().clear();
					Set<String> toCurrencies = new TreeSet<String>();

					for (ExchangeProvider ep : ExchangeProvider.values()) {
						toCurrencies.addAll(ep.getInstance().getPairsForCurrency(newValue));
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
			fromCmb.setItems(sortedVersion);

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
					String symbol = fromCmb.getValue() + "-" + toCmb.getValue();
					for (ExchangeProvider ep : ExchangeProvider.values()) {
						if (ep.getInstance().getName().equals(market.getValue())) {
							symbol = ep.getInstance().getPairName(fromCmb.getValue(), toCmb.getValue());
							break;
						}
					}
					// NOTE: Divide the time by 1000 to get result in seconds
					return new Order(symbol, market.getValue(), fromCmb.getValue(), toCmb.getValue(),
							Double.parseDouble(fromAmount.getText()), Double.parseDouble(toAmount.getText()),
							java.sql.Date.valueOf(dp.getValue()).getTime() / 1000);

				}
				return null;
			});
		}

		Optional<Order> result = dialog.showAndWait();
		if (result.isPresent()) {
			Order o = result.get();
			TradeLibrary.getInstance().addOrder(o);
		}
		ae.consume();
	}

	public void saveTemplate(ActionEvent ae) {

		XMLFactory.saveLibraryToXML();
		ae.consume();
	}

	public void loadTemplate(ActionEvent ae) {

		XMLFactory.loadOrderListFromXML();
		ae.consume();
	}

	public void clearTemplate(ActionEvent ae) {

		TradeLibrary.getInstance().clearLibrary();
		ae.consume();
	}

	public void backToMain(ActionEvent ae) {
		Main.getInstance().changeScene(UIPage.Page.START);
		ae.consume();
	}
}
