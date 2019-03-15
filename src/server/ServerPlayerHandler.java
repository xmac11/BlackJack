package server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import database.Session;
import shareable.FinishedPlayers;

public class ServerPlayerHandler implements Runnable {

	private SocketConnection socketConnection = null;
	private int ID;
	private List<List<String>> table;
	private Deck deck;
	private Semaphore deckWait;
	CyclicBarrier dealersTurn;
	private int noPlayers;
	private boolean active;
	private int barriers;
	private FinishedPlayers finishedPlayers;
	private List<SocketConnection> gameQueue;
	private int sessionID;

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

	@Override
	public void run() {
		Session.startSession(socketConnection.getUsername(), sessionID);
		String hello = "Welcome player " + ID + " there is " + noPlayers
				+ " player(s) in the current session, have fun";
		socketConnection.getOutput().println(hello); // Sends greeting to client
		socketConnection.getOutput().println("sessionID" + sessionID);
		System.out.println(hello);
		System.out.println("Number of players in game " + noPlayers); // Prints the number of players in the game to the
		String in = ""; // server thread

		/*
		 * Each connected client will have a thread running in this class, therefore any
		 * variable access must be synchronised
		 */
		try {
			while (!in.contains("betIs")) {
				in = socketConnection.getInput().readLine();
				if (in.contains("gameChatMessage")) {
					String toSend = socketConnection.getInput().readLine().substring(15) + " > "
							+ socketConnection.getInput().readLine().substring(15);
					System.out.println("Sending chat message");
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
			finishedPlayers.playerBet();
			while (gameQueue.size() > finishedPlayers.getPlayersBet()) {
				in = socketConnection.getInput().readLine();
				if (in.equals("breakFromLoop")) {
					break;
				}
				if (in.contains("gameChatMessage")) {
					String toSend = socketConnection.getInput().readLine().substring(15) + " > "
							+ socketConnection.getInput().readLine().substring(15);
					System.out.println("Sending chat message");
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
		System.out.println("Server passed bet");
		socketConnection.getOutput().println(in);
		barriers++;
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

		while (active) { // While the player is still active (until they break or pass)
			try {
				in = socketConnection.getInput().readLine(); // Reads the message from the client
				System.out.println("This is in: " + in);
				if (in.contains("gameChatMessage")) {
					String toSend = socketConnection.getInput().readLine().substring(15) + " > "
							+ socketConnection.getInput().readLine().substring(15);
					System.out.println("Sending chat message");
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
		active = false;
		socketConnection.getOutput().println("Player " + ID + " finished");
		System.out.println("Player " + ID + " finished");
		finishedPlayers.playerFinished();
		deckWait.release();
		while (finishedPlayers.getFinishedPlayers() < gameQueue.size()) {
			try {
				System.out.println(
						"Finished players " + finishedPlayers.getFinishedPlayers() + " out of " + gameQueue.size());
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
				if (in.equals("breakFromLoop")) {
					break;
				}
				if (in.contains("gameChatMessage")) {
					String toSend = socketConnection.getInput().readLine().substring(15) + " > "
							+ socketConnection.getInput().readLine().substring(15);
					System.out.println("Sending chat message");
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
				socketConnection.getOutput().println("playerInitialCard" + j);
				socketConnection.getOutput().println("playerInitialCard" + table.get(j).get(0));
				socketConnection.getOutput().println("playerInitialCard" + table.get(j).get(1));
			}
		}
		socketConnection.getOutput().println("initialCardsSent");
		System.out.println("players finished");
		socketConnection.getOutput().println("playersFinished"); // Once all threads have reached playersTurnWait they

		if (finishedPlayers.getBustedPlayers() == noPlayers) { // will all be allowed to
			socketConnection.getOutput().println("skipDealer");
		} else {
			socketConnection.getOutput().println("dealerPlays");
		}

		socketConnection.getOutput().println("showPlayerCards");
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
		System.out.println("Dealers hand: " + table.get(0));
		if (finishedPlayers.getBustedPlayers() != noPlayers) {
			for (int i = 2; i < table.get(0).size(); i++) {
				socketConnection.getOutput().println("dealerCard" + table.get(0).get(i)); // Sends the clients the
																							// dealers
			}
		}
		socketConnection.getOutput().println("dealerDone"); // Tells the client the dealer is finished
		System.out.println("Dealer done");
		socketConnection.setInLobby(true);
		Session.setSessionend(socketConnection.getUsername(), sessionID);
		socketConnection.getSessionWait().release();
		System.out.println("player released");
	}

	public void triggerBarrier() {
		try {
			System.out.println("entered trigger");
			gameQueue.remove(socketConnection);
			Session.setSessionend(socketConnection.getUsername(), sessionID);
			switch (barriers) {
			case 0:
				finishedPlayers.playerBet();
			case 1:
				System.out.println("releasing");
				if (active)
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
