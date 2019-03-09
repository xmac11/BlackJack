package gui;

import javafx.event.ActionEvent;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

	@FXML
	private TextField ipField;
	
	@FXML
	private TextField userField;
	
	@FXML
	private Label errorLabel;
	
	public void serverDown() {
		errorLabel.setVisible(true);
	}

	public void joinPressed(ActionEvent event) throws IOException {

		if (!ipField.getText().equals("") && !userField.getText().equals("")) {
			errorLabel.setVisible(false);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LobbyScreen.fxml"));
			Scene lobbyScene = new Scene(loader.load());
			LobbyController lobbyController = loader.<LobbyController>getController();
			Stage thisStage = (Stage)((Node)event.getSource()).getScene().getWindow();
			lobbyController.initData(ipField.getText(), userField.getText(), thisStage);
			thisStage.setHeight(800);
			thisStage.setWidth(800);
			thisStage.setScene(lobbyScene);
			thisStage.show();
			thisStage.setOnCloseRequest(e -> lobbyController.thisPlayerLeft());
		}
	}

}
