package client;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.security.auth.kerberos.KerberosKey;

import com.sun.glass.ui.TouchInputSupport;

import database.MatchHistory;
import database.Session;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import server.Deck;
import shareable.FinishedPlayers;

import java.io.*;

public class Client implements Runnable {

	// This is a test comment to see if GIT works
	// harris test

	// This is a test comment to see if GIT works
	// Nick test
	// Eclipse test
	// Hello World

	// Lobby branch test

	// Test to LobbyBranch

	Semaphore waitForController;
	GameController gameController;
	LobbyController lobbyController;
	private int ID;
	private int noPlayers;
	private List<List<String>> table;
	private PrintWriter output;
	private List<String> onlinePlayers;
	private String username;
	private String IP;
	private List<String> inQueue;
	private boolean playerLeft;
	private boolean pocketBlackJack;
	private boolean inGame = false;
	private boolean gameFinished;
	private int sessionID;

	public Client(List<List<String>> table, Semaphore waitForController, String IP, LobbyController lobbyController) {
		this.table = table;
		this.waitForController = waitForController;
		output = null;
		this.IP = IP;
		this.lobbyController = lobbyController;
		gameController = null;
		this.pocketBlackJack = false;
		this.gameFinished = false;
	}

	public void setGameController(GameController gameController) {
		this.gameController = gameController;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isGameFinished() {
		return gameFinished;
	}

	@Override
	public void run() {
		try (Socket socket = new Socket(IP, 9999);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
			output = new PrintWriter(socket.getOutputStream(), true);
			onlinePlayers = new ArrayList<>();
			inQueue = new ArrayList<>();
			try {
				waitForController.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			setUsername(lobbyController.getUsername());
			System.out.println(username + " has joined");
			output.println(username);
			while (true) {
				inGame = false;
				playerLeft = false;
				String in = "";
				lobbyController.setOutput(output);
				System.out.println("Client back in lobby");
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
						lobbyController.clearQueue();
					}
					if (in.equals("Game in progress")) {
						lobbyController.gameInProgress(input.readLine());
					}
					if (in.contains("playerSignedOut")) {
						onlinePlayers.remove(in.replaceFirst("playerSignedOut", ""));
						lobbyController.addOnline(onlinePlayers);
					}
					if (in.contains("newPlayer")) {
						onlinePlayers.add(in.replaceFirst("newPlayer", ""));
						lobbyController.addOnline(onlinePlayers);
					}
					if (in.contains("activeGame")) {
						if (Boolean.parseBoolean(in.substring(10))) {
							lobbyController.joinUnavailable();
						}
					}
					if (in.equals("playerQueue")) {
						in = input.readLine();
						inQueue.clear();
						while (!in.equals("queueUpdated")) {
							inQueue.add(in.replaceFirst("playerQueue", ""));
							in = input.readLine();
						}
						lobbyController.addQueue(inQueue);
					}
					if (in.contains("lobbyChatMessage")) {
						lobbyController.addToChat(in.replaceFirst("lobbyChatMessage", ""));
					}
				}

				lobbyController.gameBegin();
				String hello = input.readLine();
				System.out.println(hello); // The first message received is the greeting message so just print this
				ID = Integer.parseInt(hello.substring(15, 16));
				try {
					waitForController.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				gameFinished = false;
				sessionID = Integer.parseInt(input.readLine().replaceFirst("sessionID", ""));
				Session.setSessionPoints(sessionID, username, false);
				gameController.setOutput(output);
				gameController.setUsername(username);
				gameController.setID(ID);
				inGame = true;
				noPlayers = Integer.parseInt(hello.substring(26, 27)); // Max of 3 players so reading one char is fine
				gameController.setNoPlayers(noPlayers);
				System.out.println(noPlayers);
				table.add(new ArrayList<>());
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
				// players hand
				gameController.setTable(table);
				if (Deck.total(table.get(ID)) == 21 && table.get(ID).size() == 2) {
					System.out.println("Black Jack!");
					gameController.setLabel("Black Jack!");
					System.out.println("Your hand: " + table.get(ID) + " total: " + Deck.total(table.get(ID)));
					pocketBlackJack = true;
				}
				while (!gameFinished && !playerLeft) { // Loops this until it reaches a 'break;'
					in = input.readLine();
					System.out.println("Client in: " + in);
					if (in.equals("Make move")) { // Reads the message received and responds accordingly
						if (pocketBlackJack) {
							output.println("p");
							gameController.disableHit();
							gameController.disableStand();
							// break;
						} else {
							gameController.enableHit();
							gameController.enableStand();
							System.out.println(in + ", h (hit) p (pass)");
							System.out.println("Waiting for move");
							gameController.setLabel("Make Move: " + Deck.total(table.get(ID)));
						}
					}
					if (in.contains("gameChatMessage")) {
						gameController.addToChat(in.replaceFirst("gameChatMessage", ""));
					}
					if (in.equals("playerQueue")) {
						in = input.readLine();
						inQueue.clear();
						while (!in.equals("queueUpdated")) {
							inQueue.add(in.replaceFirst("playerQueue", ""));
							in = input.readLine();
						}
						lobbyController.addQueue(inQueue);
					}
					if (in.equals("playerLeftGame")) {
						playerLeft = true;
						gameController.disableHit();
						gameController.disableStand();
						// break;
					}
					if (in.contains("playerCard")) {
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
								// break;
							} else if (Deck.total(table.get(ID)) == 21) {
								System.out.println("Black Jack!");
								gameController.setLabel("Black Jack!");
								System.out.println(
										"Your hand: " + table.get(ID) + " total: " + Deck.total(table.get(ID)));
								output.println("p");
								gameController.disableHit();
								gameController.disableStand();
								// break;
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
					if (in.contains("finished")) { // Server tells the client its turn is over
						System.out.println("Your hand: " + table.get(ID) + " total: " + Deck.total(table.get(ID)));
						System.out.println(in + " turn... waiting for other players");
						gameController.setLabel("Your hand: " + Deck.total(table.get(ID)) + "\nWaiting for others");
						gameController.disableHit();
						gameController.disableStand();
						// break;
					}
					if (in.contains("newPlayer")) {
						onlinePlayers.add(in.replaceFirst("newPlayer", ""));
						lobbyController.addOnline(onlinePlayers);
					}
					if (in.contains("showPlayerCards")) {
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
					if (in.contains("playerInitialCard")) {
						int playerID = Integer.parseInt(in.replaceFirst("playerInitialCard", ""));
						System.out.println("initial card received");
						table.get(playerID).add(input.readLine().replaceFirst("playerInitialCard", ""));
						table.get(playerID).add(input.readLine().replaceFirst("playerInitialCard", ""));
					}

					if (in.contains("playerSignedOut")) {
						onlinePlayers.remove(in.replaceFirst("playerSignedOut", ""));
						lobbyController.addOnline(onlinePlayers);
						if (in.replaceFirst("playerSignedOut", "").equals(username)) {
							lobbyController.connectionLost();
						}
						return;
					}
					if (in.contains("playersFinished")) { // Server tells client what to display
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
					if (in.contains("dealerCard")) {
						// sleep thread for 1s in order to simulate the dealer picking cards one by one
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
					if (in.contains("dealerDone")) {
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

	public List<String> extractCards(String hand) {
		List<String> cards = new ArrayList<>();
		hand = hand.substring(1, hand.length() - 1); // String is in the form [card1, card2, ...]
		String[] arr = hand.split(", ");
		cards.addAll(Arrays.asList(arr));
		return cards;
	}

	/*
	 * The following calculates the result of the game using the total scores of the
	 * clients hand and dealers hand
	 */
	public void declareWinner() {
		System.out.println("Dealers cards: " + table.get(0) + " total: " + Deck.total(table.get(0)));
		if (Deck.total(table.get(ID)) > 21) {
			gameController.setLabel("Bust!! You lose!");
		} else if (Deck.total(table.get(0)) > 21) {
			MatchHistory.setGamesWon(username, 1);
			Session.setSessionPoints(sessionID, username, true);
			gameController.setLabel("Dealer bust! You Win!");
		} else if (Deck.total(table.get(ID)) == Deck.total(table.get(0))) {
			gameController.setLabel("Draw!");
		} else if (Deck.total(table.get(ID)) > Deck.total(table.get(0))) {
			Session.setSessionPoints(sessionID, username, true);
			MatchHistory.setGamesWon(username, 1);
			gameController.setLabel("You win!!");
		} else {
			gameController.setLabel("Dealer Wins!!");
		}
	}

	public void closeGame(Stage window) {
		boolean confirmation = GameController.displayConfirmBox("Warning", "Are you sure you want to exit?");
		if (confirmation) {
			window.close();
			gameController.playerLeft();
		}
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
