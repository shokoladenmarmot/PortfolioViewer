package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import Start.Main;
import exchanges.Exchange;
import exchanges.ExchangeProvider;
import fxml.UIPage;
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
import javafx.scene.control.RadioButton;

public class ChartController implements Initializable {

	private static final Logger LOGGER = Logger.getLogger( ChartController.class.getName() );
	
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

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		String fileName = location.getFile();
		fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

		assert btcchart != null : "fx:id=\"btcchart\" not declared in " + fileName;
		assert exchangeCmb != null : "fx:id=\"exchange\" not declared in " + fileName;

		// TODO add for all

		init();
	}

	private void init() {

		duration = Duration.ONE;
		exchange = ExchangeProvider.KRAKEN.getInstance();

		btcchart.setCreateSymbols(false);
		btcchart.setLegendVisible(false);

		updateChart();
	}

	public void refreshChart(ActionEvent ae) {
		ae.consume();
		Main.getInstance().changeScene(UIPage.Page.START);
//		updateChart();
	}

	public void exchangeChanged() {

		if (months1Rd.isSelected()) {
			duration = Duration.ONE;
		} else if (months3Rd.isSelected()) {
			duration = Duration.THREE;
		} else if (months6Rd.isSelected()) {
			duration = Duration.SIX;
		} else if (customRd.isSelected()) {
			duration = Duration.CUSTOM;
		}

		if (exchangeCmb.getValue().equalsIgnoreCase("Kraken")) {
			exchange = ExchangeProvider.KRAKEN.getInstance();
		} else if (exchangeCmb.getValue().equalsIgnoreCase("Coinbase")) {
			exchange = ExchangeProvider.COINBASE.getInstance();
		}

		// System.out.println(JSONFactory.getJSONObject("https://api.kraken.com/0/public/Ticker?pair=BCHEUR").toString(2));
		updateChart();
	}

	public void radioChanged() {
		updateChart();
	}

	private void updateChart() {

		if (exchange.getDate().isEmpty()) {
		} else {
			btcchart.getData().clear();
			Series<String, Double> series1 = new XYChart.Series<String, Double>();

			exchange.getDate().get("BCHUSD").forEach(pd -> {
				series1.getData().add(new Data<String, Double>(pd.getDate(), pd.getValue()));

			});

			btcchart.getData().add(series1);
		}
	}

}
