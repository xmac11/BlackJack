package testing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;

import client.Client;
import client.LobbyController;
import server.Server;

public class Testing {
	private Server server;
	private LobbyController lobbyController;
	private List<List<String>> table;
	private Semaphore waitForController;
	private Client client;
	
	@Before
	public void setUp() {
		server = new Server();
		Thread gameSession = new Thread(server);
		gameSession.start();
		lobbyController = new LobbyController();
		table = new ArrayList<>();
		waitForController = new Semaphore(0);
		client = new Client(table, waitForController, lobbyController);
		
	}
	
	@Test
	public void test1() {
		Thread clientThread = new Thread(client);
		clientThread.start();
		assertTrue(clientThread.isAlive());
		//assertTrue(lobbyController.getPlayButton().isDisable());
	}
}
