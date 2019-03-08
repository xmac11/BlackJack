package gui;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;


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

	Semaphore waitForServer;
	Semaphore chatWait;
	GameController gameController;
	LobbyController lobbyController;
	Semaphore waitForInput;
	private int ID;
	private int noPlayers;
	private List<List<String>> table;
	private PrintWriter output;
	private Socket socket;
	private List<String> onlinePlayers;
	private String username;
	private String IP;
	private List<String> inQueue;
	private boolean playerLeft;
	private boolean pocketBlackJack;

	public Client(List<List<String>> table, Semaphore waitForServer, Semaphore waitForInput, Semaphore chatWait,
			String IP, LobbyController lobbyController) {
		this.table = table;
		this.waitForServer = waitForServer;
		this.waitForInput = waitForInput;
		output = null;
		this.chatWait = chatWait;
		this.IP = IP;
		this.lobbyController = lobbyController;
		gameController = null;
		this.pocketBlackJack = false;
	}

	public void setGameController(GameController gameController) {
		this.gameController = gameController;
	}

	public int total(List<String> a) {
		int sum = 0;
		int value;
		for (int i = 0; i < a.size(); i++) {
			String cardValue = a.get(i).replaceAll(" .*", ""); // remove everything after the space			
			try {
				value = Integer.parseInt(cardValue);
			}
			catch(NumberFormatException e) {
				if(cardValue.equals("A")) 
					value = 11;							
				else 
					value = 10;
			}
			sum += value;
		}
		if (sum > 21 && a.stream().anyMatch(x -> x.startsWith("A"))) { // if busted, but hand contains an Ace
			sum -= 10;
		}
		return sum;
	}


	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public void run() {
		try (Socket socket = new Socket(IP, 9999);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
			output = new PrintWriter(socket.getOutputStream(), true);
			onlinePlayers = new ArrayList<>();
			inQueue = new ArrayList<>();
			System.out.println(username + " has joined");
			setUsername(lobbyController.getUsername());
			output.println(username);
			while (true) {
				playerLeft = false;
				String in = "";
				lobbyController.setOutput(output);
				System.out.println("Client back in lobby");
				while (true) { // Loops this until it reaches a 'break;'
					in = input.readLine();
					System.out.println("Client in: " + in);
					if (in.equals("Game Starting")) { // Reads the message received and responds accordingly
						output.println("breakFromLobby");
						break;
					}
					if (in.equals("queueJoined")) {
						lobbyController.queueJoined();
					}
					if (in.equals("Clear queue")) {
						lobbyController.clearQueue();
					}
					if (in.equals("Game in progress")) {
						lobbyController.gameInProgress(input.readLine());
					}
					if (in.equals("newPlayer")) {
						in = input.readLine();
						onlinePlayers.clear();
						while (!in.equals("playersUpdated")) {
							onlinePlayers.add(in.substring(9));
							in = input.readLine();
						}
						lobbyController.addOnline(onlinePlayers);
					}
					if(in.contains("activeGame")) {
						if(Boolean.parseBoolean(in.substring(10))) {
							lobbyController.joinUnavailable();
						}
					}
					if (in.equals("playerQueue")) {
						in = input.readLine();
						inQueue.clear();
						while (!in.equals("queueUpdated")) {
							inQueue.add(in.substring(11));
							in = input.readLine();
						}
						lobbyController.addQueue(inQueue);
					}
					if (in.contains("lobbyChatMessage")) {
						lobbyController.addToChat(in.substring(16));
					}
				}
				lobbyController.gameBegin();
				String hello = input.readLine();
				System.out.println(hello); // The first message received is the greeting message so just print this
				ID = Integer.parseInt(hello.substring(15, 16));
				try {
					waitForServer.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				gameController.setOutput(output);
				gameController.setUsername(username);
				gameController.setID(ID);
				noPlayers = Integer.parseInt(hello.substring(26, 27));
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
				gameController.setLabel("Your hand: " + total(table.get(ID)));
				System.out.println("Your Hand: " + table.get(ID) + " total: " + total(table.get(ID))); // Prints the
																										// players hand
				gameController.setTable(table);
				if(total(table.get(ID)) == 21) {
					System.out.println("Black Jack!");
					gameController.setLabel("Black Jack!" + total(table.get(ID)));
					System.out.println("Your hand: " + table.get(ID) + " total: " + total(table.get(ID)));					
					pocketBlackJack = true;
				}
				while (true) { // Loops this until it reaches a 'break;'
					if(pocketBlackJack) break;						
					in = input.readLine();
					System.out.println("Client in: " + in);
					if (in.contains("resend")) {
						output.println(in.substring(6));
					} else {
						if (in.equals("Make move")) { // Reads the message received and responds accordingly
							gameController.enableHit();
							gameController.enableStand();
							System.out.println(in + ", h (hit) p (pass)");
							System.out.println("Waiting for move");
							gameController.setLabel("Make Move: " + total(table.get(ID)));
						}
						if (in.contains("gameChatMessage")) {
							gameController.addToChat(in.substring(15));
						}
						if (in.equals("playerQueue")) {
							in = input.readLine();
							inQueue.clear();
							while (!in.equals("queueUpdated")) {
								inQueue.add(in.substring(11));
								in = input.readLine();
							}
							lobbyController.addQueue(inQueue);
						}
						if (in.equals("playerLeft")) {
							playerLeft = true;
							break;
						}
						if (in.contains("playerCard")) {
							String card = in.substring(10);
							table.get(ID).add(card);
							gameController.addCardToPlayerHand(card);
							if (total(table.get(ID)) > 21) {
								System.out.println("Break");
								gameController.setLabel("Busted: " + total(table.get(ID)));
								System.out.println("Your hand: " + table.get(ID) + " total: " + total(table.get(ID)));
								output.println("p");
								break;
							} 
							else if(total(table.get(ID)) == 21) {
								System.out.println("Black Jack!");
								gameController.setLabel("Black Jack!" /*+ total(table.get(ID))*/);
								System.out.println("Your hand: " + table.get(ID) + " total: " + total(table.get(ID)));
								output.println("p");
								break;
							} 
							else {
								output.println("move");
								System.out.println("Your hand: " + table.get(ID) + " total: " + total(table.get(ID)));
							}
						}
						if (in.contains("finished")) { // Server tells the client its turn is over
							System.out.println("Your hand: " + table.get(ID) + " total: " + total(table.get(ID)));
							System.out.println(in + " turn... waiting for other players");
							gameController.setLabel("Your hand: " + total(table.get(ID)) + "\nWaiting for others");
							break;
						}
						if (in.equals("newPlayer")) {
							in = input.readLine();
							onlinePlayers.clear();
							while (!in.equals("playersUpdated")) {
								onlinePlayers.add(in.substring(9));
								in = input.readLine();
							}
							lobbyController.addOnline(onlinePlayers);
						}
					}
				}
				if (!playerLeft) {
					gameController.disableHit();
					gameController.disableStand();
					//gameController.setLabel("Your hand: " + total(table.get(ID)));
					boolean dealerTurn = true;
					while (dealerTurn) { // Sits in loop whilst dealer chooses new cards
						in = input.readLine();
						if (in.contains("resend")) {
							output.println(in.substring(6));
						} else {
							if (in.contains("gameChatMessage")) {
								gameController.addToChat(in.substring(15, 16) + " > " + in.substring(16));
							}
							if (in.equals("breakFromLoop")) {
								output.println("reader");
							}
							if (in.equals("newPlayer")) {
								in = input.readLine();
								onlinePlayers.clear();
								while (!in.equals("playersUpdated")) {
									onlinePlayers.add(in);
									in = input.readLine();
								}
								lobbyController.addOnline(onlinePlayers);
							}
							if (in.contains("otherPlayer")) {
								int otherPlayerID = Integer.parseInt(input.readLine());
								List<String> cards = extractCards(input.readLine());
								for (int j = 0; j < cards.size(); j++) {
									table.get(otherPlayerID).add(cards.get(j));
								}
								System.out.println("This is table >>>");
								for (int i = 0; i < table.size(); i++) {
									System.out.println(table.get(i));
								}
								System.out.println("<<< This is table");
							}
							if (in.contains("tableSent")) {
								int playerCount = 2;
								for (int i = 1; i < table.size(); i++) {
									if (i != ID) {
										gameController.removeFacedown(playerCount);
										for (int j = 0; j < table.get(i).size(); j++) {
											gameController.addCardToOpposingPlayerHand(playerCount,
													table.get(i).get(j));
										}
										playerCount++;
									}
								}
							}
							if (in.contains("playersFinished")) { // Server tells client what to display
								System.out.println("All players finished");
							}
							if (in.contains("showDealerHand")) {
								System.out.println("Dealers cards: " + table.get(0) + "total: " + total(table.get(0)));
								System.out.println("Dealer taking cards....");
							}
							if (in.contains("dealerCard"))
								table.get(0).add(in.substring(10));
							if (in.contains("dealerDone"))
								dealerTurn = false; // Dealers turn is finished, break from loop
							if (in.equals("Clear queue")) {
								lobbyController.clearQueue();
							}
						}
					}
					System.out.println(table.get(0));
					gameController.removeDealerFacedown();
					for (int i = 0; i < table.get(0).size(); i++) {
						gameController.addCardToDealerHand(table.get(0).get(i));
					}
					declareWinner();
				}
				table.clear();
				inQueue.clear();
				gameController.endChat();
			}
		} catch (IOException e) {
			System.out.println("Session not joinable");
			e.printStackTrace();
			output.close();
			waitForServer.release();
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
		System.out.println("Dealers cards: " + table.get(0) + " total: " + total(table.get(0)));
		if (total(table.get(ID)) > 21) {
			gameController.setLabel("Bust!! You lose!");
		} 
		else if (total(table.get(0)) > 21) {
			gameController.setLabel("Dealer bust! You Win!");
		} 
		else if (total(table.get(ID)) == total(table.get(0))) {
			gameController.setLabel("Draw!");
		} 
		else if (total(table.get(ID)) > total(table.get(0))) {
			gameController.setLabel("You win!!");
		} 
		else {
			gameController.setLabel("Dealer Wins!!");
		}
	}
}
