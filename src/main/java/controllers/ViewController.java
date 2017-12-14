package controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import core.Order;
import core.Order.OrderType;
import exchanges.ExchangeProvider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void addNewTrade(ActionEvent ae) {

		Dialog<Order> dialog = new Dialog<Order>();
		dialog.setTitle("Add New Trade");

		{
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			// grid.setPadding(new Insets(20, 150, 10, 10));

			// Create symbol combobox
			Set<String> symbolList = new HashSet<String>();
			boolean isEmpty = symbolList.isEmpty();
			while (isEmpty) {
				for (ExchangeProvider ep : ExchangeProvider.values()) {
					symbolList.addAll(ep.getInstance().getAvailablePairList());
				}
				isEmpty = symbolList.isEmpty();
				if (isEmpty) {
					LOGGER.info("Getting symbol list from exchanges.");
				}
			}

			ComboBox<String> symbolCmb = new ComboBox<String>();
			// TODO : Editable + autocomplete
			symbolCmb.getItems().addAll(symbolList);

			ComboBox<OrderType> typeCmb = new ComboBox<OrderType>();
			typeCmb.getItems().addAll(OrderType.values());

			TextField amount = new TextField();
			amount.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					if (!newValue.matches("\\d*\\.?\\d*")) {
						amount.setText(oldValue);
					}
				}
			});

			DatePicker dp = new DatePicker(LocalDate.now());
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

			// Build the layout
			grid.add(new Label("Symbol: "), 0, 0);
			grid.add(symbolCmb, 1, 0);
			grid.add(new Label("Type: "), 0, 1);
			grid.add(typeCmb, 1, 1);
			grid.add(new Label("Amount: "), 0, 2);
			grid.add(amount, 1, 2);
			grid.add(new Label("Date: "), 0, 3);
			grid.add(dp, 1, 3);

			dialog.getDialogPane().setContent(grid);

			ButtonType logButtonType = new ButtonType("Add", ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(logButtonType, ButtonType.CANCEL);

			final Button logButton = (Button) dialog.getDialogPane().lookupButton(logButtonType);
			logButton.addEventFilter(ActionEvent.ACTION, event -> {

				String symVal = symbolCmb.getValue();
				String amountVal = amount.getText();
				OrderType typeVal = typeCmb.getValue();
				LocalDate dpbVal = dp.getValue();

				boolean isValid = (Objects.nonNull(symVal) && !symVal.isEmpty() && Objects.nonNull(amountVal)
						&& !amountVal.isEmpty() && Objects.nonNull(typeVal) && Objects.nonNull(dpbVal));

				if (!isValid) {
					event.consume();
				}
			});

			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == logButtonType) {
					return new Order(symbolCmb.getValue(), Double.parseDouble(amount.getText()), typeCmb.getValue(),
							java.sql.Date.valueOf(dp.getValue()));
				}
				return null;
			});
		}

		Optional<Order> result = dialog.showAndWait();
		if (result.isPresent()) {
			createLayoutFromOrder(result.get());
		}
		ae.consume();
	}

	public void clearTemplate(ActionEvent ae) {
		ae.consume();
	}

	public void saveTemplate(ActionEvent ae) {
		ae.consume();
	}

	private void createLayoutFromOrder(Order newOrder) {
		LOGGER.info("Creating layout for order: " + newOrder.toString());

		// TODO Layout?

	}
}
