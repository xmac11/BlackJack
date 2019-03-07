package server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class SocketConnection {
	
	private Semaphore sessionWait;
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private boolean inLobby;
	private String username;
	
	public SocketConnection(Socket socket, Semaphore sessionWait, PrintWriter output, BufferedReader input, boolean inLobby, String username) {
		this.socket = socket;
		this.sessionWait = sessionWait;
		this.input = input;
		this.output = output;
		this.setInLobby(inLobby);
		this.setUsername(username);
	}

	public PrintWriter getOutput() {
		return output;
	}

	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	public BufferedReader getInput() {
		return input;
	}

	public void setInput(BufferedReader input) {
		this.input = input;
	}

	public Semaphore getSessionWait() {
		return sessionWait;
	}

	public void setSessionWait(Semaphore sessionWait) {
		this.sessionWait = sessionWait;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public String toString() {
		return this.sessionWait + " " + this.socket;
	}

	public boolean isInLobby() {
		return inLobby;
	}

	public void setInLobby(boolean inLobby) {
		this.inLobby = inLobby;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
