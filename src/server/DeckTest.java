package server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class DeckTest {

	@Test
	void test1() {
		Deck deck = new Deck();
		assertEquals(deck.getDeck().size(), 52);
	}
	
	@Test
	void test2() {
		Deck deck = new Deck();
		assertEquals(deck.getDeck().size(), 52);
		deck.drawCard();
		assertEquals(deck.getDeck().size(), 51);
	}
	
	@Test
	void test3() {
		ArrayList<String> hand = new ArrayList<>();
		hand.add("K Hearts");
		hand.add("7 Hearts");
		assertEquals(Deck.total(hand), 17);
	}
	
	@Test
	void test4() {
		ArrayList<String> hand = new ArrayList<>();
		hand.add("A Diamonds");
		hand.add("4 Spades");
		hand.add("2 Spades");
		hand.add("3 Spades");
		assertEquals(Deck.total(hand), 20);
	}
	
	@Test
	void test5() {
		ArrayList<String> hand = new ArrayList<>();
		hand.add("A Spades");
		hand.add("A Hearts");
		assertEquals(Deck.total(hand), 12);
	}
	
	@Test
	void test6() {
		ArrayList<String> hand = new ArrayList<>();
		hand.add("A Spades");
		hand.add("Q Hearts");
		assertEquals(Deck.total(hand), 21);
	}
	
	@Test
	void test7() {
		ArrayList<String> hand = new ArrayList<>();
		hand.add("10 Spades");
		hand.add("10 Hearts");
		hand.add("A Hearts");
		assertEquals(Deck.total(hand), 21);
	}

}
