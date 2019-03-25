package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import shareable.GameStart;

/**
 * Class responsible for accepting new client connections
 * 
 * @author Group 21
 *
 */
public class PlayerJoin implements Runnable {

	ServerSocket serverSocket;
	Map<String, SocketConnection> joined;
	List<SocketConnection> gameQueue;
	Socket socket = null;
	boolean sessionJoinable;
	GameStart gameStart;
	private int port;
	private ServerController serverController;

	/**
	 * Constructor to create Player Join object
	 * 
	 * @param joined           list of active connections
	 * @param gameQueue        list of connections in the game queue
	 * @param serverSocket     the server socket to connect to
	 * @param gameStart        the shareable variable to determine when a game
	 *                         starts
	 * @param port             the port to bind to
	 * @param serverController instance of the server controller to modify GUI
	 *                         elements from this class
	 */

	public PlayerJoin(Map<String, SocketConnection> joined, List<SocketConnection> gameQueue, ServerSocket serverSocket,
			GameStart gameStart, int port, ServerController serverController) {
		this.port = port;
		this.serverController = serverController;
		this.joined = joined;
		this.serverSocket = serverSocket;
		this.gameQueue = gameQueue;
		sessionJoinable = true;
		this.gameStart = gameStart;
	}

	/**
	 * Run method to be executed when a new thread of this class is created. The
	 * method sits in an infinite loop, accepting connections, then creating a
	 * 'ServerLobbyThread' instance for each connection
	 */
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port); // Creates a ServerSocket
			InetAddress inetAddress = InetAddress.getLocalHost(); // LocalHost IP is added
			serverController.setIP(InetAddress.getLocalHost().getHostAddress());
			System.out.println("Connection launched on : " + inetAddress.getHostAddress());
		} catch (IOException e) {
			System.exit(0);
			e.printStackTrace();
		}
		// Infinite loop while waiting for players to join
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
				username = input.readLine(); // Client is asked for their username
			} catch (IOException exception) {
				System.out.println("Error when joining");
				return;
			}
			if (joined.containsKey(username)) { // Checks if account is already in use and close the socket
				output.println("accountAlreadyActive");
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sessionJoinable = false;
			}
			// SocketConnection is instantiated with the relevant information and added to
			// the map of connected users
			if (sessionJoinable) {
				System.out.println(username + " has joined");
				SocketConnection socketConnection = new SocketConnection(socket, new Semaphore(0), output, input, true,
						username);
				joined.put(username, socketConnection);
				Runnable runnable = new ServerLobbyThread(socketConnection, gameQueue, joined, gameStart); // Instance
																											// of
																											// ServerLobbyThread
																											// is
																											// created
				Thread thread = new Thread(runnable); // Thread starts and loops back to wait for another connection
				thread.start();
			}
			sessionJoinable = true;
		}
	}
}
