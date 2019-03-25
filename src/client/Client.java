package client;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import database.MatchHistory;
import database.Session;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import server.Deck;

import java.io.*;

import static javafx.scene.media.MediaPlayer.INDEFINITE;

/**
 * Class used to handle requests from the server
 *
 * @author George
 *
 */
public class Client implements Runnable {

	Semaphore waitForController;
	GameController gameController;
	LobbyController lobbyController;
	private int ID;
	private int noPlayers;
	private List<List<String>> table;
	private PrintWriter output;
	private Socket socket;
	private BufferedReader input;
	private List<String> onlinePlayers;
	private String username;
	private String IP;
	private List<String> inQueue;
	private boolean playerLeft;
	private boolean pocketBlackJack;
	private boolean inGame = false;
	private boolean gameFinished;
	private int sessionID;
	private int betAmount;
	private boolean isBetPlaced;
	private int port;

	protected AudioClip lobbyScreenMusic = new AudioClip(getClass().getResource("/music/LobbyMusic.wav").toExternalForm());
	protected AudioClip gameScreenMusic = new AudioClip(getClass().getResource("/music/InGameMusic.wav").toExternalForm());
	protected AudioClip placeYourBets = new AudioClip(
			getClass().getResource("/music/PleasePlaceYourBets.wav").toExternalForm());
	protected AudioClip playerWins = new AudioClip(getClass().getResource("/music/PlayerWins.wav").toExternalForm());
	protected AudioClip dealerWins = new AudioClip(getClass().getResource("/music/DealerWins.wav").toExternalForm());
	protected AudioClip draw = new AudioClip(getClass().getResource("/music/Draw.wav").toExternalForm());

	/**
	 * Constructor for the client object
	 * @param table the structure to hold all of the cards on the table
	 * @param waitForController semaphore to ensure controller is initialised before using it
	 * @param lobbyController the controller that creates an instance of client
	 */
	public Client(List<List<String>> table, Semaphore waitForController, LobbyController lobbyController) {
		this.table = table;
		this.waitForController = waitForController;
		output = null;
		this.lobbyController = lobbyController;
		gameController = null;
		this.pocketBlackJack = false;
		this.gameFinished = false;
		this.isBetPlaced = false;
	}
	
	/**
	 * Setter for controller for when user is in game
	 * @param gameController the game screen controller
	 */
	public void setGameController(GameController gameController) {
		this.gameController = gameController;
	}

	/**
	 * Method to set some important initial values 
	 * @param username the username of the user
	 * @param IP the IP to connect to 
	 * @param port the port to connect to
	 */
	public void setInitialVariables(String username, String IP, int port) {
		this.username = username;
		this.IP = IP;
		this.port = port;
	}
	
	/**
	 * Method to determine whether a user is in game
	 * @return is the user in the game?
	 */
	public boolean isInGame() {
		return inGame;
	}

	/**
	 * Method to determine whether the game is finished, different to isInGame as a user can leave without ending the game
	 * @return is the game finished?
	 */
	public boolean isGameFinished() {
		return gameFinished;
	}

	/**
	 * Determines whether the user has placed bet
	 * @return have they placed their bet?
	 */
	public boolean isBetPlaced() {
		return isBetPlaced;
	}

