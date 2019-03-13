package server;

import java.net.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import database.SQLDatabaseConnection;
import database.Session;
import shareable.*;

public class Server implements Runnable {

	Semaphore deckWait;
	Semaphore gameBegin;
	CyclicBarrier dealersTurn;
	private List<List<String>> table;
	Boolean join;
	private List<SocketConnection> joined;
	private List<SocketConnection> gameQueue;
	private GameStart gameStart;
	private ServerSocket serverSocket;
	private FinishedPlayers finishedPlayers;

	public Server() {
		table = new ArrayList<>();
		table.add(new ArrayList<>());
		joined = new ArrayList<>();
		gameQueue = new ArrayList<>();
	}

	public static void main(String[] args) {
		Server server = new Server();
		Thread gameSession = new Thread(server);
		gameSession.start(); // Sends off a thread that represents a game session
	}

	@Override
	public void run() {
		SQLDatabaseConnection sqlDatabaseConnection = new SQLDatabaseConnection();
		Thread thread = new Thread(sqlDatabaseConnection);
		thread.start();
		serverSocket = null;
		gameStart = new GameStart();
		gameStart.setGameStart(false);
		PlayerJoin playerJoin = new PlayerJoin(joined, gameQueue, serverSocket, gameStart);
		new Thread(playerJoin).start();
		// Sends off a thread which waits on the socket to accept clients. The main
		while (true) {
			while (!gameStart.isGameStart() && !(gameQueue.size() == 3)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			gameStart.setGameStart(true);
			int sessionID = Session.getMaxSessionID() + 1;
			for (int i = 0; i < joined.size(); i++) {
				joined.get(i).getOutput().println("Game in progress");
				joined.get(i).getOutput().println(gameQueue.size());
			}
			for (int i = 0; i < gameQueue.size(); i++) {
				gameQueue.get(i).setInLobby(false);
				gameQueue.get(i).getOutput().println("Game Starting");
			}
			Deck deck = new Deck(); // Creates a deck
			table.clear();
			table.add(new ArrayList<>());
			table.get(0).add(deck.drawCard());
			table.get(0).add(deck.drawCard());
			System.out.println("This is Dealers cards: " + table.get(0)); // Prints the dealers hand to the server
																			// console
 			deckWait = new Semaphore(1); // Creates a semaphore to allow 1 thread to access a critical section, this
											// is used to control access to the deck
			dealersTurn = new CyclicBarrier((gameQueue.size() + 1));
			finishedPlayers = new FinishedPlayers();
			if (gameQueue.size() > 0) { // Ensures there are players in the session
				System.out.println("Game Starting...");
				for (int i = 0; i < gameQueue.size(); i++) {
					ServerPlayerHandler serverThread = null;
					table.add(new ArrayList<>());
					serverThread = new ServerPlayerHandler(gameQueue.get(i), i + 1, deck, deckWait, gameQueue.size(),
							dealersTurn, table, finishedPlayers, gameQueue, sessionID);
					System.out.println("Player " + (i + 1) + " added"); // For debugging
					new Thread(serverThread).start(); // Sends thread
				}
				while (gameQueue.size() > finishedPlayers.getFinishedPlayers()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				
				for (int i = 0; i < gameQueue.size(); i++) {
					gameQueue.get(i).getOutput().println("breakFromLoop");
				}

				System.out.println("All players finished, dealer picking cards"); // Once all players in
																					// ServerThread have reached the
																					// barrier the main thread
																					// continues...
				while (Deck.total(table.get(0)) < 17) {
					table.get(0).add(deck.drawCard()); // Logic to make the dealer pick their cards, since the
														// dealersHand variable passed to the threads is a reference
														// to this variable, all threads will see the changes.
				}
				try {
					dealersTurn.await(); // Dealers turn is finished, all player threads waiting on this barrier in
											// server threads can now continue
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("No players joined, session ending");
			}
			for (int i = 0; i < joined.size(); i++) {
				System.out.println("sending clear queue to joined");
				joined.get(i).getOutput().println("Clear queue");
			}
			gameStart.setGameStart(false);
			gameQueue.clear();
			System.out.println("Game over");
		}
		
	}

}
