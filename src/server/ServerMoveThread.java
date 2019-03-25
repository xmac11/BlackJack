package server;

import java.util.concurrent.Semaphore;

/**
 * Class to control users turns
 * 
 * @author Group 21
 *
 */
public class ServerMoveThread implements Runnable {

	private SocketConnection socketConnection;
	private Semaphore moveWait;

	/**
	 * Constructor for move thread
	 * @param socketConnection the SocketConnection of a user in the game
	 * @param moveWait the semaphore to ensure only 1 player gets a move at a time
	 */
	public ServerMoveThread(SocketConnection socketConnection, Semaphore moveWait) {
		this.socketConnection = socketConnection;
		this.moveWait = moveWait;
	}

	/**
	 * Method to send sequential move requests 
	 */
	@Override
	public void run() {
		try {
			moveWait.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!socketConnection.isInLobby())
			socketConnection.getOutput().println("Make move");
		else
			moveWait.release();
	}
}
