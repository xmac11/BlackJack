package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class Server implements Runnable {


	Semaphore deckWait;
	Semaphore initialCardWait;
	Semaphore waitForPlayerOne;
	CyclicBarrier playersWait;
	CyclicBarrier playersTurnWait;
	CyclicBarrier dealersTurn;
	private int secondsToWait;
	private List<List<String>> table;
	Boolean join;
	private List<String> chatLog;
	private Semaphore serverChatWait;

	public Server() {
		table = new ArrayList<>();
		table.add(new ArrayList<>());
		waitForPlayerOne = new Semaphore(0);
		secondsToWait = 5;
	}

	public static void main(String[] args) throws InterruptedException {
		while (true) {
			Server server = new Server();
			Thread gameSession = new Thread(server);
			gameSession.start(); //Sends off a thread that represents a game session
			gameSession.join(); //Waits for session to end before starting another
		}
	}

	@Override
	public void run() {
		List<Socket> joined = new ArrayList<>();
		ServerSocket serverSocket = null;
		join = true;
		PlayerJoin playerJoin = new PlayerJoin(joined, waitForPlayerOne, serverSocket, join);
		new Thread(playerJoin).start(); //Sends off a thread which waits on the socket to accept clients. The main thread will continue 
		System.out.println("Waiting for first player to connect...");
		try {
			waitForPlayerOne.acquire(); //Main thread waits here until the semaphore is signalled from the playerJoin thread
		} catch (InterruptedException e2) { //The first client that connects will signal the semaphore allowing the main thread to continue
			e2.printStackTrace();
		}

		while (secondsToWait > 0) {
			System.out.println("Game begins in " + secondsToWait + " seconds."); //The main thread sits in this loop for 'secondsToWait' seconds
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			secondsToWait--;
		}
		join = false;
		playerJoin.closeConnection(); //Closes the server socket used in the playerJoin thread
		Deck deck = new Deck(); //Creates a deck 
		table.get(0).add(deck.drawCard());
		table.get(0).add(deck.drawCard());
		System.out.println("Dealers cards: " + table.get(0)); //Prints the dealers hand to the server console (for debugging)
		playersWait = new CyclicBarrier(joined.size() + 1); //Sets the barrier to be used to wait for all players + main server thread, makes threads wait until it is the dealers turn.
		dealersTurn = new CyclicBarrier(joined.size() + 1); //Same as above barrier, this barrier is potentially redundant, I just haven't got round to removing it yet
		playersTurnWait = new CyclicBarrier(joined.size()); //Sets the barrier to wait for all players to finish their turn 
		deckWait = new Semaphore(1); //Creates a semaphore to allow 1 thread to access a critical section, this is used to control access to the deck
		initialCardWait = new Semaphore(1);
		chatLog = new ArrayList<>();
		serverChatWait = new Semaphore(0);
		if (joined.size() > 0) { //Ensures there are players in the session
			System.out.println("Game Starting...");
			for (int i = 0; i < joined.size(); i++) {
				ServerPlayerHandler serverThread = null;
				table.add(new ArrayList<>());
				serverThread = new ServerPlayerHandler(joined.get(i), i + 1, deck, deckWait, playersWait, playersTurnWait,
						joined.size(), dealersTurn, initialCardWait, table, chatLog, serverChatWait); //Creates a thread for a client, sends all relevant variables
				System.out.println("Player " + i + " added"); //For debugging
				new Thread(serverThread).start(); //Sends thread
			}

			try {
				playersWait.await(); //Main thread waits here until all clients have completed their turn
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
			System.out.println("All players finished, dealer picking cards"); //Once all players in ServerThread have reached the barrier the main thread continues...
			while (deck.total(table.get(0)) < 17) {
				table.get(0).add(deck.drawCard()); //Logic to make the dealer pick their cards, since the dealersHand variable passed to the threads is a reference to this variable, all threads will see the changes.
			}
			try {
				dealersTurn.await(); //Dealers turn is finished, all player threads waiting on this barrier in server threads can now continue
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("No players joined, session ending");
		}
		System.out.println("Game over");
	}

}
