package server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;

import shareable.GameStart;

public class ServerLobbyThread implements Runnable {

	private SocketConnection socketConnection;
	private List<SocketConnection> gameQueue;
	private List<SocketConnection> joined;
	Semaphore gameBegin;
	private GameStart gameStart;

	public ServerLobbyThread(SocketConnection socketConnection, List<SocketConnection> gameQueue,
			List<SocketConnection> joined, GameStart gameStart) {
		this.socketConnection = socketConnection;
		this.gameQueue = gameQueue;
		this.joined = joined;
		this.gameStart = gameStart;
	}

	@Override
	public void run() {
		
		String in = "";
		socketConnection.getOutput().println("playerQueue");
		for (int j = 0; j < gameQueue.size(); j++) {
			socketConnection.getOutput().println("playerQueue" + gameQueue.get(j).getUsername());
		}
		socketConnection.getOutput().println("queueUpdated");
		socketConnection.getOutput().println("activeGame" + gameStart.isGameStart());
		for (int i = 0; i < joined.size(); i++) {
			if (!socketConnection.getUsername().equals(joined.get(i).getUsername()))
				socketConnection.getOutput().println("newPlayer" + joined.get(i).getUsername());
			joined.get(i).getOutput().println("newPlayer" + socketConnection.getUsername());
		}
		while (true) {
			System.out.println(socketConnection.getUsername() + " back in lobby");
			while (socketConnection.isInLobby()) {
				try {
					in = socketConnection.getInput().readLine();

					System.out.println("lobby thread in: " + in);
					if (in.contains("lobbyChatMessage")) {
						String toSend = socketConnection.getInput().readLine().substring(16) + " > "
								+ socketConnection.getInput().readLine().substring(16);
						System.out.println("Sending chat message");
						for (int i = 0; i < joined.size(); i++) {
							joined.get(i).getOutput().println("lobbyChatMessage" + toSend);
						}
					}
					if (in.equals("gameStart")) {
						gameStart.setGameStart(true);
						break;
					}
					if (in.equals("breakFromLobby")) {
						break;
					}
					if (in.equals("thisPlayerSignedOut")) {
						joined.remove(socketConnection);
						gameQueue.remove(socketConnection);
						for (int i = 0; i < joined.size(); i++) {
							joined.get(i).getOutput().println("playerSignedOut" + socketConnection.getUsername());
							joined.get(i).getOutput().println("playerQueue");
							for (int j = 0; j < gameQueue.size(); j++) {
								joined.get(i).getOutput().println("playerQueue" + gameQueue.get(j).getUsername());
							}
							joined.get(i).getOutput().println("queueUpdated");
						}
					}
					if (in.equals("joinQueue")) {
						synchronized (gameQueue) {
							if (gameQueue.size() < 3) {
								gameQueue.add(socketConnection);
								System.out.println(gameQueue);
								socketConnection.getOutput().println("queueJoined");
								for (int i = 0; i < joined.size(); i++) {
									joined.get(i).getOutput().println("playerQueue");
									for (int j = 0; j < gameQueue.size(); j++) {
										joined.get(i).getOutput()
												.println("playerQueue" + gameQueue.get(j).getUsername());
									}
									joined.get(i).getOutput().println("queueUpdated");
								}
							}
							if (gameQueue.size() == 3) {
								gameStart.setGameStart(true);
								break;
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
