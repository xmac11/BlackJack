package gui;

import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

public class ClientMoveThread implements Runnable{
	
	private GameController controller;
	private PrintWriter output;
	private Semaphore moveWait;
	
	public ClientMoveThread(GameController controller, PrintWriter output, Semaphore moveWait) {
		this.controller = controller;
		this.output = output;
		this.moveWait = moveWait;
	}

	@Override
	public void run() {
		System.out.println("Move thread started");
			try {
				moveWait.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			output.println(controller.getToSend());
			System.out.println("Move made: " + controller.getToSend());
	}

}
