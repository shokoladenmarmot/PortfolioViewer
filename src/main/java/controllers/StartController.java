package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import Start.Main;
import fxml.UIPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public class StartController implements Initializable {

	
	@FXML
	private Button newTemp;

	@FXML
	private Button load;
	
	@FXML
	private Button chart;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		String fileName = location.getFile();
		fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
	}

	public void newTemplate(ActionEvent ae) {
		Main.getInstance().changeScene(UIPage.Page.VIEW);
		ae.consume();
	}

	public void loadTemplate(ActionEvent ae) {
		Main.getInstance().changeScene(UIPage.Page.VIEW);
		ae.consume();
	}

	public void loadCharts(ActionEvent ae) {
		Main.getInstance().changeScene(UIPage.Page.CHART);
		ae.consume();
	}

}
