package controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import exchanges.Exchange;
import exchanges.ExchangeProvider;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.util.Callback;

public class ChartController implements Initializable {

	private static final Logger LOGGER = Logger.getLogger(ChartController.class.getName());

	enum Duration {
		ONE, THREE, SIX, CUSTOM
	}

	private Duration duration;
	private Exchange exchange;

	// Cache?

	@FXML
	private LineChart<String, Double> btcchart;

	@FXML
	private CategoryAxis xAxis;

	@FXML
	private NumberAxis yAxis;

	@FXML
	private ComboBox<String> exchangeCmb;

	@FXML
	private Button refresh;

	@FXML
	private RadioButton months1Rd;

	@FXML
	private RadioButton months3Rd;

	@FXML
	private RadioButton months6Rd;

	@FXML
	private RadioButton customRd;

	@FXML
	private DatePicker startDate;

	@FXML
	private DatePicker endDate;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		init();
	}

	private void init() {

		btcchart.setCreateSymbols(false);
		btcchart.setLegendVisible(false);

		startDate.setValue(LocalDate.now());
		endDate.setValue(LocalDate.now());

		startDate.setShowWeekNumbers(false);
		endDate.setShowWeekNumbers(false);

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
		startDate.setDayCellFactory(dayCellFactory);
		endDate.setDayCellFactory(dayCellFactory);

		optionChanged();
	}

	public void refreshChart(ActionEvent ae) {
		// Main.getInstance().changeScene(UIPage.Page.START);
		ae.consume();

		optionChanged();
	}

	public void optionChanged() {

		if (customRd.isSelected()) {
			startDate.setDisable(false);
			endDate.setDisable(false);
			duration = Duration.CUSTOM;
		} else if (months3Rd.isSelected()) {
			duration = Duration.THREE;
			startDate.setDisable(true);
			endDate.setDisable(true);
		} else if (months6Rd.isSelected()) {
			duration = Duration.SIX;
			startDate.setDisable(true);
			endDate.setDisable(true);
		} else if (months1Rd.isSelected()) {
			duration = Duration.ONE;
			startDate.setDisable(true);
			endDate.setDisable(true);
		}

		if (exchangeCmb.getValue().equalsIgnoreCase("Kraken")) {
			exchange = ExchangeProvider.KRAKEN.getInstance();
		} else if (exchangeCmb.getValue().equalsIgnoreCase("Coinbase")) {
			exchange = ExchangeProvider.COINBASE.getInstance();
		}

		// TODO Save configs inside a ChartObject
		updateChart();
	}

	private void updateChart() {

		btcchart.getData().clear();
		Series<String, Double> series1 = new XYChart.Series<String, Double>();

		exchange.getData("BCH", "USD", 1440).forEach(pd -> {
			series1.getData().add(new Data<String, Double>(pd.getDate(), pd.getValue()));

		});

		btcchart.getData().add(series1);
	}
}
