/**
 * Author: Group21 - Final version
 * Class Deck: Creates the game deck to be used
 */
package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

	List<String> cards;

	public Deck() { // constructs a randomized ArrayList of 52 Strings in the format Rank Suit for example "4 Clubs"
		String[] rank =  { "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K" };
		String[] suit =  { "Hearts", "Spades", "Diamonds", "Clubs"};
		cards = new ArrayList<>();
		for(int i = 0; i<suit.length; i++) {
			for(int j = 0; j<rank.length; j++) {
				cards.add(rank[j]+" "+suit[i]);
			}
		}
		Collections.shuffle(cards); // shuffling the deck
	}

	public String drawCard() {
		int size = cards.size();
		if(size > 0)
			return cards.remove(size - 1); // remove from the end of the ArrayList (more efficient operation)
		else
			return "Deck is Empty";
	}

	public List<String> getDeck(){
		return cards;
	}

	public static int total(List<String> hand) {
		int sum = 0;
		int value;
		for (int i = 0; i < hand.size(); i++) {
			String cardValue = hand.get(i).replaceAll(" .*", ""); // remove everything after the space			
			try {
				value = Integer.parseInt(cardValue);
			}
			catch(NumberFormatException e) {
				if(cardValue.equals("A")) 
					value = 11;							
				else 
					value = 10;
			}
			sum += value;
		}
		if (sum > 21 && hand.stream().anyMatch(x -> x.startsWith("A"))) { // if busted, but hand contains an Ace
			sum -= 10;
		}
		return sum;
	}
}
