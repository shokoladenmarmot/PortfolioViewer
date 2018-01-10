package controllers;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import Start.Main;
import exchanges.Exchange;
import exchanges.Exchange.PairData;
import exchanges.ExchangeProvider;
import fxml.UIPage;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.transform.Scale;
import javafx.util.Callback;

public class ChartController implements Initializable {

	private static final Logger LOGGER = Logger.getLogger(ChartController.class.getName());

	enum Duration {
		ONE, THREE, SIX, CUSTOM
	}

	@FXML
	private LineChart<String, Double> btcchart;

	@FXML
	private CategoryAxis xAxis;

	@FXML
	private NumberAxis yAxis;

	@FXML
	private ComboBox<String> exchangeCmb;

	@FXML
	private ComboBox<String> symbolCmb;

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

	private Duration duration;
	private Exchange exchange;
	private Series<String, Double> series1;
	private Tooltip tp;

	private boolean init = false;
	private final SimpleDateFormat chartFormatter = new SimpleDateFormat("dd/MM/yy");

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (init == false) {
			init = true;
			init();
		}
	}

	private void init() {
		tp = new Tooltip();

		yAxis.setAnimated(false);
		xAxis.setAnimated(false);
		series1 = new XYChart.Series<String, Double>();
		btcchart.getData().add(series1);

		btcchart.setCreateSymbols(true);
		btcchart.setLegendVisible(false);

		btcchart.lookup(".chart-plot-background").setOnMouseMoved(e -> {
			String x = xAxis.getValueForDisplay(e.getX());
			Number y = yAxis.getValueForDisplay(e.getY());

			if (x != null) {
				tp.setText("Date: " + x + "\nPrice: " + y);
			}
		});
		Scale sc = new Scale();
		btcchart.lookup(".chart-plot-background").setOnScroll(e -> {
			double amount = sc.getX();
			if (e.getDeltaY() > 0) {
				amount += 0.1;
			} else {
				amount -= 0.1;
			}
			sc.setX(amount);
		});

		btcchart.getTransforms().add(sc);

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

	public void backToMain(ActionEvent ae) {
		Main.getInstance().changeScene(UIPage.Page.START);
		ae.consume();
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

		Exchange currentVal = exchange;
		exchange = ExchangeProvider.getMarket(exchangeCmb.getValue());

		if (currentVal != exchange) {
			symbolCmb.getItems().setAll(exchange.getAvailablePairs());
		}

		// TODO Save configs inside a ChartObject
		updateChart();
	}

	private void updateChart() {
		String pairName = symbolCmb.getValue();

		if ((pairName == null) || pairName.trim().isEmpty())
			return;

		ObservableList<PairData> dataList = exchange.getOHLCData(pairName, 1440);

		Callable<Void> v = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				Platform.runLater(() -> {
					List<Data<String, Double>> templ = new ArrayList<Data<String, Double>>();

					dataList.forEach(pd -> {
						templ.add(new Data<String, Double>(chartFormatter.format(pd.getDate()), pd.getValue()));
					});
					series1.getData().setAll(templ);

					Set<Node> nList = btcchart.lookupAll(".chart-line-symbol");
					for (Node n : nList) {
						n.setOnMouseEntered(e -> {
							n.setEffect(new Glow());
						});
						n.setOnMouseExited(e -> {
							n.setEffect(null);
						});
						Tooltip.install(n, tp);
					}
				});
				return null;
			}
		};

		if (dataList.isEmpty()) {
			dataList.addListener(new ListChangeListener<PairData>() {

				@Override
				public void onChanged(Change<? extends PairData> c) {
					try {
						v.call();
					} catch (Exception e) {
						e.printStackTrace();
					}
					dataList.removeListener(this);
				}

			});
		} else {
			try {
				v.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
