package server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 * Class to hold all the important information about a new connection
 * 
 * @author Group 21
 *
 */
public class SocketConnection {

	private Semaphore sessionWait; // thread synchronization tool
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private boolean inLobby;
	private String username;

	/**
	 * Constructor for the class
	 * 
	 * @param socket      the socket of the connection
	 * @param sessionWait the semaphore used to wait outside the lobby until a game
	 *                    has finished
	 * @param output      the PrintWriter to send messages over the connection
	 * @param input       the BufferedReader used to receive messages on the
	 *                    connection
	 * @param inLobby     determines whether a user is in the lobby
	 * @param username    the username of the connected user
	 */
	public SocketConnection(Socket socket, Semaphore sessionWait, PrintWriter output, BufferedReader input,
			boolean inLobby, String username) {
		this.socket = socket;
		this.sessionWait = sessionWait;
		this.input = input;
		this.output = output;
		this.setInLobby(inLobby);
		this.setUsername(username);
	}

	/**
	 * Getter for the printwriter
	 * 
	 * @return the printwriter
	 */
	public PrintWriter getOutput() {
		return output;
	}

	/**
	 * Setter for the printwriter
	 * 
	 * @param output the new printwriter
	 */
	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	/**
	 * Getter for the bufferedreader
	 * 
	 * @return the bufferedreader
	 */
	public BufferedReader getInput() {
		return input;
	}

	/**
	 * Setter for the bufferedreader
	 * 
	 * @param input the new bufferedreader
	 */
	public void setInput(BufferedReader input) {
		this.input = input;
	}

	/**
	 * Getter for the semaphore
	 * 
	 * @return the semaphore
	 */
	public Semaphore getSessionWait() {
		return sessionWait;
	}

	/**
	 * Setter for the semaphore
	 * 
	 * @param sessionWait the new semaphore
	 */
	public void setSessionWait(Semaphore sessionWait) {
		this.sessionWait = sessionWait;
	}

	/**
	 * Getter for the socket
	 * 
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Setter for the socket
	 * @param socket the new socket
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Method to determine whether a connected user is in the lobby
	 * @return is the user in lobby?
	 */
	public boolean isInLobby() {
		return inLobby;
	}

	/**
	 * Setter for the inLobby condition
	 * @param inLobby the new condition
	 */
	public void setInLobby(boolean inLobby) {
		this.inLobby = inLobby;
	}

	/**
	 * Getter for the connected user's name
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Setter for the connected user's name
	 * @param username the new username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

}
