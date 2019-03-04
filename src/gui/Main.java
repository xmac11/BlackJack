package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	private static int WIDTH = 1600;
	private static int HEIGHT = 900;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
		primaryStage.setTitle("Black Jack-off");
		Scene scene = new Scene(root, WIDTH, HEIGHT);
		scene.getStylesheets().addAll(getClass().getResource("style.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image("image/Playing Cards/a_of_spades.png"));
		primaryStage.show();
		primaryStage.setMinWidth(WIDTH-200);
		primaryStage.setMinHeight(HEIGHT-100);
		primaryStage.setOnCloseRequest(e -> System.exit(0));
	}

	public static void main(String[] args) {
		launch(args);
	}

}
