package testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


import client.GameController;

class GameControllerTests {
	
	GameController gameController;

	@Test
	void test1() {
		String actual = GameController.cardToFile("A Hearts");
		String expected = "a_of_hearts.png";
		assertEquals(expected, actual);		
	}
	
	@Test
	void test2() {
		String actual = GameController.cardToFile("10 Spades");
		String expected = "10_of_spades.png";
		assertEquals(expected, actual);		
	}
	
	@Test
	void test3() {
		String actual = GameController.cardToFile("K Clubs");
		String expected = "k_of_clubs.png";
		assertEquals(expected, actual);		
	}
	
	@Test
	void test4() {
		String actual = GameController.cardToFile("4 Diamonds");
		String expected = "4_of_diamonds.png";
		assertEquals(expected, actual);		
	}

}
