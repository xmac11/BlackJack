package server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import database.Session;
import shareable.FinishedPlayers;

/**
 * Class to handle in game requests
 * 
 * @author Group 21
 *
 */
public class ServerPlayerHandler implements Runnable {

	private SocketConnection socketConnection = null;
	private int ID;
	private List<List<String>> table;
	private Deck deck;
	private Semaphore deckWait;
	CyclicBarrier dealersTurn;
	private int noPlayers;
	private boolean active;
	private boolean myTurn;
	private int barriers;
	private boolean betPlaced;
	private FinishedPlayers finishedPlayers;
	private List<SocketConnection> gameQueue;
	private int sessionID;

	/**
	 * Constructor for class
	 * @param socketConnection the SocketConnection of the user
	 * @param ID the users ID for the game
	 * @param deck the deck to be used
	 * @param deckWait the semaphore to pass to ServerMoveThread
	 * @param noPlayers	the number of players in the game
	 * @param dealersTurn barrier to inform when dealer has finished turn
	 * @param table the structure to hold all of the hands on the table (index 0 is dealer)
	 * @param finishedPlayers the shareable object 
	 * @param gameQueue the list of players in the game
	 * @param sessionID the ID of the game
	 */
	public ServerPlayerHandler(SocketConnection socketConnection, int ID, Deck deck, Semaphore deckWait, int noPlayers,
			CyclicBarrier dealersTurn, List<List<String>> table, FinishedPlayers finishedPlayers,
			List<SocketConnection> gameQueue, int sessionID) {
		this.socketConnection = socketConnection;
		this.ID = ID;
		this.deck = deck;
		this.deckWait = deckWait;
		this.noPlayers = noPlayers;
		active = true;
		this.dealersTurn = dealersTurn;
		this.table = table;
		barriers = 0;
		this.finishedPlayers = finishedPlayers;
		this.gameQueue = gameQueue;
		this.sessionID = sessionID;
	}

