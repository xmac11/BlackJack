package server;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ServerChatThread implements Runnable {
	private PrintWriter output;
	Semaphore chatSend;
	List<String> message;

	public ServerChatThread(PrintWriter output, Semaphore chatSend, List<String> message) {
		this.output = output;
		this.chatSend = chatSend;
		this.message = message;
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			output.println(message.get(index));
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (threadName.equals(Thread.currentThread().getName())) {
				index++;
			}

		}
	}
}
