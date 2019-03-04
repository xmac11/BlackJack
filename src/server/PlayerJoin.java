package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Semaphore;

public class PlayerJoin implements Runnable {

	int maxPlayers = 3;
	int joinedPlayers;
	ServerSocket serverSocket = null;
	List<Socket> joined;
	Socket socket = null;
	Semaphore wait;
	Boolean sessionJoinable;

	public PlayerJoin(List<Socket> joined, Semaphore wait, ServerSocket serverSocket, Boolean join) {
		this.joined = joined;
		joinedPlayers = joined.size();
		this.wait = wait;
		this.serverSocket = serverSocket;
		sessionJoinable = join;
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
		while (maxPlayers > joinedPlayers && sessionJoinable) {
			try {
				System.out.println("waiting for player...");
				socket = serverSocket.accept();
				wait.release();
				if(!sessionJoinable) {
					break;
				}
			} catch (IOException e) {
				if(!sessionJoinable) {
					System.out.println("Session no longer joinable");
					break;
				}
				e.printStackTrace();
			}
			joined.add(socket);
			joinedPlayers = joined.size();
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
