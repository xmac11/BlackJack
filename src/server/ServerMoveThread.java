package server;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Semaphore;


public class ServerMoveThread implements Runnable{
	
	private PrintWriter output;
	private Semaphore moveWait;
	
	public ServerMoveThread(PrintWriter output, Semaphore moveWait) {
		this.output = output;
		this.moveWait = moveWait;
	}

	@Override
	public void run() {
		System.out.println("Chat thread started");
				try {
					moveWait.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				output.println("Make move");
		}
}
