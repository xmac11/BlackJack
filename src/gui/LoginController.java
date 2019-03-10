package gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController implements Initializable{

	@FXML
	private TextField ipField;

	@FXML
	private TextField userField;

	@FXML
	private Label errorLabel;
	
	double userFieldX;
	double userFieldY;

	public void serverDown() {
		errorLabel.setVisible(true);
	}


	public void joinPressed(ActionEvent event) throws IOException {
		if (ipField.getText().trim().length() > 0 && userField.getText().trim().length() > 0) {
			errorLabel.setVisible(false);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LobbyScreen.fxml"));
			Scene lobbyScene = new Scene(loader.load());
			LobbyController lobbyController = loader.<LobbyController>getController();
			Stage thisStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			lobbyController.initData(ipField.getText(), userField.getText(), thisStage);
			thisStage.setHeight(800);
			thisStage.setWidth(800);
			thisStage.setScene(lobbyScene);
			thisStage.show();
			thisStage.setOnCloseRequest(e -> lobbyController.thisPlayerLeft());
		} else if(!(ipField.getText().trim().length() > 0)) {
			wobbleField(ipField, userFieldX, userFieldY);
		} if (!(userField.getText().trim().length() > 0)) {
			wobbleField(userField, userFieldX, userFieldY);
		}
	}

	public void moveToIP(ActionEvent actionEvent) {
		if (userField.getText().trim().length() > 0) {
			userField.setOnAction(e -> ipField.requestFocus());
		}

	}
	
	public void wobbleField(TextField field, double X, double Y) {
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().addAll(new KeyFrame(Duration.ZERO, // set start position at 0
				new KeyValue(field.translateXProperty(), X + 5),
				new KeyValue(field.translateYProperty(), Y)),
				new KeyFrame(new Duration(50), new KeyValue(field.translateXProperty(), X),
						new KeyValue(field.translateYProperty(), Y)),
				new KeyFrame(new Duration(50), // set start position at 0
						new KeyValue(field.translateXProperty(), X - 5),
						new KeyValue(field.translateYProperty(), Y)),
				new KeyFrame(new Duration(100), new KeyValue(field.translateXProperty(), X),
						new KeyValue(field.translateYProperty(), Y)));
		timeline.play();
	}

	public void startIfFieldsFulfilled(ActionEvent actionEvent) throws IOException {
		joinPressed(actionEvent);
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		userFieldX = userField.getTranslateX();	
		userFieldY = userField.getTranslateY();	
	}
}
