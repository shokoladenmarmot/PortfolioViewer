package Start;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import exchanges.ExchangeProvider;
import fxml.UIPage;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	private static Main instance;
	private Stage myStage;

	private final ScheduledThreadPoolExecutor threadExc = new ScheduledThreadPoolExecutor(2);

	public static Main getInstance() {
		return instance;
	}

	@Override
	public void start(Stage primaryStage) {
		instance = this;
		myStage = primaryStage;

		initialize();
		changeScene(UIPage.Page.START);
	}

	private void initialize() {
		
		threadExc.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				for (ExchangeProvider ep : ExchangeProvider.values()) {
					ep.getInstance().update();
				}
			}
		}, 0, 10, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {
		threadExc.shutdown();
		System.out.println("Exiting");
	}

	public void changeScene(UIPage.Page p) {
		try {
			Parent root = UIPage.INSTANCE.getParent(p);

			double width = 800;
			double height = 400;

			if (myStage.getScene() != null) {
				width = myStage.getScene().getWidth();
				height = myStage.getScene().getHeight();
			}

			Scene scene = new Scene(root, width, height);
			scene.getStylesheets().add(UIPage.defaultStyleSheet);
			if (p.css != null) {
				scene.getStylesheets().add(p.css.toExternalForm());
			}

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
