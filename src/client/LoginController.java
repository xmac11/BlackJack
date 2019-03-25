package client;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;

import static javafx.scene.media.MediaPlayer.INDEFINITE;

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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Class that handles the Controller for the LobbyScreen
 *
 * @author Group21
 *
 */

public class LoginController implements Initializable {

    /**
     * List of FXML id's used to match between the Login Screen objects and the
     * specific actions those objects should be utilised in.
     */
    @FXML
    private TextField ipField;

    @FXML
    private TextField userField;

    @FXML
    private TextField portField;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField passField;

    @FXML
    protected ToggleButton muteButton;

    /**
     * List of Private variables denoting screen size positioning on the
     * Y and X axis. Used to create wobble effect when information is missing.
     */

    private double userFieldX;
    private double userFieldY;
    private double ipFieldX;
    private double ipFieldY;
    private double passFieldX;
    private double passFieldY;
    private double portFieldX;
    private double portFieldY;
    public AudioClip welcomeVoice = new AudioClip(
            getClass().getResource("/music/WelcomeToCLub21.wav").toExternalForm());
    public AudioClip loginMusic = new AudioClip(getClass().getResource("/music/LoginMusic.wav").toExternalForm());

    /**
     * Method called whever the server cannot be connected to. Uses the error label which becomes visible upon
     * this method being called.
     */

    public void serverDown() { // shows error message if server is down
        System.out.println("Server is down.");
        errorLabel.setText("Error - Cannot connect to server");
        errorLabel.setVisible(true);
    }

    /**
     * Method controls the 'mute' button. The muteButton is a toggle button. When selected, the loginMusic and
     * welcomeVoice audio files are stopped. Otherwise the loginmusic plays.
     */

    public void muteMusic() {
        if (muteButton.isSelected()) {
            loginMusic.stop();
            welcomeVoice.stop();
        } else
            loginMusic.play(0.1);

    }

    /**
     * This method controls the actions undertaken when the join button (denoted with the word "start") is pressed.
     * The error label visibility is set to false unless an error occurs. The method checks that the ipField, userField,
     * passField and portFields are all provided with the correct entries. Entries of less than 1 length are classes as
     * incorrect entries as provided by the if statements. The entries for the portfield must only be of type numeric.
     * This is set using the regex 0-9.
     *
     * In this method the database is checked against the provided userfield and passfield text entries. If it is a perfect
     * match then the login music stops and the lobbyscreen loads and opens. However if they are not a match then the appropriate
     * error label vecomes visible and the wobbleField method is called.
     * @param event start button is pressed
     * @throws IOException for .load in the case that it cannot load the screen
     */

    public void joinPressed(ActionEvent event) throws IOException {
        errorLabel.setVisible(false);
        if (ipField.getText().trim().length() > 0 && userField.getText().trim().length() > 0
                && passField.getText().trim().length() > 0 && portField.getText().trim().length() > 0
                && portField.getText().matches("[0-9]+")) {
            if (Authentication.login(userField.getText(), passField.getText())) {
                loginMusic.stop();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("LobbyScreen.fxml"));
                Scene lobbyScene = new Scene(loader.load());
                LobbyController lobbyController = loader.<LobbyController>getController();
                Stage thisStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                lobbyController.initData(ipField.getText(), userField.getText(), portField.getText(), thisStage);
                thisStage.setHeight(768);
                thisStage.setWidth(1366);
                thisStage.setResizable(false);
                thisStage.setScene(lobbyScene);
                thisStage.show();
                thisStage.setOnCloseRequest(e -> {
                    lobbyController.thisPlayerClosedLobby();
                });
            } else {
                errorLabel.setText("Error - Wrong log in credentials");
                errorLabel.setVisible(true);
                wobbleField(userField, userFieldX, userFieldY);
                wobbleField(passField, passFieldX, passFieldY);
            }
        } else {
            if (!(ipField.getText().trim().length() > 0)) {
                wobbleField(ipField, ipFieldX, ipFieldY);
            }
            if (!(userField.getText().trim().length() > 0)) {
                wobbleField(userField, userFieldX, userFieldY);
            }
            if (!(passField.getText().trim().length() > 0)) {
                wobbleField(passField, passFieldX, passFieldY);
            }
            if (!(portField.getText().trim().length() > 0) || !portField.getText().matches("[0-9]+")) {
                wobbleField(portField, portFieldX, portFieldY);
            }
            errorLabel.setText("Error - Wrong log in credentials");
            errorLabel.setVisible(true);
        }
    }

    /**
     * The signUp method is called when the user clickes the "Sign up" label at the bottom of the
     * login screen. It ooens a new stage, which is a very basic small screen housing only a username
     * text field and two password text fields (so that they ensure they enter the correct password
     * that they initially desired to enter).
     *
     * Once the stage is set, the sign up button's actions are controlled. Upon clicking the button,
     * the username and password field entries are checked that only alphanumeric are entered = otherwise
     * an error label appears. Limits for legnth are also set with a minimum of 4 and a maximum of 16 for passwords
     * and 2-10 for usernames. A
     * permitted username and password is then stored in the new account table in the database.
     *
     * The cancel button, once pressed, closes the sign up window.
     */

    public void signUp() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(new Image("image/appIcon.png"));
        stage.setTitle("Sign Up");
        stage.setWidth(600);
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
            if (username.getText().contains(" ") || (username.getText().trim().length() > 10)
                    || (username.getText().trim().length() < 2) || !(username.getText().matches("[a-zA-Z0-9\\_]*"))) {
                error.setText("Username must contain only alphanumerics and be 2-10 characters long");
                error.setVisible(true);
            } else if (password1.getText().equals(password2.getText()) && (password1.getText().trim().length() > 3)
                    && !(password1.getText().trim().length() > 16)) {
                if (Authentication.newAccount(username.getText(), password1.getText())) {
                    stage.close();
                } else {
                    error.setText("Username already exists");
                    error.setVisible(true);
                }
            } else {
                error.setText("Passwords must match and be 4-16 characters long");
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

    /**
     * This method initiates the fields 'wobble' movement whenever there is missing/wrong information in one or more of the
     * fields.
     * @param field the textfield text entered
     * @param X the position of the field on the x axis
     * @param Y the position of the field on the y axis
     */
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

    /**
     * THis method runs automatically when the login screen is first opened and starts the thread foor the SQL
     * database connection. The login music and welcome voice also starts here automsatically. The music is set to run indefinitely
     * so that it will coninue looping until the screen is closed.
     * @param location FXML file location
     * @param resources locale specific resources for the login screen
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userFieldX = userField.getTranslateX();
        userFieldY = userField.getTranslateY();
        ipFieldX = ipField.getTranslateX();
        ipFieldY = ipField.getTranslateY();
        passFieldX = passField.getTranslateX();
        passFieldY = passField.getTranslateY();
        portFieldX = portField.getTranslateX();
        portFieldY = portField.getTranslateY();
        SQLDatabaseConnection sqlDatabaseConnection = new SQLDatabaseConnection();
        Thread thread = new Thread(sqlDatabaseConnection);
        thread.start();
        loginMusic.setVolume(0.1);
        loginMusic.setCycleCount(INDEFINITE);
        loginMusic.play();
        welcomeVoice.play();
    }

}
