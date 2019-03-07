package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import shareable.FinishedPlayers;

public class ServerPlayerHandler implements Runnable {

	private SocketConnection socketConnection = null;
	private int ID;
	private List<List<String>> table;
	private Deck deck;
	private Semaphore deckWait;
	private Semaphore serverChatWait;
	CyclicBarrier playersWait;
	CyclicBarrier playersTurnWait;
	CyclicBarrier dealersTurn;
	CyclicBarrier gameOver;
	private int noPlayers;
	private boolean active;
	private int barriers;
	private List<String> chatLog;
	private FinishedPlayers finishedPlayers;

	public ServerPlayerHandler(SocketConnection socketConnection, int ID, Deck deck, Semaphore deckWait,
			CyclicBarrier playersWait, CyclicBarrier playersTurnWait, int noPlayers, CyclicBarrier dealersTurn,
			List<List<String>> table, List<String> chatLog, Semaphore serverChatWait, FinishedPlayers finishedPlayers) {
		this.socketConnection = socketConnection;
		this.ID = ID;
		this.deck = deck;
		this.deckWait = deckWait;
		this.playersWait = playersWait;
		this.playersTurnWait = playersTurnWait;
		this.noPlayers = noPlayers;
		active = true;
		this.dealersTurn = dealersTurn;
		this.table = table;
		barriers = 0;
		this.chatLog = chatLog;
		this.serverChatWait = serverChatWait;
		this.finishedPlayers = finishedPlayers;
	}

	@Override
	public void run() {
		String hello = "Welcome player " + ID + " there is " + noPlayers
				+ " player(s) in the current session, have fun";
		socketConnection.getOutput().println(hello); // Sends greeting to client
		System.out.println(hello);
		System.out.println("Number of players in game " + noPlayers); // Prints the number of players in the game to the
																		// server thread

		/*
		 * Each connected client will have a thread running in this class, therefore any
		 * variable access must be synchronised
		 */

		synchronized (deck) {
			socketConnection.getOutput().println(table.get(0).get(0));
			socketConnection.getOutput().println(table.get(0).get(1)); // Sends the dealers hand to the client
			String card1 = deck.drawCard();
			String card2 = deck.drawCard();
			socketConnection.getOutput().println(card1);
			socketConnection.getOutput().println(card2); // Draws the clients hand
			table.get(ID).add(card1);
			table.get(ID).add(card2);
		}

		Runnable r = new ServerMoveThread(socketConnection.getOutput(), deckWait);
		Thread thread = new Thread(r);
		thread.start();

		Runnable chatRunnable = new ServerChatThread(socketConnection.getOutput(), serverChatWait, chatLog);
		Thread chatThread = new Thread(chatRunnable);
		chatThread.start();
		String in = "";

		while (active) { // While the player is still active (until they break or pass)
			try {
				in = socketConnection.getInput().readLine(); // Reads the message from the client
			} catch (IOException e) {
				System.out.println("Player disconnected");
				triggerBarrier();
				chatThread.interrupt();
				socketConnection.setInLobby(true);
				socketConnection.getSessionWait().release();
				return;
			}
			System.out.println("This is in: " + in);
			if (in.contains("gameChatMessage")) {
				System.out.println("Sending chat message");
				chatLog.add(in);
				for (int i = 0; i < noPlayers; i++) {
					serverChatWait.release();
				}
			}
			if(in.equals("playerLeft")) {
				socketConnection.getOutput().println("playerLeft");
				triggerBarrier();
				return;
			}
			if (in.equals("h")) {
				String card = deck.drawCard();
				table.get(ID).add(card);
				socketConnection.getOutput().println("playerCard" + card); // If the client asked for a card then
																			// send a new card from the deck
			}
			if (in.equals("p")) { // If the client passed then set active to false to break from loop
				active = false;
			}
			if (in.equals("move")) { // Client requests the Make move message
				socketConnection.getOutput().println("Make move");
			}
		}
		socketConnection.getOutput().println("Player " + ID + " finished");
		System.out.println("Player " + ID + " finished");
		// Releases the deck to allow another thread

		finishedPlayers.playerFinished();
		deckWait.release();
		while (finishedPlayers.getFinishedPlayers() < noPlayers) {
			try {
				System.out.println("Finished players " + finishedPlayers.getFinishedPlayers());
				in = socketConnection.getInput().readLine();
			} catch (IOException e) {
				System.out.println("Connection closed...");
				break;
			}
			if(in.equals("playerLeft")) {
				socketConnection.getOutput().println("playerLeft");
				triggerBarrier();
				return;
			}
			if (in.contains("chatMessage")) {
				System.out.println("Sending chat message");
				chatLog.add(in);
				for (int i = 0; i < noPlayers; i++) {
					serverChatWait.release();
				}
			}
		}

		System.out.println("players finished");
		socketConnection.getOutput().println("playersFinished"); // Once all threads have reached playersTurnWait they
																	// will all be allowed to
		if (noPlayers > 1) {
			for (int i = 1; i < table.size(); i++) {
				if (i != ID) {
					socketConnection.getOutput().println("otherPlayer");
					socketConnection.getOutput().println(i);
					socketConnection.getOutput().println(table.get(i));
				}
			}
			socketConnection.getOutput().println("tableSent");
			System.out.println("Table Sent");
		}

		try {
			playersWait.await(); // Threads now reach the barrier that the main thread is waiting on, as all
									// threads are now here (players+main) they can continue
		} catch (InterruptedException | BrokenBarrierException e) {
			triggerBarrier();
			e.printStackTrace();
		}
		System.out.println("Dealers turn");
		barriers++;
		try {
			dealersTurn.await(); // Player threads get stopped here, main server thread continues in server
									// class.
		} catch (InterruptedException | BrokenBarrierException e) {
			triggerBarrier();
			e.printStackTrace();
		}
		barriers++;
		socketConnection.getOutput().println("showDealerHand"); // Tells the client to display the dealers hand to the
																// players, the clients
		// only have the dealers first 2 cards at this point
		System.out.println("Dealers hand: " + table.get(0));
		for (int i = 2; i < table.get(0).size(); i++) {
			socketConnection.getOutput().println("dealerCard" + table.get(0).get(i)); // Sends the clients the dealers
																						// new cards
		}
		socketConnection.getOutput().println("dealerDone"); // Tells the client the dealer is finished
		System.out.println("Dealer done");
		chatThread.interrupt();
		socketConnection.setInLobby(true);
		socketConnection.getSessionWait().release();
		System.out.println("player released");
	}

	public void triggerBarrier() {
		try {
			System.out.println("entered trigger");
			switch (barriers) {
			case 0:
				deckWait.release();
				finishedPlayers.playerFinished();
				playersWait.await();
			case 1:
				dealersTurn.await();
			}
		} catch (Exception e) {

		}
	}

}