	/**
	 * Method to handle server messages
	 */
	@Override
	public void run() {
		try {
			onlinePlayers = new ArrayList<>();
			inQueue = new ArrayList<>(); //Creates two lists
			try {
				waitForController.acquire(); //ensures the controller has finished loading
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			socket = new Socket(IP, port); //creates socket with IP and port
			output = new PrintWriter(socket.getOutputStream(), true);
			lobbyController.setOutput(output);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println(username + " has joined");
			output.println(username);
			int forever = INDEFINITE;
			lobbyScreenMusic.setCycleCount(forever);
			if (!lobbyScreenMusic.isPlaying() && !lobbyController.muteButton.isSelected()) //Only starts music if it is not playing and not muted
				lobbyScreenMusic.play(0.100); //Starts lobby music
			while (true) {
				inGame = false;
				playerLeft = false;
				String in = "";
				lobbyController.enableChat();
				/*
				 * Waits in this loop whilst user is in the lobby
				 * Input is read then response is decided using the conditional statements
				 * Messages are intended to be human readable to some extent
				 */
				lobbyController.muteButton.setDisable(false);
				while (true) { // Loops this until it reaches a 'break;'
					in = input.readLine();
					if (in.equals("accountAlreadyActive")) {
						lobbyController.connectionLost();
						return;
					}
					if (in.equals("Game Starting")) { // Reads the message received and responds accordingly
						output.println("breakFromLobby");
						break;
					}
					if (in.equals("queueJoined")) {
						lobbyController.queueJoined(); //Adds player to queue
					}
					if (in.equals("queueLeft")) {
						lobbyController.queueLeft(); //Removes player from queue
					}
					if (in.equals("Clear queue")) {
						lobbyController.updateData();
						lobbyController.clearQueue(); //Sets relevant UI elements at the end of a game
					}
					if (in.startsWith("Game in progress")) {
						lobbyController.gameInProgress(in.replaceFirst("Game in progress", ""));
					}
					if (in.startsWith("playerSignedOut")) {
						onlinePlayers.remove(in.replaceFirst("playerSignedOut", "")); //Removes the player that signed out
						lobbyController.addOnline(onlinePlayers);
					}
					if (in.startsWith("newPlayer")) {
						onlinePlayers.add(in.replaceFirst("newPlayer", "")); //Adds new player to list
						lobbyController.addOnline(onlinePlayers);
					}
					if (in.startsWith("activeGame")) {
						if (Boolean.parseBoolean(in.substring(10))) {
							lobbyController.joinUnavailable(); //Determines whether a game is in progress
						}else {
							lobbyController.joinButton.setDisable(false);
						}
					}
					if (in.equals("insufficientFunds")) {
						lobbyController.joinButton.setDisable(false); 
						lobbyController.showAddFunds();
					}
					if (in.startsWith("playerJoinedQueue")) {
						String player = in.replaceFirst("playerJoinedQueue", ""); //Updates game queue
						inQueue.add(player);
						lobbyController.addQueue(inQueue);
					}
					if (in.startsWith("playerLeftQueue")) {
						String player = in.replaceFirst("playerLeftQueue", ""); //Updates game queue
						inQueue.remove(player);
						lobbyController.addQueue(inQueue);
					}
					if (in.startsWith("lobbyChatMessage")) {
						lobbyController.addToChat(in.replaceFirst("lobbyChatMessage", ""));
					}
				}
				lobbyController.muteButton.setDisable(true);
				lobbyController.gameBegin(); //Loads the game screen
				
				try {
					waitForController.acquire(); //Ensures controller is loaded before using it
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lobbyScreenMusic.stop();
				gameScreenMusic.setCycleCount(forever);
				if (!gameScreenMusic.isPlaying() && !gameController.muteButton.isSelected()) {
					placeYourBets.play(20); 
					gameScreenMusic.play(0.100); //Starts game music and sound effect
				}
				lobbyController.disableChat();
				gameFinished = false;
				playerLeft = false;
				isBetPlaced = false;
				inGame = true;
				int pointsAvailable = MatchHistory.getAmount(username); //Gets the funds of the user
				gameController.setOutput(output);
				gameController.setUsername(username);
				gameController.showBetPane();
				gameController.setPointsLabel("Funds available: " + String.valueOf(pointsAvailable));
				gameController.disableHit();
				gameController.hideLabel();
				gameController.disableStand(); //Sets game screen up to accept bet
				table = new ArrayList<>();
				table.add(new ArrayList<>()); //Makes space for the dealer's cards
				/*
				 * Waits in this loop whilst user is in game
				 */
				while (!gameFinished && !playerLeft) { // Loops this until it reaches a 'break;'
					in = input.readLine();
					if (in.startsWith("sessionID")) {
						sessionID = Integer.parseInt(in.replaceFirst("sessionID", "")); //Saves the session ID
					}
					if (in.startsWith("Welcome player ")) {
						ID = Integer.parseInt(in.substring(15, 16)); //Finds the user's ID from the welcome message
						noPlayers = Integer.parseInt(in.substring(26, 27));
						gameController.setNoPlayers(noPlayers);
						gameController.setID(ID); //Sets the ID
					}
					if (in.startsWith("betIs")) { //Receives the users bet amount
						pointsAvailable = MatchHistory.getAmount(username); 
						betAmount = Integer.parseInt(in.substring(6));
						Session.setWinnings(sessionID, username, -1*betAmount);
						gameController.setPointsLabel("Funds availlable: " + String.valueOf(pointsAvailable)); //Updates their funds
						isBetPlaced = true; 
						output.println("betComplete");
					}
					if (in.equals("startCards")) { //Receives the initial cards
						table.get(0).add(input.readLine());
						table.get(0).add(input.readLine()); // First two cards are dealer's cards
						for (int i = 0; i < noPlayers; i++) {
							table.add(new ArrayList<>()); //Makes room for all players
						}
						table.get(ID).add(input.readLine());
						table.get(ID).add(input.readLine()); // Player's first cards
						gameController.setLabel("Your hand: " + Deck.total(table.get(ID)) + "\nWait for your turn");
						MatchHistory.setGamesPlayed(username, 1); //Increased the number of games played
						gameController.setTable(table); //Displays the cards
						pocketBlackJack = false;
						if (Deck.total(table.get(ID)) == 21) { //Determines whether hand is blackjack
							gameController.setLabel("Black Jack!");
							pocketBlackJack = true;
						}
					}
					if (in.equals("Make move")) { // Reads the message received and responds accordingly
						output.println("myTurn"); //tells the server that it is the users turn
						if (pocketBlackJack) {
							output.println("p"); //force stand if hand is blackjack
							gameController.disableHit();
							gameController.disableStand(); //disable move buttons
						} else {
							gameController.enableHit();
							gameController.enableStand(); //enable move buttons
							gameController.setLabel("Make Move: " + Deck.total(table.get(ID))); //Label changed to tell user to make move
						}
					}
					if (in.startsWith("gameChatMessage")) {
						gameController.addToChat(in.replaceFirst("gameChatMessage", "")); //Adds the chat message to the chat box
					}
					if (in.startsWith("playerJoinedQueue")) {
						String player = in.replaceFirst("playerJoinedQueue", "");
						inQueue.add(player);
						lobbyController.addQueue(inQueue);
					}
					if (in.startsWith("playerLeftQueue")) {
						String player = in.replaceFirst("playerLeftQueue", ""); //Updates game queue
						inQueue.remove(player);
						lobbyController.addQueue(inQueue);
					}
					if (in.equals("playerLeftGame")) {
						playerLeft = true;
						gameController.disableHit(); //Disables buttons if the player is leaving
						gameController.disableStand();
					}
					if (in.startsWith("playerCard")) {
						int playerID = Integer.parseInt(in.replaceFirst("playerCard", "")); //Finds which player the card is for
						String card = input.readLine().replaceFirst("playerCard", ""); //Finds the card
						table.get(playerID).add(card); //Adds card to relevant hand
						if (ID == playerID) { 
							gameController.addCardToPlayerHand(card); //If its the players card then display it 
							if (Deck.total(table.get(ID)) > 21) { //If hand bust then enter
								gameController.setLabel("Busted: " + Deck.total(table.get(ID)));
								output.println("busted"); //Tell server turn is over
								gameController.disableHit();
								gameController.disableStand(); //disable buttons when bust
							} else if (Deck.total(table.get(ID)) == 21) {
								gameController.setLabel("Black Jack!");
								output.println("p"); //Force stand when blackjack
								gameController.disableHit();
								gameController.disableStand(); //Disable buttons after stand
							} else {
								output.println("move"); //tells the server to ask for another move
							}
						} else
							gameController.addCardToOpposingPlayerHand(getOtherPlayerID(playerID), "facedown.jpg"); //If card is for another player then show a face down card

					}
					if (in.equals("breakFromLoop")) {
						output.println("breakFromLoop"); //Tells server to break from loop it waits in for other players to finish turn
					}
					if (in.equals("breakFromBetLoop")) {
						output.println("breakFromBetLoop"); //Tells server to break from loop it waits in for other players to place bet
					}
					if (in.equals("iHaveFinished")) { // Server tells the client its turn is over
						gameController.setLabel("Your hand: " + Deck.total(table.get(ID)) + "\nWaiting for others"); //Change label to waiting for others
						gameController.disableHit();
						gameController.disableStand(); //Ensure buttons are disabled
					}
					if (in.startsWith("newPlayer")) {
						onlinePlayers.add(in.replaceFirst("newPlayer", ""));
						lobbyController.addOnline(onlinePlayers); //Add a new player to the online list 
					}
					if (in.startsWith("showPlayerCards")) {
						for (int i = 1; i < table.size(); i++) {
							if (i != ID) {
								gameController.removeFacedown(getOtherPlayerID(i));
								for (int j = 0; j < table.get(i).size(); j++) {
									gameController.addCardToOpposingPlayerHand(getOtherPlayerID(i), //Displays the other player's cards
											table.get(i).get(j));
								}
							}
						}
					}
					if (in.startsWith("playerInitialCard")) {
						int playerID = Integer.parseInt(in.replaceFirst("playerInitialCard", ""));
						table.get(playerID).add(input.readLine().replaceFirst("playerInitialCard", ""));
						table.get(playerID).add(input.readLine().replaceFirst("playerInitialCard", "")); //Adds other player's initial cards to table as initial cards are only sent to the relevant client at the start
					}
					if (in.startsWith("playerSignedOut")) {
						onlinePlayers.remove(in.replaceFirst("playerSignedOut", "")); //Updates the list of online players
						lobbyController.addOnline(onlinePlayers);
						if (in.replaceFirst("playerSignedOut", "").equals(username)) { //If the player signing out is this player then enter
							lobbyController.signOut(); //Signs the player out
							return;
						}
					}
					if (in.startsWith("playersFinished")) { // Server tells client what to display
						in = input.readLine();
						if (!in.equals("skipDealer")) {
							gameController.removeDealerFacedown(); //Displays dealer's cards
							gameController.addCardToDealerHand(table.get(0).get(1));
							gameController.setDealerLabel("Dealer: " + Deck.total(table.get(0)));
						} else {
							gameFinished = true;

						}
					}
					if (in.startsWith("dealerCard")) {
						try {
							Thread.sleep(1000); //Simulates real draw by adding a card every second
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						String dealerCard = in.replaceFirst("dealerCard", "");
						table.get(0).add(dealerCard); //Adds cards to dealers hand
						gameController.addCardToDealerHand(dealerCard); //displays new card
						gameController.setDealerLabel("Dealer: " + Deck.total(table.get(0)));
					}
					if (in.startsWith("dealerDone")) {
						gameFinished = true; //Game finished when dealer finished

					}
					if (in.equals("Clear queue")) {
						lobbyController.clearQueue(); //Clear the game queue when game ends
					}
				}
				if (!playerLeft) {
					declareWinner(); //If the player did not leave the game then calculate the result
				}
				table.clear();
				inQueue.clear(); //Clear the variables ready for next game
				gameController.endChat(); //Close the chat
				gameController.showLeaveButton(); //Display leave button
				lobbyController.updateData(); //Update the user's info
			}
		} catch (IOException e) {
			System.out.println("Session not joinable");
			e.printStackTrace();
			if (inGame)
				gameController.connectionLost();
			lobbyController.connectionLost();
			output.close();
			return;
		}
	}

	/**
	 * Method to sign a player out
	 */
	public void signOut() {
		lobbyScreenMusic.stop(); //Stops the lobby music
		gameScreenMusic.stop(); //Starts the login music
		output.println("thisPlayerSignedOut");
	}
	
	
	/**
	 * The following calculates the result of the game using the total scores of the
	 * clients hand and dealers hand
	 */
	public void declareWinner() {
		if (Deck.total(table.get(ID)) > 21) {
			gameController.setLabel("Bust!! You lose!");	//Displays the result
			Session.setWinnings(sessionID, username, -1 * betAmount); //Updates the winnings for the game
			if (!gameController.muteButton.isSelected())
				dealerWins.play(20); //Announces the result
		} else if (Deck.total(table.get(0)) > 21) {
			MatchHistory.setGamesWon(username, 1);
			MatchHistory.increaseAmount(username, 2 * betAmount); 
			Session.setSessionResult(sessionID, username, true);
			gameController.setLabel("Dealer bust! You Win!");
			Session.setWinnings(sessionID, username, betAmount);
			if (!gameController.muteButton.isSelected())
				playerWins.play(20);
		} else if (Deck.total(table.get(ID)) == Deck.total(table.get(0))) {
			gameController.setLabel("Draw!");
			if (!gameController.muteButton.isSelected())
				draw.play(20);
			Session.setWinnings(sessionID, username, 0);
			MatchHistory.increaseAmount(username, betAmount); // take money back
		} else if (Deck.total(table.get(ID)) > Deck.total(table.get(0))) {
			Session.setSessionResult(sessionID, username, true);
			MatchHistory.setGamesWon(username, 1); //Updates the number of games won
			MatchHistory.increaseAmount(username, 2 * betAmount); //Give winnings
			Session.setWinnings(sessionID, username, betAmount);
			gameController.setLabel("You win!!");
			if (!gameController.muteButton.isSelected())
				playerWins.play(20);
		} else {
			gameController.setLabel("Dealer Wins!!");
			Session.setWinnings(sessionID, username, -1 * betAmount);
			if (!gameController.muteButton.isSelected())
				dealerWins.play(20);
		}
		gameController.setPointsLabel("Funds available: " + String.valueOf(MatchHistory.getAmount(username))); //Updates the available funds
	}

	/**
	 * Method to close game window after confirmation
	 * @param window the game window
	 */
	
	public void closeGame(Stage window) {
		boolean confirmation = GameController.displayConfirmBox("Warning", "Are you sure you want to exit?");
		if (confirmation) {
			window.close();
			stopGameMusic();
			gameController.playerLeft();
		}
	}

	/**
	 * Method to stop game music
	 */
	public void stopGameMusic() {
		gameScreenMusic.stop();
		if (!lobbyScreenMusic.isPlaying() && !lobbyController.muteButton.isSelected())
			lobbyScreenMusic.play(0.100);
	}

	/**
	 * Method to convert other players ID to the correct UI position
	 * @param playerID the other players ID to check
	 * @return the UI position of the player
	 */
	public int getOtherPlayerID(int playerID) {
		switch (ID) {
		case 1:
			return playerID;
		case 2:
			return ((playerID % noPlayers) + (noPlayers - 1));
		case 3:
			return playerID + 1;
		default:
			return -1;
		}
	}
}
