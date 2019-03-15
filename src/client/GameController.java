/**
 * Author: Group21 - Final version
 * Class GameController: This is the Controller for the GameScreen
 */
package client;

import java.io.PrintWriter;
import java.net.URL;

import javafx.scene.Scene;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import database.MatchHistory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

	public void setID(int ID) {
		this.ID = ID;
	}

	public void sendChat() {
		String incomingText = textField.getText();
		if (incomingText.matches("[a-zA-Z\\s0-9\\s.,!@$%£^&?<>()]*") && incomingText.trim().length() > 0) {
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
				chatView.scrollTo(chatView.getItems().size() - 1);
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

	public void closeGameScreen() {
		stage.close();
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

	public void setPointsLabel(String text) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				points.setText(text);
				points.setVisible(true);
			}
		});
	}

	public static String cardToFile(String card) {
		String file = card;
		file = file.replaceAll(" ", "_of_");
		file = file.toLowerCase();
		file += ".png";
		System.out.println("card file:" + file);
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

	public void showLeaveButton() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				leaveButton.setVisible(true);
			}
		});
	}

	public Image getImage(String card) {
		if (card.contains("facedown")) {
			return new Image("/image/Playing Cards/" + card);
		} else {
			return new Image("/image/Playing Cards/" + cardToFile(card));
		}
	}

	public void addCardToPlayerHand(String card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ImageView cardImage = new ImageView();
				cardImage.setFitHeight(150);
				cardImage.setFitWidth(100);
				Image image = getImage(card);
				cardImage.setImage(image);
				hBoxPlayer.getChildren().add(cardImage);
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

	public void addCardToOpposingPlayerHand(int player, String card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ImageView cardImage = new ImageView();
				cardImage.setFitHeight(150);
				cardImage.setFitWidth(100);
				javafx.scene.image.Image image = getImage(card);
				cardImage.setImage(image);
				if (player == 2) {
					hBoxPlayer2.getChildren().add(cardImage);
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

	public void addCardToDealerHand(String card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				ImageView cardImage = new ImageView();
				cardImage.setFitHeight(150);
				cardImage.setFitWidth(100);
				Image image = getImage(card);
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
					hBoxPlayer2.getChildren().clear();
				} else {
					hBoxPlayer3.getChildren().clear();
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

	public void showBetPane() {
		fundsAvailable = MatchHistory.getAmount(username);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				betPane.setVisible(true);
			}
		});
	}

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

	public void chatButtonPressed() {
		sendChat();
	}

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void increaseBet() {
		String betString = betAmount.getText();
		int bet = Integer.parseInt(betString);
		if (bet < fundsAvailable) {
			betAmount.setText(Integer.toString(bet + MINBET));
		}
	}

	public void hideLabel() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				label.setVisible(false);
			}
		});
	}

	public void decreaseBet() {
		String betString = betAmount.getText();
		int bet = Integer.parseInt(betString);
		if (bet > MINBET) {
			betAmount.setText(Integer.toString(bet - MINBET));
		}
	}

	public void placeBet() {
		String betString = betAmount.getText();
		int bet = Integer.parseInt(betString);
		MatchHistory.reduceAmount(username, bet);
		output.println("betIs " + bet);
		hideBetPane();
	}

}
