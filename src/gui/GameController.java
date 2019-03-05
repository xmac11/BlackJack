package gui;

import java.net.URL;

import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Part of the application that handles user input.
 */
public class GameController implements Initializable {

	@FXML
	private HBox hBoxDealer;

	@FXML
	private HBox hBoxPlayer;

	@FXML
	private HBox hBoxPlayer2;

	@FXML
	private HBox hBoxPlayer3;

	@FXML
	private Label label;

	@FXML
	private Label player2Label;

	@FXML
	private Label player3Label;

	@FXML
	private Button standButton;

	@FXML
	private Button hitButton;

	@FXML
	private TextField textField;

	@FXML
	private Label chat;

	@FXML
	private ListView<String> chatView;

	private List<String> dealerHand;
	private List<String> hand;
	private List<List<String>> table;
	private String IP;

	private Semaphore waitForServer;
	private Semaphore waitForInput;
	private Semaphore chatWait;
	private String toSend;
	private String chatMessage = "";
	private int ID;
	private int noPlayers;

	/**
	 * Action handlers for hit and stand buttons being clicked. If the user clicks
	 * hit and the card they are dealt is over 21, then they get the message -
	 * "Sorry, player is bust".
	 */
	public void onHitButtonClicked() {
		System.out.println("Player is dealt a new card from the deck.");
		toSend = "h";
		waitForInput.release();
	}

	public void sendChat() {
		chatMessage = textField.getText();
		textField.setText("");
		chatWait.release();
	}

	public void addToChat(String message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatView.getItems().add(message);
			}
		});
	}

	public String getChat() {
		return chatMessage;
	}

	public void setChatMessage(String message) {
		this.chatMessage = message;
	}

	public void onStandButtonClicked() {
		disableHit();
		disableStand();
		System.out.println("Player stands. Next player's turn...");
		toSend = "p";
		waitForInput.release();
	}

	public String getToSend() {
		return toSend;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void setNoPlayers(int noPlayers) {
		this.noPlayers = noPlayers;
	}

	public void setLabel(String text) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				label.setText(text);
			}
		});
	}

	public static String cardToFile(String card) {
		String file = card;
		file = file.replaceAll(" ", "_of_");
		file = file.toLowerCase();
		file += ".png";
		return file;
	}

	public void disableHit() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				hitButton.setDisable(true);
			}
		});
	}

	public void disableStand() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				standButton.setDisable(true);
			}
		});
	}

	public void enableHit() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				hitButton.setDisable(false);
			}
		});
	}

	public void enableStand() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				standButton.setDisable(false);
			}
		});
	}

	public void addCardToHand(String card, HBox hand) {
		ImageView cardImage = new ImageView();
		cardImage.setFitHeight(150);
		cardImage.setFitWidth(100);
		Image image = null;
		if (card.contains("facedown")) {
			image = new javafx.scene.image.Image("/image/Playing Cards/" + card);
		} else {
			image = new javafx.scene.image.Image("/image/Playing Cards/" + cardToFile(card));
		}
		cardImage.setImage(image);
		hand.getChildren().add(cardImage);
	}

	public void addCardToPlayerHand(String card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ImageView cardImage = new ImageView();
				cardImage.setFitHeight(150);
				cardImage.setFitWidth(100);
				javafx.scene.image.Image image = new javafx.scene.image.Image(
						"/image/Playing Cards/" + cardToFile(card));
				cardImage.setImage(image);
				hBoxPlayer.getChildren().add(cardImage);
			}
		});
	}

	public void addCardToOpposingPlayerHand(int player, String card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ImageView cardImage = new ImageView();
				cardImage.setFitHeight(150);
				cardImage.setFitWidth(100);
				javafx.scene.image.Image image = new javafx.scene.image.Image(
						"/image/Playing Cards/" + cardToFile(card));
				cardImage.setImage(image);
				if (player == 2)
					hBoxPlayer2.getChildren().add(cardImage);
				else
					hBoxPlayer3.getChildren().add(cardImage);
			}
		});
	}

	public void addCardToDealerHand(String card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ImageView cardImage = new ImageView();
				cardImage.setFitHeight(150);
				cardImage.setFitWidth(100);
				javafx.scene.image.Image image = new javafx.scene.image.Image(
						"/image/Playing Cards/" + cardToFile(card));
				cardImage.setImage(image);
				hBoxDealer.getChildren().add(cardImage);
			}
		});
	}

	public void removeDealerFacedown() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				hBoxDealer.getChildren().remove(0);
				hBoxDealer.getChildren().remove(0);
			}
		});
	}

	public void removeFacedown(int player) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (player == 2) {
					hBoxPlayer2.getChildren().remove(0);
					hBoxPlayer2.getChildren().remove(0);
				} else {
					hBoxPlayer3.getChildren().remove(0);
					hBoxPlayer3.getChildren().remove(0);
				}
			}
		});
	}
	
	void initData(String IP) {
	    this.IP = IP;
	  }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		hitButton.setDisable(true);
		standButton.setDisable(true);
		waitForServer = new Semaphore(0);
		dealerHand = new ArrayList<>();
		hand = new ArrayList<>();
		table = new ArrayList<>();
		waitForInput = new Semaphore(0);
		chatWait = new Semaphore(0);
		toSend = "";
		Runnable runnable = new Client(table, dealerHand, hand, waitForServer, this, waitForInput, chatWait, IP);
		Thread thread = new Thread(runnable);
		thread.start();
		System.out.println("Waiting at lock");
		try {
			waitForServer.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("release lock");
		if (noPlayers > 1) {
			player2Label.setVisible(true);
			addCardToHand("facedown.jpg", hBoxPlayer2);
			addCardToHand("facedown.jpg", hBoxPlayer2);
		}
		if (noPlayers > 2) {
			player3Label.setVisible(true);
			addCardToHand("facedown.jpg", hBoxPlayer3);
			addCardToHand("facedown.jpg", hBoxPlayer3);
		}
		if (table.size() > 0) {
			addCardToHand("facedown.jpg", hBoxDealer);
			addCardToHand("facedown.jpg", hBoxDealer);
			addCardToHand(table.get(ID).get(0), hBoxPlayer);
			addCardToHand(table.get(ID).get(1), hBoxPlayer);
		} else {
			setLabel("Error! Unable to join session.");
		}

	}

}
