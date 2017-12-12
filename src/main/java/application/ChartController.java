package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;

public class ChartController implements Initializable {

	enum Duration {
		ONE, THREE, SIX, CUSTOM
	}

	private Duration duration;

	@FXML
	private LineChart<String, Integer> btcchart;

	@FXML
	private CategoryAxis xAxis;

	@FXML
	private NumberAxis yAxis;

	@FXML
	private ComboBox<String> exchange;

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
		assert exchange != null : "fx:id=\"exchange\" not declared in " + fileName;

		// TODO add for all

		btcchart.setCreateSymbols(false);
		btcchart.setLegendVisible(false);

		radioChanged();

		// exchange.getSelectionModel().selectedItemProperty().addListener(new
		// ChangeListener<String>() {
		//
		// @Override
		// public void changed(ObservableValue<? extends String> selected, String
		// oldValue, String newValue) {
		// updateChart();
		// }
		// });

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

//		System.out.println(JSONFactory.getJSONObject("https://api.kraken.com/0/public/Ticker?pair=BCHEUR").toString(2));
		System.out.println(JSONFactory.getJSONObject("https://api.kraken.com/0/public/OHLC?pair=BCHEUR&interval=1440").toString(2));

		updateChart();
	}

	public void radioChanged() {
		updateChart();
	}

	private void updateChart() {
		btcchart.getData().clear();

		Series<String, Integer> series1 = new XYChart.Series<String, Integer>();

		series1.getData().add(new Data<String, Integer>("Jan", 23));
		series1.getData().add(new Data<String, Integer>("Feb", 14));
		series1.getData().add(new Data<String, Integer>("Mar", 15));
		btcchart.getData().add(series1);
	}

}
