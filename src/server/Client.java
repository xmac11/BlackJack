package server;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;

public class Client {

	public static void main(String[] args) {

		Client client = new Client();
		List<String> hand = new ArrayList<>();
		List<String> dealerHand = new ArrayList<>();
		// change the IP address to your server's ip address
		try (Socket socket = new Socket(InetAddress.getByName("localhost"), 9999);
				PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
			String in = "";
			System.out.println(input.readLine()); //The first message received is the greeting message so just print this
			dealerHand.add(input.readLine());
			dealerHand.add(input.readLine()); //Next messages are the dealers first hands
			hand.add(input.readLine());
			hand.add(input.readLine()); 
			System.out.println("Your Hand: " + hand + " total: " + total(hand)); //Prints the players hand
			while (true) { //Loops this until it reaches a 'break;'
				in = input.readLine();
				if(in.equals("Wait your turn...")) {
					System.out.println(in);
				}
				if(in.equals("Your turn")) {
					System.out.println(in);
				}
				if (in.equals("Make move")) {	//Reads the message received and responds accordingly
					System.out.println(in + ", h (hit) p (pass)");
					Scanner scanner = new Scanner(System.in);
					output.println(scanner.next());
				}
				if (in.contains("playerCard")) {
					hand.add(in.substring(10));
					if (total(hand) > 21) {
						System.out.println("Break");
						System.out.println("Your hand: " + hand + " total: " + total(hand));
						output.println("p");
						break;
					} else {
						output.println("move");
						System.out.println("Your hand: " + hand + " total: " + total(hand));
					}
				}
				if (in.contains("finished")) {	//Server tells the client its turn is over 
					System.out.println("Your hand: " + hand + " total: " + total(hand));
					System.out.println(in + " turn... waiting for other players");
					break;
				}
			}
			boolean dealerTurn = true;
			while (dealerTurn) { //Sits in loop whilst dealer chooses new cards
				in = input.readLine();
				if(in.contains("playersFinished")) { //Server tells client what to display
					System.out.println("All players finished");
				}
				if(in.contains("showDealerHand")) {
					System.out.println("Dealers cards: " + dealerHand + "total: " +total(dealerHand));
					System.out.println("Dealer taking cards....");
				}
				if (in.contains("dealerCard"))
					dealerHand.add(in.substring(10));
				if(in.contains("dealerDone"))
					dealerTurn = false; //Dealers turn is finished, break from loop
			}
			/*
			 * The following calculates the result of the game using the total scores of the clients hand and dealers hand
			 */
			System.out.println("Dealers cards: " + dealerHand + " total: " + total(dealerHand)); 
			if (total(hand) > 21) {
				System.out.println("Bust!! You lose!");
			}else if(total(dealerHand)>21) {
				System.out.println("Dealer bust! You Win!");
			}else if(total(hand) == total(dealerHand)) {
				System.out.println("Draw!");
			}
			else if (total(hand) > total(dealerHand)) {
				System.out.println("You win!!");
			} else {
				System.out.println("Dealer Wins!!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

}