	/**
	 * Method to handle game requests
	 */
	@Override
	public void run() {
		Session.startSession(socketConnection.getUsername(), sessionID); // Sends the session ID to the client
		String hello = "Welcome player " + ID + " there is " + noPlayers
				+ " player(s) in the current session, have fun";
		socketConnection.getOutput().println(hello); // Sends greeting to client
		socketConnection.getOutput().println("sessionID" + sessionID);
		String in = ""; // server thread
		String card1 = "";
		String card2 = "";
		
		synchronized (deck) {
			card1 = deck.drawCard();
			card2 = deck.drawCard(); //Draws the players cards
		}
		myTurn = false;
		table.get(ID).add(card1);
		table.get(ID).add(card2); //Adds the cards to the table
		/*
		 * Each connected client will have a thread running in this class, therefore any
		 * variable access must be synchronised
		 */
		try {
			betPlaced = false;
			/*
			 * Waits in this loop until the client has placed bet
			 */
			while (!in.startsWith("betIs")) { 
				in = socketConnection.getInput().readLine();
				if (in.startsWith("gameChatMessage")) {
					String toSend = socketConnection.getInput().readLine().substring(15) + " > "
							+ socketConnection.getInput().readLine().substring(15);
					for (int i = 0; i < gameQueue.size(); i++) {
						gameQueue.get(i).getOutput().println("gameChatMessage" + toSend); //Forwards chat messages to other players
					}
				}
				if (in.equals("playerLeftGame")) {
					socketConnection.getOutput().println("playerLeftGame");
					socketConnection.setInLobby(true);
					socketConnection.getSessionWait().release();
					triggerBarrier();
					return;
				}
				if (in.equals("thisPlayerLeft")) {
					socketConnection.getOutput().println("playerLeftGame");
					socketConnection.setInLobby(true);
					socketConnection.getSessionWait().release();
					triggerBarrier();
					return;
				}
			}
			betPlaced = true;
			socketConnection.getOutput().println(in);
			finishedPlayers.playerBet();
			/*
			 * Waits in this loop until all clients have placed bet
			 */
			while (gameQueue.size() > finishedPlayers.getPlayersBet()) {
				in = socketConnection.getInput().readLine();
				if (in.equals("breakFromBetLoop")) {
					break;
				}
				if (in.startsWith("gameChatMessage")) {
					String toSend = socketConnection.getInput().readLine().replaceFirst("gameChatMessage", "") + " > "
							+ socketConnection.getInput().readLine().replaceFirst("gameChatMessage", "");
					for (int i = 0; i < gameQueue.size(); i++) {
						gameQueue.get(i).getOutput().println("gameChatMessage" + toSend);
					}
				}
				if (in.equals("playerLeftGame")) {
					socketConnection.getOutput().println("playerLeftGame");
					socketConnection.setInLobby(true);
					socketConnection.getSessionWait().release();
					triggerBarrier();
					return;
				}
				if (in.equals("thisPlayerLeft")) {
					socketConnection.getOutput().println("playerLeftGame");
					socketConnection.setInLobby(true);
					socketConnection.getSessionWait().release();
					triggerBarrier();
					return;
				}
			}
		} catch (IOException e) {
			socketConnection.getOutput().println("playerLeftGame");
			socketConnection.setInLobby(true);
			socketConnection.getSessionWait().release();
			triggerBarrier();
			return;
		}
		barriers++;
		synchronized (socketConnection.getOutput()) {
			socketConnection.getOutput().println(
					"startCards\n" + table.get(0).get(0) + "\n" + table.get(0).get(1) + "\n" + card1 + "\n" + card2);
		}

		Runnable r = new ServerMoveThread(socketConnection, deckWait);
		Thread thread = new Thread(r);
		thread.start();

		/*
		 * Waits in this loop until the players has finished their turn
		 */
		while (active) { // While the player is still active (until they break or pass)
			try {
				in = socketConnection.getInput().readLine(); // Reads the message from the client
				if (in.startsWith("gameChatMessage")) {
					String toSend = socketConnection.getInput().readLine().substring(15) + " > "
							+ socketConnection.getInput().readLine().substring(15);
					for (int i = 0; i < gameQueue.size(); i++) {
						gameQueue.get(i).getOutput().println("gameChatMessage" + toSend);
					}
				}
				if (in.equals("playerLeftGame")) {
					socketConnection.getOutput().println("playerLeftGame");
					socketConnection.setInLobby(true);
					socketConnection.getSessionWait().release();
					triggerBarrier();
					return;
				}
				if (in.equals("thisPlayerLeft")) {
					socketConnection.getOutput().println("playerLeftGame");
					socketConnection.setInLobby(true);
					socketConnection.getSessionWait().release();
					triggerBarrier();
					return;
				}
				if (in.equals("h")) {
					String card = deck.drawCard();
					table.get(ID).add(card);
					for (int i = 0; i < gameQueue.size(); i++) {
						gameQueue.get(i).getOutput().println("playerCard" + ID);
						gameQueue.get(i).getOutput().println("playerCard" + card);
					}
				}
				if (in.equals("myTurn")) { // If the client passed then set active to false to break from loop
					myTurn = true;
				}
				if (in.equals("p")) { // If the client passed then set active to false to break from loop
					active = false;
				}
				if (in.equals("busted")) {
					finishedPlayers.increaseBustedPlayers();
					active = false;
				}
				if (in.equals("move")) { // Client requests the Make move message
					socketConnection.getOutput().println("Make move");
				}
			} catch (IOException e) {
				System.out.println("Player disconnected");
				socketConnection.setInLobby(true);
				socketConnection.getSessionWait().release();
				triggerBarrier();
				return;
			}
		}
		myTurn = false;
		active = false;
		socketConnection.getOutput().println("iHaveFinished");
		System.out.println("Player " + ID + " finished");
		finishedPlayers.playerFinished();
		deckWait.release();
		/*
		 * Waits in this loop until all players have finished their turn
		 */
		while (finishedPlayers.getFinishedPlayers() < gameQueue.size()) {
			try {
				in = socketConnection.getInput().readLine();
				if (in.equals("playerLeftGame")) {
					socketConnection.getOutput().println("playerLeftGame");
					socketConnection.setInLobby(true);
					socketConnection.getSessionWait().release();
					triggerBarrier();
					return;
				}
				if (in.equals("thisPlayerLeft")) {
					socketConnection.getOutput().println("playerLeftGame");
					socketConnection.setInLobby(true);
					socketConnection.getSessionWait().release();
					triggerBarrier();
					return;
				}
				if (in.equals("breakFromLoop")) { //readLine is blocking meaning the thread wont break even when the while condition is not met, until it reads something
					break;
				}
				if (in.startsWith("gameChatMessage")) {
					String toSend = socketConnection.getInput().readLine().substring(15) + " > "
							+ socketConnection.getInput().readLine().substring(15);
					for (int i = 0; i < gameQueue.size(); i++) {
						gameQueue.get(i).getOutput().println("gameChatMessage" + toSend);
					}
				}
			} catch (IOException e) {
				System.out.println("Player disconnected");
				socketConnection.setInLobby(true);
				socketConnection.getSessionWait().release();
				triggerBarrier();
				return;
			}
		}

		for (int j = 1; j < table.size(); j++) {
			if (ID != j) {
				socketConnection.getOutput().println("playerInitialCard" + j + "\nplayerInitialCard"
						+ table.get(j).get(0) + "\nplayerInitialCard" + table.get(j).get(1)); //First two cards of other players sent
			}
		}
		socketConnection.getOutput().println("playersFinished"); // Once all threads have reached playersTurnWait they

		if (finishedPlayers.getBustedPlayers() < gameQueue.size()) { // will all be allowed to
			socketConnection.getOutput().println("dealerPlays");
		} else {
			socketConnection.getOutput().println("skipDealer");
		}

		socketConnection.getOutput().println("showPlayerCards");
		barriers++;
		try {
			dealersTurn.await(); // Player threads get stopped here, main server thread continues in server
									// class.
		} catch (InterruptedException | BrokenBarrierException e) {
			triggerBarrier();
			e.printStackTrace();
		}

		barriers++;
		if (finishedPlayers.getBustedPlayers() != noPlayers) {
			for (int i = 2; i < table.get(0).size(); i++) {
				socketConnection.getOutput().println("dealerCard" + table.get(0).get(i)); // Sends the clients the
																							// dealers cards, after move
			}
		}
		socketConnection.getOutput().println("dealerDone"); // Tells the client the dealer is finished
		socketConnection.setInLobby(true);
		Session.setSessionEnd(socketConnection.getUsername(), sessionID);
		socketConnection.getSessionWait().release(); //Allows ServerLobbyThread back into lobby
	}

	/**
	 * Method to release any sync tools that need to be unlocked
	 */
	public void triggerBarrier() {
		try {
			gameQueue.remove(socketConnection);
			Session.setSessionEnd(socketConnection.getUsername(), sessionID);
			switch (barriers) {
			case 0:

			case 1:
				if (betPlaced)
					finishedPlayers.playerBetLeft();
				if (myTurn)
					deckWait.release();
			case 2:
				dealersTurn.await();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error when releasing barriers");
		}
	}

}
