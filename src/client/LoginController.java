/**
 * Author: Group21 - Final version
 * Class LoginController: This is the Controller for the LoginScreen
 */
package client;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import database.Authentication;
import database.SQLDatabaseConnection;
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
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
	private double passFieldX;
	private double passFieldY;

	public void serverDown() { // shows error message if server is down
		System.out.println("Server is down.");
		errorLabel.setText("Error - Cannot connect to server");
		errorLabel.setVisible(true);
	}

	public void joinPressed(ActionEvent event) throws IOException {
		if (ipField.getText().trim().length() > 0 && userField.getText().trim().length() > 0
				&& passField.getText().trim().length() > 0) {
			if (Authentication.login(userField.getText(), passField.getText())) {
				errorLabel.setVisible(false);
				FXMLLoader loader = new FXMLLoader(getClass().getResource("LobbyScreen.fxml"));
				Scene lobbyScene = new Scene(loader.load());
				LobbyController lobbyController = loader.<LobbyController>getController();
				Stage thisStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
				lobbyController.initData(ipField.getText(), userField.getText(), passField.getText(), thisStage);
				thisStage.setHeight(768);
				thisStage.setWidth(1366);
				thisStage.setMinHeight(768);
				thisStage.setMinWidth(1366);
				thisStage.setMaxHeight(768);
				thisStage.setMaxWidth(1366);
				thisStage.setResizable(true);
				thisStage.setScene(lobbyScene);
				thisStage.show();
				thisStage.setOnCloseRequest(e -> lobbyController.thisPlayerClosedLobby());
			} else {
				errorLabel.setText("Error - Wrong log in credentials");
				errorLabel.setVisible(true);
				wobbleField(userField, userFieldX, userFieldY);
				wobbleField(passField, passFieldX, passFieldY);
			}
		} else if (!(ipField.getText().trim().length() > 0)) {
			wobbleField(ipField, ipFieldX, ipFieldY);
		}
		if (!(userField.getText().trim().length() > 0)) {
			wobbleField(userField, userFieldX, userFieldY);
		}
		if (!(passField.getText().trim().length() > 0)) {
			wobbleField(passField, passFieldX, passFieldY);
		}

	}

	public void moveToPassword(ActionEvent actionEvent) {
		if (userField.getText().trim().length() > 0) {
			userField.setOnAction(e -> passField.requestFocus());
		}
	}

	public void moveToIP(ActionEvent actionEvent) {
		if (passField.getText().trim().length() > 0) {
			passField.setOnAction(e -> ipField.requestFocus());
		}

	}

	public void signUp() {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.getIcons().add(new Image("image/appIcon.png"));
		stage.setTitle("Sign Up");
		stage.setWidth(400);
		stage.setHeight(300);
		stage.setResizable(false);
		Label usernameLabel = new Label("Enter Username");
		Label passLabel = new Label("Enter Password");
		Label passLabel2 = new Label("Enter Password again");
		Label error = new Label("");
		usernameLabel.setTextFill(Color.WHITE);
		passLabel.setTextFill(Color.WHITE);
		passLabel2.setTextFill(Color.WHITE);
		error.setTextFill(Color.RED);
		error.setVisible(false);

		TextField username = new TextField();
		username.setMaxWidth(300);
		PasswordField password1 = new PasswordField();
		PasswordField password2 = new PasswordField();
		password1.setMaxWidth(300);
		password2.setMaxWidth(300);
		username.setPadding(new Insets(5, 5, 5, 5));
		password1.setPadding(new Insets(5, 5, 5, 5));
		password2.setPadding(new Insets(5, 5, 5, 5));

		// sign up button
		Button signButton = new Button("Sign up");
		signButton.setOnAction(e -> {
			error.setVisible(false);
			if (username.getText().contains(" ") || !(username.getText().matches("[a-zA-Z0-9\\_]*")
					|| (username.getText().trim().length() > 10) || (username.getText().trim().length() < 2))) {
				error.setText("Username must not contain spaces and must be between 2-10 characters");
				error.setVisible(true);
			} else if (password1.getText().equals(password2.getText()) && (password1.getText().trim().length() > 3)) {
				if (Authentication.newAccount(username.getText(), password1.getText())) {
					stage.close();
				} else {
					error.setText("Username already exists");
					error.setVisible(true);
				}
			} else {
				error.setText("Passwords must match and be longer than 3 characters");
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
		vBox.getChildren().addAll(usernameLabel, username, passLabel, password1, passLabel2, password2, error, hBox1);
		vBox.setAlignment(Pos.CENTER);
		StackPane root = new StackPane(vBox);
		root.setStyle("-fx-background-image: url('" + "/image/signUpBackground.jpg" + "'); "
				+ "-fx-background-position: center center; " + "-fx-background-repeat: repeat;");
		Scene scene = new Scene(root, 200, 400);
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
		passFieldX = passField.getTranslateX();
		passFieldY = passField.getTranslateY();
		SQLDatabaseConnection sqlDatabaseConnection = new SQLDatabaseConnection();
		Thread thread = new Thread(sqlDatabaseConnection);
		thread.start();
	}

}
