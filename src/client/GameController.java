package client;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import database.MatchHistory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Class that handles user input
 *
 * @author Group21
 *
 */

public class GameController implements Initializable {

	@FXML
	protected HBox hBoxDealer;

	@FXML
	protected ToggleButton muteButton;

	@FXML
	protected HBox hBoxPlayer;

	@FXML
	protected HBox hBoxPlayer2;

	@FXML
	protected HBox hBoxPlayer3;

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
	private GridPane betPane;

	@FXML
	private Label betAmount;

	@FXML
	private Button leaveButton;

	@FXML
	private TextField textField;

	@FXML
	private Label chat;

	@FXML
	private ListView<String> chatView;

	@FXML
	private Label points;

	@FXML
	private TextField betField;

	private String username;
	private int ID;
	private PrintWriter output;
	private int noPlayers;
	private static boolean confirm;
	private Stage stage;
	private static final int MINBET = 5;
	private int fundsAvailable;
	private Client client;

	/**
	 * Action handlers for hit and stand buttons being clicked. If the user clicks
	 * hit and the card they are dealt is over 21, then they get the message -
	 * "Sorry, player is bust".
	 */
	public void onHitButtonClicked() {
		System.out.println("Player is dealt a new card from the deck.");
		output.println("h");
	}

	/**
	 * Sets the music On and Off
	 * When mute button is clicked all the sounds stop
	 * and when its back on the background music plays again
	 */
	public void muteMusic() {
		if (muteButton.isSelected()) {
			client.gameScreenMusic.stop();
			client.dealerWins.stop();
			client.draw.stop();
			client.placeYourBets.stop();
			client.playerWins.stop();
		}else
			client.gameScreenMusic.play(0.100);

	}

	public void setClient(Client client) {
		this.client = client;
	}

