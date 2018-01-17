package controllers;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import Start.Main;
import fxml.UIPage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;

public class StartController implements Initializable {

	private static final Logger LOGGER = Logger.getLogger(StartController.class.getName());

	@FXML
	private Button newTemp;

	@FXML
	private Button load;

	@FXML
	private Button chart;

	@FXML
	private ProgressBar progressBar;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		progressBar.progressProperty().unbind();
		progressBar.progressProperty().bind(Main.getInstance().getInitiateMarket().progressProperty());
		progressBar.progressProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				progressBar.setVisible((newValue.doubleValue() != 1));
			}

		});
	}

	public void newTemplate(ActionEvent ae) {
		ae.consume();
		if (progressBar.getProgress() != 1) {
			Alert dialog = new Alert(AlertType.CONFIRMATION);
			dialog.setTitle("Waring");
			dialog.setHeaderText("Initialization in progress!");
			dialog.setContentText("Do you want to continue?");

			Optional<ButtonType> result = dialog.showAndWait();
			if (result.get() != ButtonType.OK) {
				return;
			}
		}

		Main.getInstance().changeScene(UIPage.Page.VIEW);
	}

	public void loadCharts(ActionEvent ae) {
		ae.consume();
		if (progressBar.getProgress() != 1) {
			Alert dialog = new Alert(AlertType.CONFIRMATION);
			dialog.setTitle("Waring");
			dialog.setHeaderText("Initialization in progress!");
			dialog.setContentText("Do you want to continue?");

			Optional<ButtonType> result = dialog.showAndWait();
			if (result.get() != ButtonType.OK) {
				return;
			}
		}

		Main.getInstance().changeScene(UIPage.Page.CHART);
	}

}
