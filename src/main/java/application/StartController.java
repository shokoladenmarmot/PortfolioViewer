package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class StartController implements Initializable {

	@FXML
	private GridPane startLayout;

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
		Main.getInstance().changeScene("View.fxml");
		ae.consume();
	}

	public void loadTemplate(ActionEvent ae) {
		Main.getInstance().changeScene("View.fxml");
//		Parser.open();
		ae.consume();
	}

	public void loadCharts(ActionEvent ae) {
		Main.getInstance().changeScene("Charts.fxml");
		ae.consume();
	}

}
