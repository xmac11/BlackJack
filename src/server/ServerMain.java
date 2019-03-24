package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Class responsible for starting the server GUI
 * @author Group 21
 *
 */
public class ServerMain extends Application {

	private static int WIDTH = 300;
	private static int HEIGHT = 200;

	/**
	 * Method to start the GUI
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("ServerScreen.fxml")); // login screen loads first
		primaryStage.setTitle("Club21");
		Scene scene = new Scene(root, WIDTH, HEIGHT); // default resolution
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image("image/appIcon.png"));
		primaryStage.show();
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(e -> System.exit(0));
	}

	public static void main(String[] args) {
		launch(args);
	}

}
