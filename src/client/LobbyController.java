package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

import database.MatchHistory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Class that handles the Controller for the LobbyScreen
 *
 * @author Group21
 *
 */

public class LobbyController implements Initializable {

	/**
	 * List of FXML id's used to match between the Login Screen objects and the
	 * specific actions those objects should be utilised in.
	 */

	@FXML
	protected Button joinButton;

	@FXML
	private Button playButton;

	@FXML
	private Button leaveButton;

	@FXML
	protected ToggleButton muteButton;

	@FXML
	private ListView<String> queueView;

	@FXML
	private ListView<String> chatView;

	@FXML
	private ListView<String> onlineView;

	@FXML
	private TextField chatField;

	@FXML
	private Label wonLabel;

	@FXML
	private Label fundsLabel;

	@FXML
	private Button addFundsButton;

	@FXML
	private Label walletLabel;

	@FXML
	private Label playedLabel;

	private Stage thisStage;
	private Stage gameStage;
	private Client client;
	private Semaphore waitForController;
	private List<List<String>> table;
	private PrintWriter output;
	private String username;

	/**
	 * Method controls the 'mute' button. The muteButton is a toggle button. When selected, the lobbyMusic and
	 * welcomeVoice audio files are stopped. Otherwise the lobbyMusic plays.
	 */

	public void muteMusic() {
		if (muteButton.isSelected())
			client.lobbyScreenMusic.stop();
		else
			client.lobbyScreenMusic.play(0.100);
	}

