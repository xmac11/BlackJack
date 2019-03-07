package server;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ServerChatThread implements Runnable {
	private PrintWriter output;
	Semaphore chatSend;
	List<String> messages;
	private boolean playerLeft = false;

	public ServerChatThread(PrintWriter output, Semaphore chatSend, List<String> messages) {
		this.output = output;
		this.chatSend = chatSend;
		this.messages = messages;
	}

	@Override
	public void run() {
		System.out.println("Server chat thread started");
		int index = 0;
		String threadName = "";
		synchronized (threadName) {
			threadName = Thread.currentThread().getName();
		}
		while (true) {
			try {
				chatSend.acquire();
				output.println(messages.get(index));
				Thread.sleep(500);
				synchronized (threadName) {
					if(playerLeft) {
						threadName = Thread.currentThread().getName();
						playerLeft = false;
					}
				}
				if (threadName.equals(Thread.currentThread().getName())) {
					index++;
				}
			} catch (InterruptedException e) {
				if(threadName.equals(Thread.currentThread().getName())) {
					playerLeft = true;
				}
				e.printStackTrace();
				return;
			}

		}
	}
}
