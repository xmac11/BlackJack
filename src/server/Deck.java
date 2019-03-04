package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deck {

	List<String> cards;

	public Deck() {
		String[] cardH = { "A Hearts", "2 Hearts", "3 Hearts", "4 Hearts", "5 Hearts", "6 Hearts", "7 Hearts", "8 Hearts", "9 Hearts", "K Hearts", "J Hearts", "Q Hearts" };
		String[] cardS = { "A Spades", "2 Spades", "3 Spades", "4 Spades", "5 Spades", "6 Spades", "7 Spades", "8 Spades", "9 Spades", "K Spades", "J Spades", "Q Spades" };
		String[] cardD = { "A Diamonds", "2 Diamonds", "3 Diamonds", "4 Diamonds", "5 Diamonds", "6 Diamonds", "7 Diamonds", "8 Diamonds", "9 Diamonds", "K Diamonds", "J Diamonds", "Q Diamonds" };
		String[] cardC = { "A Clubs", "2 Clubs", "3 Clubs", "4 Clubs", "5 Clubs", "6 Clubs", "7 Clubs", "8 Clubs", "9 Clubs", "K Clubs", "J Clubs", "Q Clubs" };
		cards = new ArrayList<>();
		cards.addAll(Arrays.asList(cardH));
		cards.addAll(Arrays.asList(cardS));
		cards.addAll(Arrays.asList(cardD));
		cards.addAll(Arrays.asList(cardC));
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
	
	public int total(List<String> hand) {
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