	/**
	 * getClient call allows this class to call methods from within the Client class.
	 * @return client instance
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * Method which controls whether the 'Add funds' button shows. Method uses the runLater call so that the button will
	 * show after the appropriate point. This ensures the run thread is placed backwards in the thread queue.
	 */
	public void showAddFunds() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				fundsLabel.setVisible(true);
				addFundsButton.setVisible(true);
			}
		});
	}

	/**
	 * This method sets out when to disable the chat function using the similar method of runLater. This is to
	 * prevent the chat function in the lobby screen whilst in game.
	 */
	public void disableChat() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatField.setDisable(true);
			}
		});
	}

	/**
	 * This method enables the chat function.
	 */

	public void enableChat() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatField.setDisable(false);
			}
		});
	}

	/**
	 * This method is called when the add funds button is clicked. It adds funds to the user's account through the
	 * increaseAmount method in the Match History class. It uses the wallet label to display the amount that the user
	 * has in their account.
	 */

	public void addFunds() {
		MatchHistory.increaseAmount(username, 200);
		addFundsButton.setVisible(false);
		walletLabel.setText("Wallet: " + MatchHistory.getAmount(username));
		fundsLabel.setVisible(false);
	}

	/**
	 * This method is called when a game starts - either when the user presses the 'play' button or when the
	 * game queue reaches three users. It loads the game screen fxml file which disables the play and join buttons
	 * in the lobby. The waitForController semaphore is released and the scene is set. The user can close the window
	 * despite it not being finished as per the setOnCloseRequest. If the game is finished, the game music is stopped.
	 */

	public void gameBegin() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("GameScreen.fxml"));
				Scene gameScene = null;
				try {
					gameScene = new Scene(loader.load());
				} catch (IOException e) {
					e.printStackTrace();
				}
				playButton.setDisable(true);
				joinButton.setDisable(true);
				gameScene.getStylesheets().addAll(getClass().getResource("style.css").toExternalForm());
				Stage window = new Stage();
				GameController gameController = loader.<GameController>getController();
				client.setGameController(gameController);
				waitForController.release();
				gameController.setClient(client);
				window.setScene(gameScene);
				window.setHeight(900);
				gameStage = window;
				window.setWidth(1600);
				window.setMinWidth(1366);
				window.setMinHeight(768);
				gameController.setStage(window);
				window.setMaxHeight(1080);
				window.setMaxWidth(1920);
				window.setTitle("Club21");
				window.getIcons().add(new Image("image/appIcon.png"));
				window.setScene(gameScene);
				window.show();
				window.setOnCloseRequest(e -> {
					if (!client.isGameFinished()) {
						e.consume();
						client.closeGame(window);
					}else 
						client.stopGameMusic();
				});
			}
		});
	}

	/**
	 * Method controls the sign out procedure. If the client is in a game session then the window closes and the
	 * player leaving causes the print line message to the console. Otherwise, the lobby music stops and the client is
	 * signed out of the lobby which closes loads the login screen.
	 */
	public void signOut() {
		if(client.isInGame()) {
			output.println("thisPlayerLeft");
			gameStage.close();
		}
		client.lobbyScreenMusic.stop();
		client.signOut();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginScreen.fxml"));
		Scene loginScene = null;
		try {
			loginScene = new Scene(loader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		thisStage.sizeToScene();
		thisStage.setResizable(false);
		thisStage.setScene(loginScene);
		thisStage.show();
		thisStage.setOnCloseRequest(e -> System.exit(0));
	}

	/**
	 * Method controls the scenario where connection is lost. In this instance, the lobby music stops and
	 * the login screen is reloaded.
	 */

	public void connectionLost() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				client.lobbyScreenMusic.stop();
				FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginScreen.fxml"));
				Scene loginScene = null;
				try {
					loginScene = new Scene(loader.load());
				} catch (IOException e) {
					e.printStackTrace();
				}
				LoginController loginController = loader.<LoginController>getController();
				loginController.serverDown();
				thisStage.sizeToScene();
				thisStage.setResizable(false);
				thisStage.setScene(loginScene);
				thisStage.show();
				thisStage.setOnCloseRequest(e -> System.exit(0));
			}
		});
	}

	/**
	 * Getter for username
	 * @return the username String
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Method initiates the events which occur when the user joins the game queue: they are unable
	 * to press the join button again since they have already done so and the play and leave buttons are
	 * now enabled.
	 */
	public void queueJoined() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				playButton.setDisable(false);
				joinButton.setDisable(true);
				leaveButton.setDisable(false);
			}
		});
	}

	/**
	 * Method initiates the events which occur when the user leaves the game queue: they are able
	 * to press the join button and the play and leave buttons are
	 * now disabled.
	 */
	public void queueLeft() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				playButton.setDisable(true);
				joinButton.setDisable(false);
				leaveButton.setDisable(true);
			}
		});
	}

	/**
	 * Once users join the queue, the queue size is added to and updated.
	 * This is done using a for loop over the queue list elements.
	 * @param queue List of usernames in String form which are in the queue
	 */
	public void addQueue(List<String> queue) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				queueView.getItems().clear();
				for (int i = 0; i < queue.size(); i++) {
					queueView.getItems().add(queue.get(i));
				}
			}
		});
	}

	/**
	 * Once users are logged in, this method updates the amount of online players.
	 * Does this again through a for loop over the List of online players as Strings.
	 * @param online List of Strings of usernames
	 */
	public void addOnline(List<String> online) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				onlineView.getItems().clear();
				for (int i = 0; i < online.size(); i++) {
					onlineView.getItems().add(online.get(i));
				}
			}
		});
	}

	/**
	 * Method takes in a String noPlayers and disables play, leave and join buttons. This is called when
	 * a game is in progress and displays the number of players as a String in the chat box.
	 * @param noPlayers number of players as a String
	 */

	public void gameInProgress(String noPlayers) {
		System.out.println("called"+ noPlayers);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				System.out.println("entered");
				playButton.setDisable(true);
				leaveButton.setDisable(true);
				joinButton.setDisable(true);
				chatView.getItems().add("Game in progress with " + noPlayers + " player"
						+ ((Integer.parseInt(noPlayers) > 1) ? "s" : ""));
			}
		});
	}

	/**
	 * This method is used in order too set the join button to being disabled.
	 */
	public void joinUnavailable() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				joinButton.setDisable(true);
			}
		});
	}

	/**
	 * ClearQueue method is called when a game is finished and the in game queue is then cleared of all
	 * the players. The leave and play buttons are then disabled and the join button is reenabled.
	 */
	public void clearQueue() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				queueView.getItems().clear();
				leaveButton.setDisable(true);
				joinButton.setDisable(false);
				playButton.setDisable(true);
				chatView.getItems().add("Game finished");
			}
		});
	}

	/**
	 * This method is used to update the database fields such as the gamesPlayed, gamesWon and amount in
	 * the user's wallet.
	 */

	public void updateData() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (MatchHistory.getGamesPlayed(username) != -1) {
					playedLabel.setText("Games played: " + MatchHistory.getGamesPlayed(username));
				} else {
					playedLabel.setText("Games played: error");
					connectionLost();
				}
				if (MatchHistory.getGamesWon(username) != -1) {
					wonLabel.setText("Games won: " + MatchHistory.getGamesWon(username));
				} else {
					wonLabel.setText("Games won: error");
					connectionLost();
				}
				walletLabel.setText("Wallet: " + MatchHistory.getAmount(username));
			}
		});
	}

	/**
	 * Method called when player closes lobby screen which outputs a message to the console stating that the
	 * player signed out.
	 */
	public void thisPlayerClosedLobby() {
		System.out.println("leaving");
		if (client.isInGame())
			client.closeGame(thisStage);
		output.println("thisPlayerSignedOut");
		System.exit(0);
	}

	/**
	 * Method for sending messages via the chat box. The field's text is stored inside the incomingText String and
	 * the method checks that the strings are of an appropriate type (with only normal GB English keyboard symbols
	 * numbers and letters allowed - as per the regex). The length has to be above 0 to prevent users spamming the
	 * chat box with empty messages. If not an error message is printed to the chat box.
	 */
	public void sendChat() {
		String incomingText = chatField.getText();
		if (incomingText.matches("[a-zA-Z\\s0-9\\-.,!@$%'£^&?<>_+=()]*") && incomingText.trim().length() > 0) {
			output.println("lobbyChatMessage\nlobbyChatMessage" + username +"\nlobbyChatMessage" + incomingText);
		} else {
			addToChat("Error - Only letters and numbers allowed in chat");
		}
		chatField.setText("");
	}

	/**
	 * Method takes in a String message and displays it in the chatView. There is automatic scrolling down to the
	 * lower most message when multiple messages are on screen through .scrollTo the size of the list minus 1.
	 * @param message
	 */
	public void addToChat(String message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatView.getItems().add(message);
				chatView.scrollTo(chatView.getItems().size() - 1);
			}
		});

	}

	/**
	 * Method joins the user to the queue on pushing the join queue button.
	 */
	public void joinQueue() {
		System.out.println("join queue pressed");
		fundsLabel.setVisible(false);
		output.println("joinQueue");
		joinButton.setDisable(true);
	}

	/**
	 * Method joins the user to the queue on pushing the leave queue button.
	 */
	public void leaveQueue() {
		output.println("leaveQueue");
	}

	/**
	 * Method called to show that the game has been started using output String.
	 */
	public void startGame() {
		output.println("gameStart");

	}

	/**
	 * Method takes in the IP, the username, the port and the stage. These are set as the initial variables for
	 * the client and the data is updated using these. Then the semaphore is released with this data.
	 * @param IP number
	 * @param username name
	 * @param port number
	 * @param stage lobbyscreen
	 */
	public void initData(String IP, String username, String port, Stage stage) {
		this.username = username;
		thisStage = stage;
		client.setInitialVariables(username, IP, Integer.parseInt(port));
		updateData();
		waitForController.release();
	}

	/**
	 * Outputs the text
	 * @return text from the printwriter is output.
	 */
	public PrintWriter getOutput() {
		return output;
	}

	/**
	 * Text from the output of the print writer is taken in and set to the output instance variable.
	 * @param output text taken in.
	 */
	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	/**
	 * Method which starts automatically. Table is created in addition to a client instance with the table and the semaphore.
	 * Play, leave and join buttons are set to desabled initially and the chatview is sent parameters which outline the cell format
	 * and positioning.
	 * The client thread is started.
	 * @param location
	 * @param resources
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		waitForController = new Semaphore(0);
		table = new ArrayList<>();
		client = new Client(table, waitForController, this);
		playButton.setDisable(true);
		leaveButton.setDisable(true);
		joinButton.setDisable(true);
		chatView.setCellFactory(param -> new ListCell<String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
					setText(null);
				} else {
					setPrefWidth(0);
					setWrapText(true);
					setText(item.toString());
				}
			}
		});
		Thread thread = new Thread(client);
		thread.start();

	}
}
