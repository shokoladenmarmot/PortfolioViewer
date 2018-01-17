package Start;

import java.io.File;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import exchanges.Exchange;
import exchanges.ExchangeProvider;
import fxml.UIPage;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

public class Main extends Application {
	public static final Logger LOGGER = Logger.getLogger(Main.class.getName());

	private static Main instance;
	private Stage myStage;

	private Task<?> initiateMarkets;

	public final ScheduledThreadPoolExecutor threadExc = new ScheduledThreadPoolExecutor(5);

	public static Main getInstance() {
		return instance;
	}

	public Task<?> getInitiateMarket() {
		if (initiateMarkets == null) {
			synchronized (this) {
				initiateMarkets = new Task<Object>() {

					@Override
					protected Object call() throws Exception {
						int doneSoFar = 0;

						// Go through all exchanges and initialize them. Given that the JSON factory is
						// synchronised there is no point for this to be concurrent.
						for (ExchangeProvider ep : ExchangeProvider.values()) {

							updateMessage("Initializing: " + ep.getInstance().getName());

							while (ep.getInstance().getStatus() != Exchange.Status.READY) {
								if (isCancelled()) {
									return false;
								}
								ep.getInstance().initiate();
							}

							updateProgress(++doneSoFar, ExchangeProvider.values().length);
						}
						return true;
					}
				};
			}
		}
		return initiateMarkets;
	}

	@Override
	public void start(Stage primaryStage) {
		instance = this;
		myStage = primaryStage;
		
		myStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/base.png"), 128, 128, true, true));

		changeScene(UIPage.Page.START);
		threadExc.execute(initiateMarkets);
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
