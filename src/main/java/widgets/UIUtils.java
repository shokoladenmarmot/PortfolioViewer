package widgets;

import core.Utils;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

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
						if (item.equals(Utils.INVALID_VALUE)) {
							setText(null);
							pi.setPrefWidth(15);
							pi.setPrefHeight(15);
							setGraphic(pi);
						} else {
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
						if (item.equals(Utils.INVALID_VALUE)) {
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

}
