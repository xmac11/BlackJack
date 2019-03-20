/**
 * Author: Group21 - Final version
 * Class ServerLobbyThread: Handle clients' requests whilst they are in the lobby
 */
package server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;
import database.MatchHistory;
import shareable.GameStart;

public class ServerLobbyThread implements Runnable {

	private SocketConnection socketConnection;
	private List<SocketConnection> gameQueue;
	private List<SocketConnection> joined;
	Semaphore gameBegin;
	private GameStart gameStart;
	private static final int MINBET = 5; // minimum allowed bet

	public ServerLobbyThread(SocketConnection socketConnection, List<SocketConnection> gameQueue,
			List<SocketConnection> joined, GameStart gameStart) {
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
				socketConnection.getOutput().println("playerJoinedQueue" + gameQueue.get(j).getUsername());
			}
			socketConnection.getOutput().println("activeGame" + gameStart.isGameStart());
			for (int i = 0; i < joined.size(); i++) {
				if (!socketConnection.getUsername().equals(joined.get(i).getUsername()))
					socketConnection.getOutput().println("newPlayer" + joined.get(i).getUsername());
				joined.get(i).getOutput().println("newPlayer" + socketConnection.getUsername());
			}
		}
		while (true) { // the thread reads the input from the client and then inspect several
						// conditional statements
			System.out.println(socketConnection.getUsername() + " back in lobby");
			while (socketConnection.isInLobby()) {
				try {
					in = socketConnection.getInput().readLine(); // ensuring that loop can break by using readline() as
																	// blocking statement
					System.out.println("lobby thread in: " + in);
					if (in.startsWith("lobbyChatMessage")) {
						String toSend = socketConnection.getInput().readLine().substring(16) + " > "
								+ socketConnection.getInput().readLine().substring(16);
						System.out.println("Sending chat message");
						for (int i = 0; i < joined.size(); i++) {
							synchronized (joined.get(i).getOutput()) {
								joined.get(i).getOutput().println("lobbyChatMessage" + toSend);
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
						joined.remove(socketConnection);
						gameQueue.remove(socketConnection);
						for (int i = 0; i < joined.size(); i++) {
							synchronized (joined.get(i).getOutput()) {
								joined.get(i).getOutput().println("playerSignedOut" + socketConnection.getUsername());
								joined.get(i).getOutput().println("playerLeftQueue" + socketConnection.getUsername());
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
									System.out.println(gameQueue);
									socketConnection.getOutput().println("queueJoined");
									for (int i = 0; i < joined.size(); i++) {
										synchronized (joined.get(i).getOutput()) {
											joined.get(i).getOutput()
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
								for (int i = 0; i < joined.size(); i++) {
									synchronized (joined.get(i).getOutput()) {
										joined.get(i).getOutput()
												.println("playerLeftQueue" + socketConnection.getUsername());
									}
								}
							}
						}
					}
				} catch (IOException e) {
					System.out.println("lobby thread error");
					gameQueue.remove(socketConnection);
					joined.remove(socketConnection);
					for (int i = 0; i < joined.size(); i++) {
						joined.get(i).getOutput().println("playerSignedOut" + socketConnection.getUsername());
					}
					return;
				}
			}
			System.out.println("broken from lobby: " + socketConnection.getUsername());
			try {
				socketConnection.getSessionWait().acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Lobby thread restarting");
		}
	}
}
