package server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import database.MatchHistory;
import shareable.GameStart;

/**
 * Class responsible for handling user requests whilst they are in the lobby
 * 
 * @author Group 21
 *
 */
public class ServerLobbyThread implements Runnable {

	private SocketConnection socketConnection;
	private List<SocketConnection> gameQueue;
	private Map<String, SocketConnection> joined;
	Semaphore gameBegin;
	private GameStart gameStart;
	private static final int MINBET = 5; // minimum allowed bet

	/**
	 * Constructor for lobby threads
	 * 
	 * @param socketConnection the SocketConnection instance for the client
	 * @param gameQueue the list of connected players in the game queue
	 * @param joined a map of connected players
	 * @param gameStart shareable object to determine when a game should start
	 */
	public ServerLobbyThread(SocketConnection socketConnection, List<SocketConnection> gameQueue,
			Map<String, SocketConnection> joined, GameStart gameStart) {
		this.socketConnection = socketConnection;
		this.gameQueue = gameQueue;
		this.joined = joined;
		this.gameStart = gameStart;
	}

	/**
	 * ServerLobbyThread handles all the following: Sending lobby chat messages,
	 * handling user requests, initiating the game (gameStart)
	 */
	@Override
	public void run() {
		String in = "";
		synchronized (socketConnection.getOutput()) {
			for (int j = 0; j < gameQueue.size(); j++) {
				socketConnection.getOutput().println("playerJoinedQueue" + gameQueue.get(j).getUsername()); //Tells new client who is in queue
			}
			socketConnection.getOutput().println("activeGame" + gameStart.isGameStart()); //Tells new client whether a game is in progress
			for (SocketConnection sConnection : joined.values()) {
				if (!socketConnection.getUsername().equals(sConnection.getUsername())) //Ensures it doesn't send itself its own username
					socketConnection.getOutput().println("newPlayer" + sConnection.getUsername()); //Sends connected players to new client
				sConnection.getOutput().println("newPlayer" + socketConnection.getUsername()); //Sends new client to connected players
			}
		}
		while (true) { // The thread reads the input from the client and then inspect several
						// conditional statements
			while (socketConnection.isInLobby()) {
				try {
					in = socketConnection.getInput().readLine(); // ensuring that loop can break by using readline() as
																	// blocking statement
					if (in.startsWith("lobbyChatMessage")) {
						String toSend = socketConnection.getInput().readLine().substring(16) + " > "
								+ socketConnection.getInput().readLine().substring(16);
						for (SocketConnection sConnection : joined.values()) {
							synchronized (sConnection.getOutput()) {
								sConnection.getOutput().println("lobbyChatMessage" + toSend); //Forwards chat messages to all clients
							}
						}
					}
					if (in.equals("gameStart")) {
						gameStart.setGameStart(true);
						break; // loop ends, this ensures that it does not intercept any messages required for
								// the game sequence
					}
					if (in.equals("breakFromLobby")) {
						break; // loop ends, this ensures that it does not intercept any messages required for
								// the game sequence
					}
					if (in.equals("thisPlayerSignedOut")) {
						joined.remove(socketConnection.getUsername()); //Removes player from connected map
						gameQueue.remove(socketConnection);
						for (SocketConnection sConnection : joined.values()) {
							synchronized (sConnection.getOutput()) {
								sConnection.getOutput().println("playerSignedOut" + socketConnection.getUsername());
								sConnection.getOutput().println("playerLeftQueue" + socketConnection.getUsername());
							}
						}
						return;
					}
					if (in.equals("joinQueue")) {
						/*
						 * client requests to join the game, thread accesses the ArrayList using a
						 * synchronized statement to ensure multiple threads cannot add their paired
						 * client at the same time, ensuring the max player limit is not breached
						 */
						synchronized (gameQueue) { //
							if (gameQueue.size() < 3) {
								if (MatchHistory.getAmount(socketConnection.getUsername()) >= MINBET) { // check if the
																										// funds are
																										// enough
									gameQueue.add(socketConnection);
									socketConnection.getOutput().println("queueJoined");
									for (SocketConnection sConnection : joined.values()) {
										synchronized (sConnection.getOutput()) {
											sConnection.getOutput()
													.println("playerJoinedQueue" + socketConnection.getUsername());
										}
									}
								} else {
									socketConnection.getOutput().println("insufficientFunds");
								}
								if (gameQueue.size() == 3) {
									gameStart.setGameStart(true);
									break; // loop ends, this ensures that it does not intercept any messages required
											// for the game sequence
								}
							}
						}
					}
					if (in.equals("leaveQueue")) {
						synchronized (gameQueue) {
							if (gameQueue.size() < 3) {
								socketConnection.getOutput().println("queueLeft");
								gameQueue.remove(socketConnection);
								for (SocketConnection sConnection : joined.values()) {
									synchronized (sConnection.getOutput()) {
										sConnection.getOutput()
												.println("playerLeftQueue" + socketConnection.getUsername());
									}
								}
							}
						}
					}
				} catch (IOException e) {
					System.out.println(socketConnection.getUsername() + "has left");
					gameQueue.remove(socketConnection);
					joined.remove(socketConnection.getUsername());
					for (SocketConnection sConnection : joined.values()) {
						sConnection.getOutput().println("playerSignedOut" + socketConnection.getUsername());
					}
					return;
				}
			}
			try {
				socketConnection.getSessionWait().acquire(); //Waits here until player has finished game
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
