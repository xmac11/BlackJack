package testing;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import server.Deck;
import static junit.framework.TestCase.*;

/**
 * Class to implement tests for the creation of Decks
 *
 * @author Group 21
 *
 */
class DeckTest {


	@Test
	void test1() { // Tests if the size of the deck is 52
		Deck deck = new Deck();
		assertEquals(deck.getDeck().size(), 52);
	}
	
	@Test
	void test2() { // Tests if the .drawCard() works as intended
		Deck deck = new Deck();
		assertEquals(deck.getDeck().size(), 52);
		deck.drawCard();
		assertEquals(deck.getDeck().size(), 51);
	}
	
	@Test
	void test3() { // Tests if the addition of the cards is correct
		ArrayList<String> hand = new ArrayList<>();
		hand.add("K Hearts");
		hand.add("7 Hearts");
		assertEquals(Deck.total(hand), 17);
	}
	
	@Test
	void test4() { // Tests if the addition of the cards is correct
		ArrayList<String> hand = new ArrayList<>();
		hand.add("A Diamonds");
		hand.add("4 Spades");
		hand.add("2 Spades");
		hand.add("3 Spades");
		assertEquals(Deck.total(hand), 20);
	}
	
	@Test
	void test5() { // Tests if the addition of the cards is correct
		ArrayList<String> hand = new ArrayList<>();
		hand.add("A Spades");
		hand.add("A Hearts");
		assertEquals(Deck.total(hand), 12);
	}
	
	@Test
	void test6() { // Tests if the addition of the cards is correct
		ArrayList<String> hand = new ArrayList<>();
		hand.add("A Spades");
		hand.add("Q Hearts");
		assertEquals(Deck.total(hand), 21);
	}
	
	@Test
	void test7() { // Tests if the addition of the cards is correct
		ArrayList<String> hand = new ArrayList<>();
		hand.add("10 Spades");
		hand.add("10 Hearts");
		hand.add("A Hearts");
		assertEquals(Deck.total(hand), 21);
	}
}
