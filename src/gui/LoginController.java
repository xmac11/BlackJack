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
	
	@FXML
	private TextField userField;

	public void joinPressed(ActionEvent event) throws IOException {

		if (!ipField.getText().equals("") && !userField.getText().equals("")) {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LobbyScreen.fxml"));
			Scene lobbyScene = new Scene(loader.load());
			Stage window = new Stage();
			LobbyController lobbyController = loader.<LobbyController>getController();
			lobbyController.initData(ipField.getText(), userField.getText());
			window.setScene(lobbyScene);
			Stage thisStage = (Stage)((Node)event.getSource()).getScene().getWindow();
			thisStage.setHeight(600);
			thisStage.setWidth(800);
			thisStage.setScene(lobbyScene);
			thisStage.show();
		}
	}

}
