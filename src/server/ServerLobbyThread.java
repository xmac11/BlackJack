package server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;

import shareable.GameStart;
import shareable.NewPlayer;

public class ServerLobbyThread implements Runnable {

	private SocketConnection socketConnection;
	private List<SocketConnection> gameQueue;
	private List<SocketConnection> joined;
	Semaphore gameBegin;
	private GameStart gameStart;
	private NewPlayer newPlayer;

	public ServerLobbyThread(SocketConnection socketConnection, List<SocketConnection> gameQueue,
			List<SocketConnection> joined, GameStart gameStart, NewPlayer newPlayer) {
		this.socketConnection = socketConnection;
		this.gameQueue = gameQueue;
		this.joined = joined;
		this.gameStart = gameStart;
		this.newPlayer = newPlayer;
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
		while (true) {
			while (socketConnection.isInLobby()) {
				try {
					in = socketConnection.getInput().readLine();
				} catch (IOException e) {
					System.out.println("lobby thread error");
					gameQueue.remove(socketConnection);
					joined.remove(socketConnection);
					newPlayer.setNewPlayer(true);
					return;
				}
				if (!socketConnection.isInLobby()) {
					socketConnection.getOutput().println("resend" + in);
					break;
				}
				System.out.println("lobby thread in: " + in);
				if (in.contains("gameChatMessage")) {
					for (int i = 0; i < gameQueue.size(); i++) {
						gameQueue.get(i).getOutput().println(in);
					}
				}
				if (in.equals("gameStart")) {
					gameStart.setGameStart(true);
					break;
				}
				if(in.equals("playerLeft")) {
					newPlayer.setNewPlayer(true);
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
									joined.get(i).getOutput().println("playerQueue" + gameQueue.get(j).getUsername());
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
			}

			try	{
				socketConnection.getSessionWait().acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Lobby thread restarting");
		}
	}
}
