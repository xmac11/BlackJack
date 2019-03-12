package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class LobbyController implements Initializable {

	@FXML
	private Button joinButton;

	@FXML
	private Button playButton;

	@FXML
	private ListView<String> queueView;

	@FXML
	private ListView<String> chatView;

	@FXML
	private ListView<String> onlineView;

	@FXML
	private TextField chatField;

	private String IP;
	private Stage thisStage;
	private Client client;
	private Semaphore waitForController;
	private List<List<String>> table;
	private PrintWriter output;
	private String username;

	public Client getClient() {
		return client;
	}

	public void gameBegin() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("GameScreen.fxml"));
				Scene gameScene = null;
				try {
					gameScene = new Scene(loader.load());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				playButton.setDisable(true);
				joinButton.setDisable(true);
				gameScene.getStylesheets().addAll(getClass().getResource("style.css").toExternalForm());
				Stage window = new Stage();

				GameController gameController = loader.<GameController>getController();
				client.setGameController(gameController);
				waitForController.release();
				window.setScene(gameScene);
				window.setHeight(900);
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
					}
				});
			}
		});
	}

	public void signOut() {
//		Platform.runLater(new Runnable() {
//			@Override
//			public void run() {
		client.signOut();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginScreen.fxml"));
		Scene loginScene = null;
		try {
			loginScene = new Scene(loader.load());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		thisStage.setResizable(true);
		thisStage.setScene(loginScene);
		thisStage.show();
		thisStage.setWidth(800);
		thisStage.setHeight(600);
		thisStage.setResizable(false);
		thisStage.setOnCloseRequest(e -> System.exit(0));
//			}
//		});
	}

	public void connectionLost() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginScreen.fxml"));
				Scene loginScene = null;
				try {
					loginScene = new Scene(loader.load());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				LoginController loginController = loader.<LoginController>getController();
				loginController.serverDown();
				thisStage.setResizable(true);
				thisStage.setScene(loginScene);
				thisStage.show();
				thisStage.setWidth(800);
				thisStage.setHeight(600);
				thisStage.setResizable(false);
				thisStage.setOnCloseRequest(e -> System.exit(0));
			}
		});
	}

	public String getUsername() {
		return username;
	}

	public void queueJoined() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				playButton.setDisable(false);
				joinButton.setDisable(true);
			}
		});
	}

	public void queueLeft() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				playButton.setDisable(true);
				joinButton.setDisable(false);
			}
		});
	}

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

	public void gameInProgress(String noPlayers) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				playButton.setDisable(true);
				joinButton.setDisable(true);
				chatView.getItems().add("Game in progress with " + noPlayers + " player"
						+ ((Integer.parseInt(noPlayers) > 1) ? "s" : ""));
			}
		});
	}

	public void joinUnavailable() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				joinButton.setDisable(true);
			}
		});
	}

	public void clearQueue() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				queueView.getItems().clear();
				joinButton.setDisable(false);
				playButton.setDisable(true);
				chatView.getItems().add("Game finished");
			}
		});
	}

	public void thisPlayerClosedLobby() {
		System.out.println("leaving");
		output.println("thisPlayerSignedOut");
		System.exit(0);
	}

	public void sendChat() {
		String incomingText = chatField.getText();
		if (incomingText.matches("[a-zA-Z\\s0-9]*") && incomingText.trim().length() > 0) {
			output.println("lobbyChatMessage");
			output.println("lobbyChatMessage" + username);
			output.println("lobbyChatMessage" + incomingText);
		} else {
			addToChat("Error - Only letters and numbers allowed in chat");
		}
		chatField.setText("");
	}

	public void addToChat(String message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatView.getItems().add(message);
				chatView.scrollTo(chatView.getItems().size() - 1);
			}
		});

	}

	public void joinQueue() {
		System.out.println("join queue pressed");
		output.println("joinQueue");
	}

	public void leaveQueue() {
		System.out.println("join queue pressed");
		output.println("leaveQueue");
	}

	public void startGame() throws IOException {
		output.println("gameStart");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void initData(String IP, String username, String password, Stage stage) {
		this.IP = IP;
		this.username = username;
		thisStage = stage;
		waitForController.release();
	}

	public PrintWriter getOutput() {
		return output;
	}

	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		waitForController = new Semaphore(0);
		table = new ArrayList<>();
		client = new Client(table, waitForController, IP, this);
		playButton.setDisable(true);
		chatView.setCellFactory(param -> new ListCell<String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
					setText(null);
				} else {
					// set the width's
					setMinWidth(param.getWidth() -20);
					setMaxWidth(param.getWidth() -20);
					setPrefWidth(param.getWidth()-20);
					// allow wrapping
					setWrapText(true);
					setText(item.toString());
				}
			}
		});
		Thread thread = new Thread(client);
		thread.start();
	}

	public Stage getThisStage() {
		return thisStage;
	}

	public void setThisStage(Stage thisStage) {
		this.thisStage = thisStage;
	}

}
