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
import shareable.NewPlayer;


public class PlayerJoin implements Runnable {

	int maxPlayers = 3;
	int joinedPlayers;
	ServerSocket serverSocket = null;
	List<SocketConnection> joined;
	List<SocketConnection> gameQueue;
	Socket socket = null;
	boolean sessionJoinable;
	GameStart gameStart;
	private NewPlayer newPlayer;

	public PlayerJoin(List<SocketConnection> joined, List<SocketConnection> gameQueue, ServerSocket serverSocket, GameStart gameStart, NewPlayer newPlayer ) {
		this.joined = joined;
		joinedPlayers = joined.size();
		this.serverSocket = serverSocket;
		this.gameQueue = gameQueue;
		sessionJoinable = true;
		this.gameStart = gameStart;
		this.newPlayer = newPlayer;
	}

	@Override
	public void run() {

		try {
			serverSocket = new ServerSocket(9999);
			InetAddress inetAddress = InetAddress.getLocalHost();
			System.out.println("Connection launched on : " + inetAddress.getHostAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (sessionJoinable) {
			try {
				System.out.println("waiting for player...");
				socket = serverSocket.accept();
				if (!sessionJoinable) {
					break;
				}
			} catch (IOException e) {
				if (!sessionJoinable) {
					System.out.println("Session no longer joinable");
					break;
				}
				e.printStackTrace();
			}
			PrintWriter output = null;
			BufferedReader input = null;
			String username = "Error";
			try {
				output = new PrintWriter(socket.getOutputStream(), true);
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				username = input.readLine();
			} catch (IOException exception) {
				System.out.println("Error when joining");
				return;
			}
			SocketConnection socketConnection = new SocketConnection(socket, new Semaphore(0), output, input, true, username);
			joined.add(socketConnection);
			joinedPlayers = joined.size();
			newPlayer.setNewPlayer(true);
			Runnable runnable = new ServerLobbyThread(socketConnection, gameQueue, joined, gameStart, newPlayer);
			Thread thread = new Thread(runnable);
			thread.start();
			System.out.println("players in lobby " + joinedPlayers);
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Maximum number of players reached, joining ended");
	}

	public void closeConnection() {
		sessionJoinable = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
