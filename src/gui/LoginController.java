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
			Stage thisStage = (Stage)((Node)event.getSource()).getScene().getWindow();
//			thisStage.close();
			thisStage.setHeight(900);
			thisStage.setWidth(1600);
			thisStage.setScene(gameScene);
			thisStage.show();
		}
	}

}
