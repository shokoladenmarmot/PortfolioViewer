package widgets;

import core.Utils;
import exchanges.Exchange;
import exchanges.ExchangeProvider;
import exchanges.Exchange.Status;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Callback;
import widgets.Assets.Currency;

public class UIUtils {

	public static <T> void setNumberCellFactory(TableColumn<T, Number> column) {
		column.setCellFactory(new Callback<TableColumn<T, Number>, TableCell<T, Number>>() {
			@Override
			public TableCell<T, Number> call(TableColumn<T, Number> param) {
				return new TableCell<T, Number>() {
					private final ProgressIndicator pi = new ProgressIndicator();
					
					@Override
					protected void updateItem(final Number item, boolean empty) {
						super.updateItem(item, empty);
						if (item == null) {
							setText(null);
							setGraphic(null);
							return;
						}
						if (item.equals(Utils.LOADING_VALUE)) {
							setText(null);
							pi.setPrefWidth(15);
							pi.setPrefHeight(15);
							setGraphic(pi);
						} else {
							if (item.doubleValue() < 0) {
//								setTextFill(Paint.valueOf("red"));
							} else {
								//setTextFill(Paint.valueOf("black"));
							}
							setText(Utils.decimalEightSymbols.format(item));
							setGraphic(null);
						}
					}
				};
			}
		});
	}

	public static <T> void setPercentCellFactory(TableColumn<T, Number> column) {
		column.setCellFactory(new Callback<TableColumn<T, Number>, TableCell<T, Number>>() {
			@Override
			public TableCell<T, Number> call(TableColumn<T, Number> param) {
				return new TableCell<T, Number>() {
					private final ProgressIndicator pi = new ProgressIndicator();

					@Override
					protected void updateItem(final Number item, boolean empty) {
						super.updateItem(item, empty);
						if (item == null) {
							setText(null);
							setGraphic(null);
							return;
						}
						if (item.equals(Utils.LOADING_VALUE)) {
							setText(null);
							pi.setPrefWidth(15);
							pi.setPrefHeight(15);
							setGraphic(pi);
						} else {
							double val = 1 - item.doubleValue();
							if (val > 0) {
								setStyle("-fx-alignment: CENTER;-fx-text-fill: red;");
							} else {
								setStyle("-fx-alignment: CENTER;-fx-text-fill: green;");
							}
							val *= -1;

							setText(Utils.decimalTwoSymbols.format(val * 100) + "%");
							setGraphic(null);
						}
					}
				};
			}
		});
	}

	public static void setComboCellFactoryForMarket(TableColumn<Currency, String> column, String currencySymbol) {
		column.setCellFactory(new Callback<TableColumn<Currency, String>, TableCell<Currency, String>>() {
			@Override
			public TableCell<Currency, String> call(TableColumn<Currency, String> param) {

				return new ComboBoxTableCell<Currency, String>() {
					@Override
					public void startEdit() {
						Currency current = (Currency) getTableRow().getItem();
						if (!current.getCurrencyName().equals(currencySymbol)) {
							ObservableList<String> newValues = FXCollections.observableArrayList();

							for (ExchangeProvider ep : ExchangeProvider.values()) {
								Exchange e = ep.getInstance();
								if (e.getStatus() == Status.READY) {
									if (e.getPairName(current.getCurrencyName(), currencySymbol) != null) {
										newValues.add(e.getName());
									}
								}
							}
							getItems().setAll(newValues);
							super.startEdit();
						}
					}

					@Override
					public void updateItem(String item, boolean empty) {

						Currency current = (Currency) getTableRow().getItem();
						if (current != null) {
							if (current.getCurrencyName().equals(currencySymbol)) {
//								setStyle("-fx-background-color: gray;");
							} else {
//								setStyle("");
							}
						}
						super.updateItem(item, empty);
					}
				};

			}
		});
	}
}
