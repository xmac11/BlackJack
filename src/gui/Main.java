package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	private static int WIDTH = 800;
	private static int HEIGHT = 600;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("LoginScreen.fxml"));
		primaryStage.setTitle("Club21");
		Scene scene = new Scene(root, WIDTH, HEIGHT);
		primaryStage.setResizable(true);
//		primaryStage.setMinHeight(400);
//		primaryStage.setMaxHeight(300);



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
