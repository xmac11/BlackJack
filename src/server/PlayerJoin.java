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
	ServerSocket serverSocket = null;
	List<SocketConnection> joined;
	List<SocketConnection> gameQueue;
	Socket socket = null;
	boolean sessionJoinable;
	GameStart gameStart;

	public PlayerJoin(List<SocketConnection> joined, List<SocketConnection> gameQueue, ServerSocket serverSocket,
			GameStart gameStart) {
		this.joined = joined;
		this.serverSocket = serverSocket;
		this.gameQueue = gameQueue;
		sessionJoinable = true;
		this.gameStart = gameStart;
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
		while (true) {
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
			for (int i = 0; i < joined.size(); i++) {
				if (joined.get(i).getUsername().equals(username)) {
					output.println("accountAlreadyActive");
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					sessionJoinable = false;
				}
			}
			if (sessionJoinable) {
				SocketConnection socketConnection = new SocketConnection(socket, new Semaphore(0), output, input, true, username);
				joined.add(socketConnection);
				Runnable runnable = new ServerLobbyThread(socketConnection, gameQueue, joined, gameStart);
				Thread thread = new Thread(runnable);
				thread.start();
			}
			sessionJoinable = true;
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
