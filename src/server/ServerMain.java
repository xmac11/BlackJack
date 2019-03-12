package server;

import com.sun.javafx.application.LauncherImpl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ServerMain extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("ServerScreen.fxml"));
		primaryStage.setTitle("Club21");
		Scene scene = new Scene(root, 800, 600);
		primaryStage.setResizable(true);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image("image/appIcon.png"));
		primaryStage.show();
//		primaryStage.setMinWidth(WIDTH - 400);
//		primaryStage.setMinHeight(HEIGHT - 150);
		primaryStage.setOnCloseRequest(e -> System.exit(0));
	}

	public static void main(String[] args) {
		launch(args);
	}

}
