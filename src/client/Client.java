/**
 * Author: Group21 - Final version
 * Class Client: This class is used by each client
 */
package client;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import database.MatchHistory;
import database.Session;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import server.Deck;

import java.io.*;

import static javafx.scene.media.MediaPlayer.INDEFINITE;

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

	public void setGameController(GameController gameController) {
		this.gameController = gameController;
	}

	public void setInitialVariables(String username, String IP, int port) {
		this.username = username;
		this.IP = IP;
		this.port = port;
	}

	public boolean isInGame() {
		return inGame;
	}

	public boolean isGameFinished() {
		return gameFinished;
	}

	public boolean isBetPlaced() {
		return isBetPlaced;
	}

	@Override
	public void run() {
		try {
			onlinePlayers = new ArrayList<>();
			inQueue = new ArrayList<>();
			try {
				waitForController.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			socket = new Socket(IP, port);
			output = new PrintWriter(socket.getOutputStream(), true);
			lobbyController.setOutput(output);
			lobbyController.joinButton.setDisable(false);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println(username + " has joined");
			output.println(username);
			int forever = INDEFINITE;
			lobbyScreenMusic.setCycleCount(forever);
			if (!lobbyScreenMusic.isPlaying() && !lobbyController.muteButton.isSelected())
				lobbyScreenMusic.play(0.100);
			while (true) {
				inGame = false;
				playerLeft = false;
				String in = "";
				System.out.println("Client back in lobby");
				lobbyController.enableChat();
				while (true) { // Loops this until it reaches a 'break;'
					in = input.readLine();
					System.out.println("Client in: " + in);
					if (in.equals("accountAlreadyActive")) {
						lobbyController.connectionLost();
						return;
					}
					if (in.equals("Game Starting")) { // Reads the message received and responds accordingly
						System.out.println("break from lobby");
						output.println("breakFromLobby");
						break;
					}
					if (in.equals("queueJoined")) {
						lobbyController.queueJoined();
					}
					if (in.equals("queueLeft")) {
						lobbyController.queueLeft();
					}
					if (in.equals("Clear queue")) {
						lobbyController.updateData();
						lobbyController.clearQueue();
					}
					if (in.equals("Game in progress")) {
						lobbyController.gameInProgress(input.readLine());
					}
					if (in.startsWith("playerSignedOut")) {
						onlinePlayers.remove(in.replaceFirst("playerSignedOut", ""));
						lobbyController.addOnline(onlinePlayers);
					}
					if (in.startsWith("newPlayer")) {
						onlinePlayers.add(in.replaceFirst("newPlayer", ""));
						lobbyController.addOnline(onlinePlayers);
					}
					if (in.startsWith("activeGame")) {
						if (Boolean.parseBoolean(in.substring(10))) {
							lobbyController.joinUnavailable();
						}
					}
					if (in.equals("insufficientFunds")) {
						lobbyController.joinButton.setDisable(false);
						lobbyController.showAddFunds();
					}
					if (in.startsWith("playerJoinedQueue")) {
						String player = in.replaceFirst("playerJoinedQueue", "");
						inQueue.add(player);
						lobbyController.addQueue(inQueue);
					}
					if (in.startsWith("playerLeftQueue")) {
						String player = in.replaceFirst("playerLeftQueue", "");
						inQueue.remove(player);
						lobbyController.addQueue(inQueue);
					}
					if (in.startsWith("lobbyChatMessage")) {
						lobbyController.addToChat(in.replaceFirst("lobbyChatMessage", ""));
					}
				}

				lobbyController.gameBegin();
				
				try {
					waitForController.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lobbyScreenMusic.stop();
				gameScreenMusic.setCycleCount(forever);
				if (!gameScreenMusic.isPlaying() && !gameController.muteButton.isSelected()) {
					placeYourBets.play(20);
					gameScreenMusic.play(0.100);
				}
				lobbyController.disableChat();
				System.out.println("passed wait");
				gameFinished = false;
				playerLeft = false;
				isBetPlaced = false;
				inGame = true;
				int pointsAvailable = MatchHistory.getAmount(username);
				gameController.setOutput(output);
				gameController.setUsername(username);
				// Max of 3 players so reading one char is fine
				System.out.println(noPlayers);
				gameController.showBetPane();
				gameController.setPointsLabel("Funds available: " + String.valueOf(pointsAvailable));
				gameController.disableHit();
				gameController.hideLabel();
				gameController.disableStand();
				table = new ArrayList<>();
				table.add(new ArrayList<>());
				while (!gameFinished && !playerLeft) { // Loops this until it reaches a 'break;'
					in = input.readLine();
					System.out.println("Client in: " + in);
					if (in.startsWith("sessionID")) {
						sessionID = Integer.parseInt(in.replaceFirst("sessionID", ""));
					}
					if (in.startsWith("Welcome player ")) {
						ID = Integer.parseInt(in.substring(15, 16));
						noPlayers = Integer.parseInt(in.substring(26, 27));
						gameController.setNoPlayers(noPlayers);
						gameController.setID(ID);
					}
					if (in.startsWith("betIs")) {
						pointsAvailable = MatchHistory.getAmount(username);
						betAmount = Integer.parseInt(in.substring(6));
						Session.setWinnings(sessionID, username, -1*betAmount);
						gameController.setPointsLabel("Funds availlable: " + String.valueOf(pointsAvailable));
						isBetPlaced = true;
						output.println("betComplete");
					}
					if (in.equals("startCards")) {
						table.get(0).add(input.readLine());
						table.get(0).add(input.readLine()); // Next messages are the dealers first hands
						System.out.println(table.get(0) + " this is dealer");
						for (int i = 0; i < noPlayers; i++) {
							table.add(new ArrayList<>());
						}
						table.get(ID).add(input.readLine());
						table.get(ID).add(input.readLine()); // Player's first hands
						gameController.setLabel("Your hand: " + Deck.total(table.get(ID)) + "\nWait for your turn");
						System.out.println("Your Hand: " + table.get(ID) + " total: " + Deck.total(table.get(ID))); // Prints
						MatchHistory.setGamesPlayed(username, 1); // the
						gameController.setTable(table);
						pocketBlackJack = false;
						if (Deck.total(table.get(ID)) == 21) {
							System.out.println("Black Jack!");
							gameController.setLabel("Black Jack!");
							System.out.println("Your hand: " + table.get(ID) + " total: " + Deck.total(table.get(ID)));
							pocketBlackJack = true;
						}
					}
					if (in.equals("Make move")) { // Reads the message received and responds accordingly
						output.println("myTurn");
						if (pocketBlackJack) {
							output.println("p");
							gameController.disableHit();
							gameController.disableStand();
						} else {
							gameController.enableHit();
							gameController.enableStand();
							System.out.println(in + ", h (hit) p (pass)");
							System.out.println("Waiting for move");
							gameController.setLabel("Make Move: " + Deck.total(table.get(ID)));
						}
					}
					if (in.startsWith("gameChatMessage")) {
						gameController.addToChat(in.replaceFirst("gameChatMessage", ""));
					}
					if (in.startsWith("playerJoinedQueue")) {
						String player = in.replaceFirst("playerJoinedQueue", "");
						inQueue.add(player);
						lobbyController.addQueue(inQueue);
					}
					if (in.startsWith("playerLeftQueue")) {
						String player = in.replaceFirst("playerLeftQueue", "");
						inQueue.remove(player);
						lobbyController.addQueue(inQueue);
					}
					if (in.equals("playerLeftGame")) {
						playerLeft = true;
						gameController.disableHit();
						gameController.disableStand();
					}
					if (in.startsWith("playerCard")) {
						int playerID = Integer.parseInt(in.replaceFirst("playerCard", ""));
						String card = input.readLine().replaceFirst("playerCard", "");
						table.get(playerID).add(card);
						if (ID == playerID) {
							gameController.addCardToPlayerHand(card);
							if (Deck.total(table.get(ID)) > 21) {
								System.out.println("Break");
								gameController.setLabel("Busted: " + Deck.total(table.get(ID)));
								System.out.println(
										"Your hand: " + table.get(ID) + " total: " + Deck.total(table.get(ID)));
								output.println("busted");
								gameController.disableHit();
								gameController.disableStand();
							} else if (Deck.total(table.get(ID)) == 21) {
								System.out.println("Black Jack!");
								gameController.setLabel("Black Jack!");
								System.out.println(
										"Your hand: " + table.get(ID) + " total: " + Deck.total(table.get(ID)));
								output.println("p");
								gameController.disableHit();
								gameController.disableStand();
							} else {
								output.println("move");
								System.out.println(
										"Your hand: " + table.get(ID) + " total: " + Deck.total(table.get(ID)));
							}
						} else
							gameController.addCardToOpposingPlayerHand(getOtherPlayerID(playerID), "facedown.jpg");

					}
					if (in.equals("breakFromLoop")) {
						output.println("breakFromLoop");
					}
					if (in.equals("breakFromBetLoop")) {
						output.println("breakFromBetLoop");
					}
					if (in.equals("iHaveFinished")) { // Server tells the client its turn is over
						System.out.println("Your hand: " + table.get(ID) + " total: " + Deck.total(table.get(ID)));
						System.out.println(in + " turn... waiting for other players");
						gameController.setLabel("Your hand: " + Deck.total(table.get(ID)) + "\nWaiting for others");
						gameController.disableHit();
						gameController.disableStand();
					}
					if (in.startsWith("newPlayer")) {
						onlinePlayers.add(in.replaceFirst("newPlayer", ""));
						lobbyController.addOnline(onlinePlayers);
					}
					if (in.startsWith("showPlayerCards")) {
						System.out.println("displaying cards");
						System.out.println(table);
						for (int i = 1; i < table.size(); i++) {
							if (i != ID) {
								gameController.removeFacedown(getOtherPlayerID(i));
								for (int j = 0; j < table.get(i).size(); j++) {
									gameController.addCardToOpposingPlayerHand(getOtherPlayerID(i),
											table.get(i).get(j));
								}
							}
						}
					}
					if (in.startsWith("playerInitialCard")) {
						int playerID = Integer.parseInt(in.replaceFirst("playerInitialCard", ""));
						System.out.println("initial card received");
						table.get(playerID).add(input.readLine().replaceFirst("playerInitialCard", ""));
						table.get(playerID).add(input.readLine().replaceFirst("playerInitialCard", ""));
					}
					if (in.startsWith("playerSignedOut")) {
						onlinePlayers.remove(in.replaceFirst("playerSignedOut", ""));
						lobbyController.addOnline(onlinePlayers);
						if (in.replaceFirst("playerSignedOut", "").equals(username)) {
							lobbyController.connectionLost();
							return;
						}
					}
					if (in.startsWith("playersFinished")) { // Server tells client what to display
						System.out.println("All players finished");
						in = input.readLine();
						if (!in.equals("skipDealer")) {
							gameController.removeDealerFacedown();
							gameController.addCardToDealerHand(table.get(0).get(1));
							gameController.setDealerLabel("Dealer: " + Deck.total(table.get(0)));
						} else {
							gameFinished = true;

						}
					}
					if (in.startsWith("dealerCard")) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						String dealerCard = in.replaceFirst("dealerCard", "");
						table.get(0).add(dealerCard);
						gameController.addCardToDealerHand(dealerCard);
						gameController.setDealerLabel("Dealer: " + Deck.total(table.get(0)));
					}
					if (in.startsWith("dealerDone")) {
						gameFinished = true;

					}
					if (in.equals("Clear queue")) {
						lobbyController.clearQueue();
					}
				}
				if (!playerLeft) {
					System.out.println(table.get(0));
					declareWinner();
				}
				table.clear();
				inQueue.clear();
				gameController.endChat();
				gameController.showLeaveButton();
				lobbyController.updateData();
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

	public void signOut() {
		lobbyScreenMusic.stop();
		gameScreenMusic.stop();
		output.println("thisPlayerSignedOut");
	}

	/*
	 * The following calculates the result of the game using the total scores of the
	 * clients hand and dealers hand
	 */
	public void declareWinner() {
		System.out.println("Dealers cards: " + table.get(0) + " total: " + Deck.total(table.get(0)));
		if (Deck.total(table.get(ID)) > 21) {
			gameController.setLabel("Bust!! You lose!");
			Session.setWinnings(sessionID, username, -1 * betAmount);
			if (!gameController.muteButton.isSelected())
				dealerWins.play(20);
		} else if (Deck.total(table.get(0)) > 21) {
			MatchHistory.setGamesWon(username, 1);
			MatchHistory.increaseAmount(username, 2 * betAmount); // this should be 1.5
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
			MatchHistory.setGamesWon(username, 1);
			MatchHistory.increaseAmount(username, 2 * betAmount); // this should be 1.5
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
		gameController.setPointsLabel("Funds available: " + String.valueOf(MatchHistory.getAmount(username)));
	}

	public void closeGame(Stage window) {
		boolean confirmation = GameController.displayConfirmBox("Warning", "Are you sure you want to exit?");
		if (confirmation) {
			window.close();
			stopGameMusic();
			gameController.playerLeft();
		}
	}

	public void stopGameMusic() {
		gameScreenMusic.stop();
		if (!lobbyScreenMusic.isPlaying() && !lobbyController.muteButton.isSelected())
			lobbyScreenMusic.play(0.100);
	}

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
