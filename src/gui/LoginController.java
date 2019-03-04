package gui;

import javafx.event.ActionEvent;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

	@FXML
	private TextField ipField;

	public void joinPressed(ActionEvent event) throws IOException {

		if (!ipField.getText().equals("")) {
//			Parent gameViewParent = FXMLLoader.load(getClass().getResource("GameScreen.FXML"));
			FXMLLoader loader = new FXMLLoader(getClass().getResource("GameScreen.FXML"));
			Scene gameScene = new Scene(loader.load());
			gameScene.getStylesheets().addAll(getClass().getResource("style.css").toExternalForm());
			Stage window = new Stage();
			GameController gameController = loader.<GameController>getController();
			gameController.initData(ipField.getText());
			window.setScene(gameScene);
//			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.setHeight(900);
			window.setWidth(1600);
			window.setScene(gameScene);
			window.show();
		}
	}

}
