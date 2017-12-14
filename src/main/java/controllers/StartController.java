package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import Start.Main;
import fxml.UIPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public class StartController implements Initializable {
	
	private static final Logger LOGGER = Logger.getLogger( StartController.class.getName() );

	
	@FXML
	private Button newTemp;

	@FXML
	private Button load;
	
	@FXML
	private Button chart;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void newTemplate(ActionEvent ae) {
		ae.consume();
		Main.getInstance().changeScene(UIPage.Page.VIEW);
	}

	public void loadCharts(ActionEvent ae) {
		ae.consume();
		Main.getInstance().changeScene(UIPage.Page.CHART);
	}

}
