/**
 * Author: Group21 - Final version
 * Class ServerMoveThread: Sends a message to client asking for their move
 */
package server;

import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

public class ServerMoveThread implements Runnable {

	private SocketConnection socketConnection;
	private Semaphore moveWait;

	public ServerMoveThread(SocketConnection socketConnection, Semaphore moveWait) {
		this.socketConnection = socketConnection;
		this.moveWait = moveWait;
	}

	@Override
	public void run() {
		System.out.println("Chat thread started");
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
