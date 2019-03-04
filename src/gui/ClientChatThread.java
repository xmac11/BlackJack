package gui;

import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

public class ClientChatThread implements Runnable{
	
	private Controller controller;
	private PrintWriter output;
	private Semaphore chatWait;
	private int ID;
	
	public ClientChatThread(Controller controller, PrintWriter output, Semaphore chatWait, int ID) {
		this.controller = controller;
		this.output = output;
		this.chatWait = chatWait;
		this.ID = ID;
	}

	@Override
	public void run() {
		System.out.println("Server chat thread started");
		while(true) {
				try {
					chatWait.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Sent chat to server: " + controller.getChat());
				output.println("chatMessage" + ID + controller.getChat());
				controller.setChatMessage("");
		}
	}

}
