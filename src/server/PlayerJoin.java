/**
 * Author: Group21 - Final version
 * Class PlayerJoin: Accepts and handles client connections
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Semaphore;

import shareable.GameStart;

public class PlayerJoin implements Runnable {

	int maxPlayers = 3;
	ServerSocket serverSocket;
	List<SocketConnection> joined;
	List<SocketConnection> gameQueue;
	Socket socket = null;
	boolean sessionJoinable;
	GameStart gameStart;
	private int port;
	private ServerController serverController;

	public PlayerJoin(List<SocketConnection> joined, List<SocketConnection> gameQueue, ServerSocket serverSocket,
			GameStart gameStart, int port, ServerController serverController) {
		this.port = port;
		this.serverController = serverController;
		this.joined = joined;
		this.serverSocket = serverSocket;
		this.gameQueue = gameQueue;
		sessionJoinable = true;
		this.gameStart = gameStart;
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			InetAddress inetAddress = InetAddress.getLocalHost();
			serverController.setIP(InetAddress.getLocalHost().getHostAddress());
			System.out.println("Connection launched on : " + inetAddress.getHostAddress());
		} catch (IOException e) {
			System.exit(0);
			e.printStackTrace();
		}
		// infinite loop while waiting for players to join
		while (true) {
			try {
				System.out.println("waiting for player...");
				socket = serverSocket.accept();
			} catch (IOException e) {
				System.exit(0);
			}
			PrintWriter output = null;
			BufferedReader input = null;
			String username = "Error";
			// connection is made
			try {
				output = new PrintWriter(socket.getOutputStream(), true);
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				username = input.readLine();// client is asked for their username
			} catch (IOException exception) {
				System.out.println("Error when joining");
				return;
			}
			for (int i = 0; i < joined.size(); i++) {
				if (joined.get(i).getUsername().equals(username)) { // checks if account is already in use and close the socket
					output.println("accountAlreadyActive");
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					sessionJoinable = false;
				}
			}
			// socketConnection is instantiated with the relevant information and added to the ArrayList of connected users
			if (sessionJoinable) {
				SocketConnection socketConnection = new SocketConnection(socket, new Semaphore(0), output, input, true, username);
				joined.add(socketConnection);
				Runnable runnable = new ServerLobbyThread(socketConnection, gameQueue, joined, gameStart); // instance of ServerLobbyThread is created
				Thread thread = new Thread(runnable); // thread starts and loops back to wait for another connection
				thread.start();
			}
			sessionJoinable = true;
		}
	}
}
