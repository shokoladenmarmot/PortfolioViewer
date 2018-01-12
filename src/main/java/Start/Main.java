package Start;

import java.io.File;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import exchanges.Exchange;
import exchanges.ExchangeProvider;
import fxml.UIPage;
import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	public static final Logger LOGGER = Logger.getLogger(Main.class.getName());

	private static Main instance;
	private Stage myStage;

	public final ScheduledThreadPoolExecutor threadExc = new ScheduledThreadPoolExecutor(5);

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

	/**
	 * Initialize all exchanges.
	 */
	private void initialize() {

		for (ExchangeProvider ep : ExchangeProvider.values()) {
			threadExc.execute(() -> {
				do {
					ep.getInstance().initiate();
				} while (ep.getInstance().getStatus() == Exchange.Status.INIT);
			});
		}
	}

	@Override
	public void stop() {
		threadExc.shutdown();
		LOGGER.info("Shutting down");
		try {
			threadExc.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.warning(e.getMessage());
			e.printStackTrace();
		}
	}

	public void changeScene(UIPage.Page p) {
		try {
			Parent root = UIPage.INSTANCE.getParent(p);

			Scene scene = root.getScene();
			if (scene == null) {
				scene = new Scene(root, 800, 400);
			}
			scene.getStylesheets().add(UIPage.defaultStyleSheet);
			if (p.css != null) {
				scene.getStylesheets().add(p.css.toExternalForm());
			}

			myStage.setScene(scene);
			myStage.show();
		} catch (Exception e) {
			LOGGER.warning(e.getMessage());
			e.printStackTrace();
		}
	}

	public Parent getCurrentParent() {
		if (myStage != null && myStage.getScene() != null) {
			return myStage.getScene().getRoot();
		}
		return null;
	}

	public static void main(String[] args) {
		launch(args);
	}

	public File openFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a trade history file");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"),
				new FileChooser.ExtensionFilter("All Types", "*.*"));

		return fileChooser.showOpenDialog(myStage);
	}

	public File saveToFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export trade history file");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML File", "*.xml"),
				new FileChooser.ExtensionFilter("All Types", "*.*"));
		return fileChooser.showSaveDialog(myStage);
	}
}