	/**
	 * Disables and sets the chat off
	 */
	public void endChat() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatView.setDisable(true);
				textField.setDisable(true);
			}
		});
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	/**
	 * Checks if the text the user wants to send into the Chat is in the valid format
	 * Users are not able to send double quotes in their message
	 */
	public void sendChat() {
		String incomingText = textField.getText();
		if (incomingText.matches("[a-zA-Z\\s0-9\\-.,!@$%'£^&?<>_+=()]*") && incomingText.trim().length() > 0) { // Checks if the incomingText is valid
			output.println("gameChatMessage\ngameChatMessage" + username + "\ngameChatMessage" + incomingText); // Outputs the text
		} else {
			addToChat("Error - Only letters and numbers allowed in chat"); // Sends an appropriate error message
		}
		textField.setText(""); // Message is sent within the system as String
	}

	/**
	 * Adds the messages in the chat
	 * @param message the message the user wants to type
	 */
	public void addToChat(String message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				chatView.getItems().add(message); // Adds the message
				chatView.scrollTo(chatView.getItems().size() - 1); // Scrolls to the next line
			}
		});
	}
 
	/**
	 * It closes the window when the connection is closed
	 */
	public void connectionLost() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.close(); // Window closes
			}
		});
	}

	/**
	 * It is activated when the user closes the window
	 * @param title the title of the window
	 * @param message the message that is shown
	 * @return confirm boolean value
	 */
	public static boolean displayConfirmBox(String title, String message) {

		Stage stage = new Stage(); // Creates new window
		stage.initModality(Modality.APPLICATION_MODAL); // Blocks events to other windows
		stage.setTitle(title);
		stage.setWidth(400);
		stage.setHeight(150);
		stage.initStyle(StageStyle.UNDECORATED);
		Label label = new Label(message); // Creates a label with the input message

		// yes button
		Button yesButton = new Button("Yes");
		yesButton.setOnAction(e -> {
			confirm = true; // Boolean value to check the option of the user
			stage.close();

		});

		// no button
		Button noButton = new Button("No");
		noButton.setOnAction(e -> {
			confirm = false;
			stage.close();
		});

		HBox hBox = new HBox(10); // Creates new HBox
		hBox.getChildren().addAll(label, yesButton, noButton); // Adds the elements in the HBox
		hBox.setAlignment(Pos.CENTER);
		Scene scene = new Scene(hBox, 250, 300); // Creates a new scene and adds the HBox
		stage.setScene(scene); // Window closes
		stage.showAndWait();

		return confirm; // Returns the boolean value
	}

	/**
	 * Prints the name of the player who left
	 */
	public void playerLeft() {
		output.println("playerLeftGame");
	}

	/**
	 * It runs when the player click the 'Stand' button, his turn ends
	 */
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

	/**
	 * Sets the text as a visible label
	 * @param text the given label
	 */
	public void setLabel(String text) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				label.setText(text);
				label.setVisible(true);
			}
		});
	}

	/**
	 * GameScreen and GameMusic closes
	 */
	public void closeGameScreen() {
		client.stopGameMusic();
		stage.close();
	}

	/**
	 * Sets the Label of dealer visible
	 * @param text the label of the dealer
	 */
	public void setDealerLabel(String text) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				labelDealer.setText(text);
				labelDealer.setVisible(true);
			}
		});
	}

	/**
	 * Sets the label of the points
	 * @param text the label of points
	 */
	public void setPointsLabel(String text) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				points.setText(text);
				points.setVisible(true);
			}
		});
	}

	/**
	 * Gets the card and creates a String file out of it
	 * @param card the card in String format
	 * @return file returns the edited image
	 */
	public static String cardToFile(String card) {
		String file = card;
		file = file.replaceAll(" ", "_of_"); // Replaces " "
		file = file.toLowerCase(); // Converts to lowerCase
		file += ".png"; // Adds the .png extension
		System.out.println("card file:" + file);
		return file;
	}

	/**
	 * Disables the hit Button
	 */
	public void disableHit() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				hitButton.setDisable(true);
			}
		});
	}

	/**
	 * Disables the stand Button
	 */
	public void disableStand() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				standButton.setDisable(true);
			}
		});
	}

	/**
	 * Enables the hit Button
	 */
	public void enableHit() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				hitButton.setDisable(false);
			}
		});
	}

	/**
	 * Disables the stand Button
	 */
	public void enableStand() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				standButton.setDisable(false);
			}
		});
	}

	/**
	 * Enables the leave Button
	 */
	public void showLeaveButton() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				leaveButton.setVisible(true);

			}
		});
	}

	/**
	 * Loads up the image if the card
	 * @param card the card in String format
	 * @return the card as Image
	 */
	public Image getImage(String card) {
		if (card.contains("facedown")) {
			return new Image("/image/Playing Cards/" + card);
		} else {
			return new Image("/image/Playing Cards/" + cardToFile(card));
		}
	}

	/**
	 * Adds cards to Player's hand
	 * @param card the card in String format
	 */
	public void addCardToPlayerHand(String card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ImageView cardImage = new ImageView(); // Creates an ImageView
				cardImage.setFitHeight(150);
				cardImage.setFitWidth(100);
				Image image = getImage(card);
				cardImage.setImage(image); // Inputs the card image inside the ImageView
				hBoxPlayer.getChildren().add(cardImage); // Adds it inside the HBox
				// Resize the HBox in case there are > 4 cards (to avoid overlapping)
				if (hBoxPlayer.getChildren().size() > 4) {
					List<ImageView> smallerImages = new ArrayList<>();
					for (int i = 0; i < hBoxPlayer.getChildren().size(); i++) {
						ImageView smaller = (ImageView) hBoxPlayer.getChildren().remove(i);
						i--;
						smaller.setFitHeight(100);
						smaller.setFitWidth(66);
						smallerImages.add(smaller);
					}
					hBoxPlayer.getChildren().addAll(smallerImages);
				}
				// Resize the HBox in case there are > 3 cards (to avoid overlapping)
				if (hBoxPlayer.getChildren().size() > 3) {
					switch (noPlayers) {
					case 3:
						List<ImageView> smallerImages = new ArrayList<>();
						for (int i = 0; i < hBoxPlayer3.getChildren().size(); i++) {
							ImageView smaller = (ImageView) hBoxPlayer3.getChildren().remove(i);
							i--;
							smaller.setFitHeight(80);
							smaller.setFitWidth(55);
							smallerImages.add(smaller);
						}
						hBoxPlayer3.getChildren().addAll(smallerImages);

					case 2:
						smallerImages = new ArrayList<>();
						for (int i = 0; i < hBoxPlayer2.getChildren().size(); i++) {
							ImageView smaller = (ImageView) hBoxPlayer2.getChildren().remove(i);
							i--;
							smaller.setFitHeight(80);
							smaller.setFitWidth(55);
							smallerImages.add(smaller);
						}
						hBoxPlayer2.getChildren().addAll(smallerImages);
					default:
						break;
					}
				}
			}
		});
	}

	/**
	 * Adds card to Opposing Player's hand
	 * @param player the number of the player
	 * @param card the card in String format
	 */
	public void addCardToOpposingPlayerHand(int player, String card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ImageView cardImage = new ImageView(); // Creates an ImageView
				cardImage.setFitHeight(150);
				cardImage.setFitWidth(100);
				javafx.scene.image.Image image = getImage(card);
				cardImage.setImage(image); // Inputs the image of the card inside the ImageView
				if (player == 2) {
					hBoxPlayer2.getChildren().add(cardImage);
					// Resizes to avoid overlapping
					if (hBoxPlayer2.getChildren().size() > 3 || hBoxPlayer.getChildren().size() > 3) {
						List<ImageView> smallerImages = new ArrayList<>();
						for (int i = 0; i < hBoxPlayer2.getChildren().size(); i++) {
							ImageView smaller = (ImageView) hBoxPlayer2.getChildren().remove(i);
							i--;
							smaller.setFitHeight(80);
							smaller.setFitWidth(55);
							smallerImages.add(smaller);
						}
						hBoxPlayer2.getChildren().addAll(smallerImages);
					}
				} else {
					hBoxPlayer3.getChildren().add(cardImage);
					// Resizes to avoid overlapping
					if (hBoxPlayer3.getChildren().size() > 3 || hBoxPlayer.getChildren().size() > 3) {
						List<ImageView> smallerImages = new ArrayList<>();
						for (int i = 0; i < hBoxPlayer3.getChildren().size(); i++) {
							ImageView smaller = (ImageView) hBoxPlayer3.getChildren().remove(i);
							i--;
							smaller.setFitHeight(80);
							smaller.setFitWidth(55);
							smallerImages.add(smaller);
						}
						hBoxPlayer3.getChildren().addAll(smallerImages);
					}
				}
			}
		});
	}

	/**
	 * Adds card to dealer's hand
	 * @param card the card in String format
	 */
	public void addCardToDealerHand(String card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ImageView cardImage = new ImageView(); // Creates an ImageView
				cardImage.setFitHeight(150);
				cardImage.setFitWidth(100);
				Image image = getImage(card);
				cardImage.setImage(image); // Inputs the image of the card inside the ImageView
				hBoxDealer.getChildren().add(cardImage);
			}
		});
	}

	/**
	 * Remove dealer's facedown cards
	 */
	public void removeDealerFacedown() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				hBoxDealer.getChildren().remove(1);
			}
		});
	}

	/**
	 * Remove player's facedown cards
	 * @param player the number of the player
	 */
	public void removeFacedown(int player) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (player == 2) {
					hBoxPlayer2.getChildren().clear();
				} else {
					hBoxPlayer3.getChildren().clear();
				}
			}
		});
	}

	/**
	 * Sets the playing table based on the amount of players
	 * @param table
	 */
	public void setTable(List<List<String>> table) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (noPlayers > 1) {
					player2Label.setVisible(true);
					addCardToOpposingPlayerHand(2, "facedown.jpg");
					addCardToOpposingPlayerHand(2, "facedown.jpg");
				}
				if (noPlayers > 2) {
					player3Label.setVisible(true);
					addCardToOpposingPlayerHand(3, "facedown.jpg");
					addCardToOpposingPlayerHand(3, "facedown.jpg");
				}
				if (table.size() > 0) {
					addCardToDealerHand(table.get(0).get(0));
					addCardToDealerHand("facedown.jpg");
					addCardToPlayerHand(table.get(ID).get(0));
					addCardToPlayerHand(table.get(ID).get(1));
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
		points.setVisible(false);
		chatView.setCellFactory(param -> new ListCell<String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
					setText(null);
				} else {
					// set the width's
					setMinWidth(param.getWidth() - 20);
					setMaxWidth(param.getWidth() - 20);
					setPrefWidth(param.getWidth() - 20);
					// allow wrapping
					setWrapText(true);
					setText(item.toString());
				}
			}
		});
		betPane.setVisible(false);
		betAmount.setText(Integer.toString(MINBET));
	}

	/**
	 * Sets the betPane visible
	 */
	public void showBetPane() {
		fundsAvailable = MatchHistory.getAmount(username);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				betPane.setVisible(true);
			}
		});
	}

	/**
	 * Sets the betPane off
	 */
	public void hideBetPane() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				betPane.setVisible(false);
				setLabel("Waiting for other\nplayers to bet");
			}
		});
	}

	public PrintWriter getOutput() {
		return output;
	}

	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Sends the text to the chat field when chat Button is pressed
	 */
	public void chatButtonPressed() {
		sendChat();
	}

	/**
	 * Increases the bet of each player
	 */
	public void increaseBet() { 
		String betString = betAmount.getText();
		int bet = Integer.parseInt(betString); // Parses the String betString into int
		if (bet < fundsAvailable) {
			betAmount.setText(Integer.toString(bet + MINBET)); // MINBET refers to the minimum betting amount of 5
		}
	}

	/**
	 * Sets the label off
	 */
	public void hideLabel() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				label.setVisible(false);
			}
		});
	}

	/**
	 * Decreases the bet of each player
	 */
	public void decreaseBet() {
		String betString = betAmount.getText();
		int bet = Integer.parseInt(betString); // Parses the String betString into int
		if (bet > MINBET) {
			betAmount.setText(Integer.toString(bet - MINBET)); // MINBET refers to the minimum betting amount of 5
		}
	}

	/**
	 * Places the bet of each player
	 */
	public void placeBet() {
		String betString = betAmount.getText();
		int bet = Integer.parseInt(betString); // Parses the String betString into int
		MatchHistory.reduceAmount(username, bet); // Reduces the betting amount from player's wallet
		output.println("betIs " + bet); // Prints the bet amount
		hideBetPane(); // BetPane closes
	}

}
