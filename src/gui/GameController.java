package gui;

import java.io.PrintWriter;
import java.net.URL;

import javafx.scene.Scene;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
	private Label labelDealer;

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

	private String username;
	private int ID;
	private PrintWriter output;
	private int noPlayers;
	private static boolean confirm;
	private Stage stage;

	/**
	 * Action handlers for hit and stand buttons being clicked. If the user clicks
	 * hit and the card they are dealt is over 21, then they get the message -
	 * "Sorry, player is bust".
	 */
	public void onHitButtonClicked() {
		System.out.println("Player is dealt a new card from the deck.");
		output.println("h");
	}

	public void endChat() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatView.setDisable(true);
				textField.setDisable(true);
			}
		});
	}

	public void leaveGame() {
		System.exit(0);
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void sendChat() {
		String incomingText = textField.getText();
		if (incomingText.matches("[a-zA-Z\\s\'\"]+") && incomingText.trim().length() > 0) {
			output.println("gameChatMessage");
			output.println("gameChatMessage" + username);
			output.println("gameChatMessage" + incomingText);
		} else {
			addToChat("Error - Only letters and numbers allowed in chat");
		}
		textField.setText("");
	}

	public void addToChat(String message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatView.getItems().add(message);
			}
		});
	}

	public void connectionLost() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.close();
			}
		});
	}

	public static boolean displayConfirmBox(String title, String message) {

		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle(title);
		stage.setWidth(300);
		stage.setHeight(150);
		Label label = new Label(message);

		// yes button
		Button yesButton = new Button("Yes");
		yesButton.setOnAction(e -> {
			confirm = true;
			stage.close();
		});

		// no button
		Button noButton = new Button("No");
		noButton.setOnAction(e -> {
			confirm = false;
			stage.close();
		});

		HBox hBox = new HBox(10);
		hBox.getChildren().addAll(label, yesButton, noButton);
		hBox.setAlignment(Pos.CENTER);
		Scene scene = new Scene(hBox, 250, 300);
		stage.setScene(scene);
		stage.showAndWait();

		return confirm;
	}

	public void playerLeft() {
		output.println("playerLeftGame");
	}

	public void onStandButtonClicked() {
		disableHit();
		disableStand();
		System.out.println("Player stands. Next player's turn...");
		output.println("p");
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setNoPlayers(int noPlayers) {
		this.noPlayers = noPlayers;
	}

	public void setLabel(String text) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				label.setText(text);
				label.setVisible(true);
			}
		});
	}

	public void setDealerLabel(String text) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				labelDealer.setText(text);
				labelDealer.setVisible(true);
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
			image = getImage(card);
		} else {
			image = getImage(card);
		}
		cardImage.setImage(image);
		hand.getChildren().add(cardImage);
	}

	public Image getImage(String card) {
		if (card.contains("facedown")) {
			return new javafx.scene.image.Image("/image/Playing Cards/" + card);
		} else {
			return new javafx.scene.image.Image("/image/Playing Cards/" + cardToFile(card));
		}
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
				hBoxDealer.getChildren().remove(1);
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

	public void setTable(List<List<String>> table) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
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
					addCardToHand(table.get(0).get(0), hBoxDealer);
					addCardToHand("facedown.jpg", hBoxDealer);
					addCardToHand(table.get(ID).get(0), hBoxPlayer);
					addCardToHand(table.get(ID).get(1), hBoxPlayer);
				} else {
					setLabel("Error! Unable to join session.");
				}

			}
		});
	}

	/**
	 * Code inspired by :
	 * https://stackoverflow.com/questions/53493111/javafx-wrapping-text-in-listview
	 * 
	 * @param location
	 * @param resources
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		hitButton.setDisable(true);
		standButton.setDisable(true);
		label.setVisible(false);
		labelDealer.setVisible(false);
		chatView.setCellFactory(param -> new ListCell<String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
					setText(null);
				} else {
					// set the width's
					setMinWidth(param.getWidth());
					setMaxWidth(param.getWidth());
					setPrefWidth(param.getWidth());
					// allow wrapping
					setWrapText(true);
					setText(item.toString());
				}
			}
		});
	}

	public PrintWriter getOutput() {
		return output;
	}

	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	public void chatButtonPressed() {
		sendChat();

	}

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

}
