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

public class ServerPlayerHandler implements Runnable {

	private Socket socket = null;
	private PrintWriter output;
	private BufferedReader input;
	private int ID;
	private List<List<String>> table;
	private Deck deck;
	private Semaphore deckWait;
	private Semaphore initialCardWait;
	private Semaphore serverChatWait;
	CyclicBarrier playersWait;
	CyclicBarrier playersTurnWait;
	CyclicBarrier dealersTurn;
	CyclicBarrier gameOver;
	private int noPlayers;
	private boolean active;
	private int barriers;
	private List<String> chatLog;

	public ServerPlayerHandler(Socket socket, int ID, Deck deck, Semaphore deckWait, CyclicBarrier playersWait,
			CyclicBarrier playersTurnWait, int noPlayers, CyclicBarrier dealersTurn, Semaphore initialCardWait,
			List<List<String>> table, List<String> chatLog, Semaphore serverChatWait) {
		this.socket = socket;
		this.ID = ID;
		this.deck = deck;
		this.deckWait = deckWait;
		this.playersWait = playersWait;
		this.playersTurnWait = playersTurnWait;
		this.noPlayers = noPlayers;
		active = true;
		this.dealersTurn = dealersTurn;
		this.initialCardWait = initialCardWait;
		this.table = table;
		barriers = 0;
		this.chatLog = chatLog;
		this.serverChatWait = serverChatWait;
	}

	@Override
	public void run() {
		try {
			output = new PrintWriter(socket.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException exception) {
			triggerBarrier();
			System.out.println("Error");
			return;
		}
		String hello = "Welcome player " + ID + " there is " + noPlayers
				+ " player(s) in the current session, have fun";
		output.println(hello); // Sends greeting to client
		System.out.println(hello);
		System.out.println("Number of players in game " + noPlayers); // Prints the number of players in the game to the
																		// server thread

		/*
		 * Each connected client will have a thread running in this class, therefore any
		 * variable access must be synchronised
		 */

		try {
			initialCardWait.acquire();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		output.println(table.get(0).get(0));
		output.println(table.get(0).get(1)); // Sends the dealers hand to the client
		String card1 = deck.drawCard();
		String card2 = deck.drawCard();
		output.println(card1);
		output.println(card2); // Draws the clients hand
		table.get(ID).add(card1);
		table.get(ID).add(card2);
		initialCardWait.release();

		Runnable r = new ServerMoveThread(output, deckWait);
		Thread thread = new Thread(r);
		thread.start();

		Runnable chatRunnable = new ServerChatThread(output, serverChatWait, chatLog);
		Thread chatThread = new Thread(chatRunnable);
		chatThread.start();

//		try {
//			deckWait.acquire(); 	// Only allows 1 thread through at a time
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		output.println("Make move");	// Asks the client to make move, this message is picked up in the client class
		// (within the while loop), the client class then asks the client for a move.
		String in = "";

		while (active) { // While the player is still active (until they break or pass)
			try {
				in = input.readLine(); // Reads the message from the client
			} catch (IOException e) {
				System.out.println("Player disconnected");
				triggerBarrier();
				return;
			}
			System.out.println("This is in: " + in);
			if (in.contains("chatMessage")) {
				System.out.println("Sending chat message");
				chatLog.add(in);
				for (int i = 0; i < noPlayers; i++) {
					serverChatWait.release();
				}
			}
			if (in.equals("h")) {
				String card = deck.drawCard();
				table.get(ID).add(card);
				output.println("playerCard" + card); // If the client asked for a card then send a new card from the
														// deck
			}
			if (in.equals("p")) { // If the client passed then set active to false to break from loop
				active = false;
			}
			if (in.equals("move")) { // Client requests the Make move message
				output.println("Make move");
			}
		}
//		try {
//			Thread.sleep(1000); // Sleeps the thread, just so that the text printed to console is not too fast
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
		output.println("Player " + ID + " finished");
		System.out.println("Player " + ID + " finished");
		deckWait.release(); // Releases the deck to allow another thread
		
		try {
			playersTurnWait.await(); // Wait her until all players have finished their move. This is NOT the same
										// barrier that the main thread is currently waiting on. Main thread is waiting
										// on playersWait
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		output.println("playersFinished"); // Once all threads have reached playersTurnWait they will all be allowed to
											// continue
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
		if (noPlayers > 1) {
			for (int i = 1; i < table.size(); i++) {
				if (i != ID) {
					output.println("otherPlayer");
					output.println(i);
					output.println(table.get(i));
				}
			}
			output.println("tableSent");
		}
		try {
			playersWait.await(); // Threads now reach the barrier that the main thread is waiting on, as all
									// threads are now here (players+main) they can continue
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		barriers++;
		try {
			dealersTurn.await(); // Player threads get stopped here, main server thread continues in server
									// class.
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		barriers++;
		output.println("showDealerHand"); // Tells the client to display the dealers hand to the players, the clients
											// only have the dealers first 2 cards at this point
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		System.out.println("Dealers hand: " + table.get(0));
		for (int i = 2; i < table.get(0).size(); i++) {
			output.println("dealerCard" + table.get(0).get(i)); // Sends the clients the dealers new cards
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
		output.println("dealerDone"); // Tells the client the dealer is finished
		while (true) {
			try {
				in = input.readLine();
			} catch (IOException e) {
				System.out.println("Connection closed...");
				break;
			}
			if (in.contains("chatMessage")) {
				System.out.println("Sending chat message");
				chatLog.add(in);
				for (int i = 0; i < noPlayers; i++) {
					serverChatWait.release();
				}
			}
			if (socket.isClosed()) {
				System.out.println("Closed socket");
				break;
			}
		}
	}

	public void triggerBarrier() {
		try {
			System.out.println("entered trigger");
			switch (barriers) {
			case 0:
				deckWait.release();
				playersTurnWait.await();
				playersWait.await();
			case 1:
				dealersTurn.await();
			}
		} catch (Exception e) {

		}
	}

}
