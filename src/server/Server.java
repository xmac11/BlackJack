/**
 * Author: Group21 - Final version
 * Class Server: Main server-side class
 */
package server;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import database.SQLDatabaseConnection;
import database.Session;
import shareable.*;

/**
 * Class Server: server activates by running this class and deactivates by
 * terminating it
 */
public class Server implements Runnable {

	Semaphore deckWait;
	Semaphore gameBegin;
	CyclicBarrier dealersTurn;
	CyclicBarrier betWait;
	private List<List<String>> table;
	Boolean join;
	private List<SocketConnection> joined;
	private List<SocketConnection> gameQueue;
	private GameStart gameStart;
	private ServerSocket serverSocket;
	private FinishedPlayers finishedPlayers;

	public Server() { // server constructor
		table = new ArrayList<>();
		table.add(new ArrayList<>()); // table holds all of the hands at a table, index 0 will always be the dealer's
										// hand
		joined = new ArrayList<>(); // stores connected users
		gameQueue = new ArrayList<>(); // stores users that are in queue for a game
	}

	public static void main(String[] args) {
		Server server = new Server();
		Thread gameSession = new Thread(server);
		gameSession.start(); // Sends off a thread that represents a game session

	}

	/* Boolean Method to use it in the testing class ServerOnTesting */
	public static boolean serverListening(String host, int port) {
		Socket s = null;
		try {
			s = new Socket(host, port);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (s != null)
				try {
					s.close();
				} catch (Exception e) {
				}
		}
	}

	@Override
	public void run() { // database connection
		SQLDatabaseConnection sqlDatabaseConnection = new SQLDatabaseConnection();
		Thread thread = new Thread(sqlDatabaseConnection);
		thread.start();
		serverSocket = null;
		gameStart = new GameStart();
		gameStart.setGameStart(false);

		// PlayerJoin thread is created with a selection of variables that maintain
		// synchronisation
		PlayerJoin playerJoin = new PlayerJoin(joined, gameQueue, serverSocket, gameStart);
		new Thread(playerJoin).start();

		// Sends off a thread which waits on the socket to accept clients
		while (true) {
			// main thread check if game is on and if the players are 3
			while (!gameStart.isGameStart() && !(gameQueue.size() == 3)) {
				try {
					Thread.sleep(500); // Sleep the thread in between checks so that it is not constantly polling the
										// while condition
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			gameStart.setGameStart(true); // game starts
			int sessionID = Session.getMaxSessionID() + 1;
			for (int i = 0; i < joined.size(); i++) {
				synchronized (joined.get(i).getOutput()) {
					joined.get(i).getOutput().println("Game in progress");
					joined.get(i).getOutput().println(gameQueue.size());
				}
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
			/*
			 * Creates a semaphore to allow 1 thread to access a critical section, this is
			 * used to control access to the deck
			 */
			deckWait = new Semaphore(1);
			/*
			 * Creates a CyclicBarrier to the number of players in game + the dealer to
			 * simulate a dealer waiting for all players to finish
			 */
			dealersTurn = new CyclicBarrier((gameQueue.size() + 1));
			betWait = new CyclicBarrier(gameQueue.size()); // Creates a CyclicBarrier to wait for the bets of all the
															// players
			finishedPlayers = new FinishedPlayers(); // shareable class
			if (gameQueue.size() > 0) { // Ensures there are players in the session
				System.out.println("Game Starting...");
				for (int i = 0; i < gameQueue.size(); i++) {
					ServerPlayerHandler serverThread = null;
					table.add(new ArrayList<>()); // creates structured ArrayLists representing players' hands
					serverThread = new ServerPlayerHandler(gameQueue.get(i), i + 1, deck, deckWait, gameQueue.size(),
							dealersTurn, table, finishedPlayers, gameQueue, sessionID);
					System.out.println("Player " + (i + 1) + " added"); // For debugging
					new Thread(serverThread).start(); // Sends thread
				}
				while (gameQueue.size() > finishedPlayers.getPlayersBet()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				for (int i = 0; i < gameQueue.size(); i++) {
					synchronized (gameQueue.get(i).getOutput()) {
						gameQueue.get(i).getOutput().println("breakFromBetLoop");
					}
				}
				while (gameQueue.size() > finishedPlayers.getFinishedPlayers()) {
					try {
						Thread.sleep(500);
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
														// table variable passed to the threads all player handler
														// threads will see the changes.
				}
				try {
					dealersTurn.await(); // Dealers turn is finished, all player threads waiting on this barrier in
											// player handler threads can now continue
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("No players joined, session ending");
			}
			for (int i = 0; i < joined.size(); i++) {
				System.out.println("sending clear queue to joined");
				synchronized (joined.get(i).getOutput()) {
					joined.get(i).getOutput().println("Clear queue");
				}
			}
			gameStart.setGameStart(false);
			gameQueue.clear();
			System.out.println("Game over");
		}

	}

}
