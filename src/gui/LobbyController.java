package gui;

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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
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

	private String IP;
	private Socket socket;
	private Client client;
	private Semaphore waitForServer = new Semaphore(0);
	private List<List<String>> table = new ArrayList<>();
	private Semaphore chatWait;
	private Semaphore waitForInput;
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
				waitForServer.release();
				window.setScene(gameScene);
				window.setHeight(900);
				window.setWidth(1600);
				window.setScene(gameScene);
				window.show();
				window.setOnCloseRequest(e -> gameController.playerLeft());
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
				playButton.setVisible(true);
				playButton.setDisable(false);
				joinButton.setDisable(true);
			}
		});
	}
	
	public void addQueue(List<String> queue) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				queueView.getItems().clear();
				for(int i = 0;i<queue.size();i++) {
					queueView.getItems().add(queue.get(i));
				}
			}
		});
	}
	
	public void addOnline(List<String> online){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				onlineView.getItems().clear();
				for(int i = 0;i<online.size();i++) {
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
				playButton.setVisible(false);
				chatView.getItems().add("Game finished");
			}
		});
	}

	public void joinQueue() {
		System.out.println("join queue pressed");
		output.println("joinQueue");
	}

	public void startGame() throws IOException {
		output.println("gameStart");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void initData(String IP, String username) {
		this.IP = IP;
		this.username = username;
	}

	public PrintWriter getOutput() {
		return output;
	}

	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
//			socket = new Socket(IP, 9999);
			waitForServer = new Semaphore(0);
			waitForInput = new Semaphore(0);
			chatWait = new Semaphore(0);
			client = new Client(table, waitForServer, waitForInput, chatWait, IP, this);
			playButton.setVisible(false);
			Thread thread = new Thread(client);
			thread.start();
		} catch (Exception e) {
			System.out.println("error");
		}
	}

}
