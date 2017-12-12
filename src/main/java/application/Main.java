package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	private static Main instance;
	private Stage myStage;

	public static Main getInstance() {
		return instance;
	}

	@Override
	public void start(Stage primaryStage) {
		instance = this;
		myStage = primaryStage;
		changeScene("Start.fxml");
	}

	public void changeScene(String fileName) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource(fileName));

			double width = 800;
			double height = 400;

			if (myStage.getScene() != null) {
				width = myStage.getScene().getWidth();
				height = myStage.getScene().getHeight();
			}
			Scene scene = new Scene(root, width, height);
			scene.getStylesheets().add(getClass().getResource("default.css").toExternalForm());

			myStage.setScene(scene);
			myStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
