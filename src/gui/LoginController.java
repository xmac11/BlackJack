package gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import database.Authentication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController implements Initializable {

	@FXML
	private TextField ipField;

	@FXML
	private TextField userField;

	@FXML
	private Label errorLabel;

	@FXML
	private TextField passField;

	private double userFieldX;
	private double userFieldY;
	private double ipFieldX;
	private double ipFieldY;

	public void serverDown() {
		System.out.println("Server is down.");
		errorLabel.setVisible(true);
	}

	public void joinPressed(ActionEvent event) throws IOException {
		if (ipField.getText().trim().length() > 0 && userField.getText().trim().length() > 0 && passField.getText().trim().length() > 0) {
			errorLabel.setVisible(false);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LobbyScreen.fxml"));
			Scene lobbyScene = new Scene(loader.load());
			LobbyController lobbyController = loader.<LobbyController>getController();
			Stage thisStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			lobbyController.initData(ipField.getText(), userField.getText(), passField.getText(), thisStage);
			thisStage.setHeight(800);
			thisStage.setWidth(800);
			thisStage.setScene(lobbyScene);
			thisStage.show();
			thisStage.setOnCloseRequest(e -> lobbyController.thisPlayerLeft());
		} else if (!(ipField.getText().trim().length() > 0)) {
			wobbleField(ipField, userFieldX, userFieldY);
		}
		if (!(userField.getText().trim().length() > 0)) {
			wobbleField(userField, userFieldX, userFieldY);
		}
		if (!(passField.getText().trim().length() > 0)) {
			wobbleField(passField, userFieldX, userFieldY);
		}


	}

	public void moveToPassword(ActionEvent actionEvent) {
		if (userField.getText().trim().length() > 0) {
			userField.setOnAction(e -> passField.requestFocus() );
		}
	}
	public void moveToIP(ActionEvent actionEvent) {
		if (passField.getText().trim().length() > 0) {
			passField.setOnAction(e -> ipField.requestFocus());
		}

	}

	public void signUp() {
		System.out.println("Clicked");
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle("Sign Up");
		stage.setWidth(400);
		stage.setHeight(300);
		Label usernameLabel = new Label("Enter Username");
		Label passLabel = new Label("Enter Password");
		Label passLabel2 = new Label("Enter Password again");
		Label error = new Label("");
		error.setVisible(false);

		TextField username = new TextField();
		PasswordField password1 = new PasswordField();
		PasswordField password2 = new PasswordField();
		username.setPadding(new Insets(5, 5, 5, 5));
		password1.setPadding(new Insets(5, 5, 5, 5));
		password2.setPadding(new Insets(5, 5, 5, 5));

		// sign up button
		Button signButton = new Button("Sign up");
		signButton.setOnAction(e -> {
			error.setVisible(false);
			if(username.getText().contains(" ")) {
				error.setText("Username must not contain spaces");
				error.setVisible(true);
			}else if (password1.getText().equals(password2.getText())) {
				if (Authentication.newAccount(username.getText(), password1.getText())) {
					stage.close();
				}else {
					error.setText("Username already exists");
					error.setVisible(true);
				}
			} else {
				error.setText("Passwords must match");
				error.setVisible(true);
			}
		});

		// cancel button
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(e -> {
			stage.close();
		});

		HBox hBox1 = new HBox(10);
		hBox1.getChildren().addAll(signButton, cancelButton);
		hBox1.setAlignment(Pos.CENTER);
		VBox vBox = new VBox(5);
		vBox.getChildren().addAll(usernameLabel, username, passLabel, password1, passLabel2, password2, error,
				hBox1);
		vBox.setAlignment(Pos.CENTER);
		Scene scene = new Scene(vBox, 200, 400);
		stage.setScene(scene);
		stage.showAndWait();

	}

	public void wobbleField(TextField field, double X, double Y) {
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().addAll(new KeyFrame(Duration.ZERO, // set start position at 0
				new KeyValue(field.translateXProperty(), X + 5), new KeyValue(field.translateYProperty(), Y)),
				new KeyFrame(new Duration(50), new KeyValue(field.translateXProperty(), X),
						new KeyValue(field.translateYProperty(), Y)),
				new KeyFrame(new Duration(50), // set start position at 0
						new KeyValue(field.translateXProperty(), X - 5), new KeyValue(field.translateYProperty(), Y)),
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
		ipFieldX = ipField.getTranslateX();
		ipFieldY = ipField.getTranslateY();
	}


}
