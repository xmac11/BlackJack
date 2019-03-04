package server;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Semaphore;


public class ServerChatThread implements Runnable{
	
	private PrintWriter output;
	private Semaphore chatWait;
	private List<String> chat;
	private int index;
	
	public ServerChatThread(PrintWriter output, Semaphore chatWait, List<String> chat, int index) {
		this.output = output;
		this.chatWait = chatWait;
		this.chat = chat;
		this.index = index;
	}

	@Override
	public void run() {
		System.out.println("Chat thread started");
		while(true) {
				try {
					chatWait.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				output.println("chatMessage" + index);
				System.out.println("Server Sending..." + chat.get(index) + " at " + index);
				output.println(chat.get(index));
		}
	}
}
