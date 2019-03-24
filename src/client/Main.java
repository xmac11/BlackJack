/**
 * Author: Group21 - Final version
 * Class Main: This is the main class of the game. Each can launch this and get in the game.
 */
package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

import static javafx.scene.media.MediaPlayer.INDEFINITE;

public class Main extends Application {

	private static int WIDTH = 800;
	private static int HEIGHT = 600;

	/**
	 * Start method opens the Login Screen of the application by loading the appropriate FXML file.
	 * @param primaryStage login screen is the first stage
	 * @throws Exception thrown should the stage not load
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("LoginScreen.fxml")); // login screen loads first
		primaryStage.setTitle("Club21");
		Scene scene = new Scene(root, WIDTH, HEIGHT); // default resolution
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image("image/appIcon.png"));
		primaryStage.show();
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(e -> System.exit(0));
	}

	/**
	 * Main method launches the application's start method
	 * @param args used to launch the start method
	 */
	public static void main(String[] args)  {
		launch(args);
	}

}
