package shareable;

import java.net.ServerSocket;

public class ServerSock {
	
	public ServerSock(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	ServerSocket serverSocket;
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

}
