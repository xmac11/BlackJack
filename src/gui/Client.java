package gui;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import com.sun.glass.ui.TouchInputSupport;

import java.io.*;

public class Client implements Runnable {
	
	//This is a test comment to see if GIT  works 
	// harris test3
	//Nick test3
	
	List<String> hand;
	List<String> dealerHand;
	Semaphore waitForServer;
	Semaphore chatWait;
	GameController controller;
	Semaphore waitForInput;
	private int ID;
	private int noPlayers;
	private List<List<String>> table;
	PrintWriter output;
	private String IP;

	public Client(List<List<String>> table, List<String> dealerHand, List<String> hand, Semaphore waitForServer,
			GameController controller, Semaphore waitForInput, Semaphore chatWait, String IP) {
		this.table = table;
		this.dealerHand = dealerHand;
		this.hand = hand;
		this.waitForServer = waitForServer;
		this.controller = controller;
		this.waitForInput = waitForInput;
		output = null;
		this.chatWait = chatWait;
		this.IP = IP;
	}

	public static int total(List<String> a) {
		int sum = 0;
		int value = 0;
		for (int i = 0; i < a.size(); i++) {
			char card = a.get(i).toCharArray()[0];
			if (Character.isLetter(card)) {
				if (card == 'A')
					value = 11;
				else
					value = 10;
			} else {
				value = Character.getNumericValue(card);
			}
			sum += value;
		}
		if (sum > 21 && (a.contains("A Hearts") || a.contains("A Diamonds") || a.contains("A Clubs")
				|| a.contains("A Spades"))) {
			sum -= 10;
		}
		return sum;
	}
	
	@Override
	public void run() {
		// change the IP address to your server's ip address
//		System.out.println("Enter IP");
//		Scanner scanner = new Scanner(System.in);
//		String IP = scanner.nextLine();
//		scanner.close();
		try (//Socket socket = new Socket(InetAddress.getByName(IP), 9999);
				Socket socket = new Socket(IP, 9999);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
			output = new PrintWriter(socket.getOutputStream(), true);
			
			String in = "";
			String hello = input.readLine();
			System.out.println(hello); // The first message received is the greeting message so just print this
			ID = Integer.parseInt(hello.substring(15, 16));
			controller.setID(ID);
			Runnable runnable = new ClientChatThread(controller, output, chatWait, ID);
			Thread thread = new Thread(runnable);
			thread.start();
			noPlayers = Integer.parseInt(hello.substring(26, 27));
			controller.setNoPlayers(noPlayers);
			System.out.println(noPlayers);
			table.add(new ArrayList<>());
			table.get(0).add(input.readLine());
			table.get(0).add(input.readLine()); // Next messages are the dealers first hands
			System.out.println(table.get(0) + " this is dealer");
			for (int i = 0; i < noPlayers; i++) {
				table.add(new ArrayList<>());
			}
			table.get(ID).add(input.readLine());
			table.get(ID).add(input.readLine());
			controller.setLabel("Your hand: " + total(table.get(ID)));
			System.out.println("Your Hand: " + hand + " total: " + total(table.get(ID))); // Prints the players hand
			waitForServer.release();
			while (true) { // Loops this until it reaches a 'break;'
				in = input.readLine();
				System.out.println("Client in: " + in);
				if (in.equals("Make move")) { // Reads the message received and responds accordingly
					controller.enableHit();
					controller.enableStand();
					System.out.println(in + ", h (hit) p (pass)");
					System.out.println("Waiting for move");
					controller.setLabel("Make Move: " + total(table.get(ID)));
					Runnable r = new MoveThread(controller, output, waitForInput);
					Thread t = new Thread(r);
					t.start();
				}
				if(in.contains("chatMessage")) {
					int index = Integer.parseInt(in.substring(11));
					in = input.readLine();
					controller.addToChat(index, in.substring(11,12) + " > " + in.substring(12));
				}
				if (in.contains("playerCard")) {
					String card = in.substring(10);
					table.get(ID).add(card);
					controller.addCardToPlayerHand(card);
					if (total(table.get(ID)) > 21) {
						System.out.println("Break");
						controller.setLabel("Break: " + total(table.get(ID)));
						System.out.println("Your hand: " + hand + " total: " + total(table.get(ID)));
						output.println("p");
						break;
					} else {
						output.println("move");
						System.out.println("Your hand: " + hand + " total: " + total(table.get(ID)));
					}
				}
				if (in.contains("finished")) { // Server tells the client its turn is over
					System.out.println("Your hand: " + hand + " total: " + total(table.get(ID)));
					System.out.println(in + " turn... waiting for other players");
					controller.setLabel("Waiting for others");
					break;
				}
			}
			controller.disableHit();
			controller.disableStand();
			controller.setLabel("Your hand: " + total(table.get(ID)));
			boolean dealerTurn = true;
			while (dealerTurn) { // Sits in loop whilst dealer chooses new cards
				in = input.readLine();
				if (in.contains("otherPlayer")) {
					int otherPlayerID = Integer.parseInt(input.readLine());
					List<String> cards = extractCards(input.readLine());
					for (int j = 0; j < cards.size(); j++) {
						table.get(otherPlayerID).add(cards.get(j));
					}
					System.out.println("This is table >>>");
					for (int i = 0; i < table.size(); i++) {
						System.out.println(table.get(i));
					}
					System.out.println("<<< This is table");
				}
				if (in.contains("tableSent")) {
					int playerCount = 2;
					for (int i = 1; i < table.size(); i++) {
						if (i != ID) {
							controller.removeFacedown(playerCount);
							for (int j = 0; j < table.get(i).size(); j++) {
								controller.addCardToOpposingPlayerHand(playerCount, table.get(i).get(j));
							}
							playerCount++;
						}
					}
				}
				if (in.contains("playersFinished")) { // Server tells client what to display
					System.out.println("All players finished");
				}
				if (in.contains("showDealerHand")) {
					System.out.println("Dealers cards: " + dealerHand + "total: " + total(table.get(0)));
					System.out.println("Dealer taking cards....");
				}
				if (in.contains("dealerCard"))
					table.get(0).add(in.substring(10));
				if (in.contains("dealerDone"))
					dealerTurn = false; // Dealers turn is finished, break from loop
			}
			System.out.println(table.get(0));
			controller.removeDealerFacedown();
			for (int i = 0; i < table.get(0).size(); i++) {
				controller.addCardToDealerHand(table.get(0).get(i));
			}
			/*
			 * The following calculates the result of the game using the total scores of the
			 * clients hand and dealers hand
			 */
			System.out.println("Dealers cards: " + table.get(0) + " total: " + total(table.get(0)));
			if (total(table.get(ID)) > 21) {
				controller.setLabel("Bust!! You lose!");
			} else if (total(table.get(0)) > 21) {
				controller.setLabel("Dealer bust! You Win!");
			} else if (total(table.get(ID)) == total(table.get(0))) {
				controller.setLabel("Draw!");
			} else if (total(table.get(ID)) > total(table.get(0))) {
				controller.setLabel("You win!!");
			} else {
				controller.setLabel("Dealer Wins!!");
			}
		} catch (IOException e) {
			System.out.println("Session not joinable");
			e.printStackTrace();
			output.close();
			waitForServer.release();
			return;
		}

	}
	
	public void setupTable() {
		
	}

	public List<String> extractCards(String hand) {
		List<String> cards = new ArrayList<>();
		hand = hand.substring(1);
		hand = hand.substring(0, hand.length() - 1);
		String[] arr = hand.split(", ");
		cards.addAll(Arrays.asList(arr));
		return cards;
	}
///testttttttttttttttt
}
