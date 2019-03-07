package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

	List<String> cards;

	public Deck() {
		String[] rank =  { "A", "2", "3", "4", "5", "6", "7", "8", "9", "K", "J", "Q" };
		String[] suit =  { "Hearts", "Spades", "Diamonds", "Clubs"};
		cards = new ArrayList<>();
		for(int i = 0; i<suit.length; i++) {
			for(int j = 0; j<rank.length; j++) {
				cards.add(rank[j]+" "+suit[i]);
			}
		}
		Collections.shuffle(cards);
	}
	
	public String drawCard() {
		if(cards.size() > 0)
		return cards.remove(0);
		else
			return "Deck is Empty";
	}
	
	public List<String> getDeck(){
		return cards;
	}
	
	public static int total(List<String> hand) {
		int sum = 0;
		int value = 0;
		for (int i = 0; i < hand.size(); i++) {
			char card = hand.get(i).toCharArray()[0];
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
		if (sum > 21 && (hand.contains("A Hearts") || hand.contains("A Diamonds") || hand.contains("A Clubs")
				|| hand.contains("A Spades"))) {
			sum -= 10;
		}
		return sum;
	}
}
