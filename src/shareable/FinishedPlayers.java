package shareable;

/**
 * Class to create objects to be shared among threads. Each new game will have
 * a new instance of this object to hold the relevant values for that game.
 *
 * @author George
 *
 */
public class FinishedPlayers {

	private int finishedPlayers; // A finished player is a player who has made their move
	private int bustedPlayers; // Number of players with hand total > 21
	private int playersBet; // Number of players that have placed a bet

	/**
	 * Constructor used to create objects
	 */
	public FinishedPlayers() {
		finishedPlayers = 0;
		bustedPlayers = 0;
		playersBet = 0;
	}

	/**
	 * Method to increment the number of finished players
	 */
	public void playerFinished() {
		this.finishedPlayers++;
	}

	/**
	 * Method to increment the number of busted players
	 */
	public void increaseBustedPlayers() {
		bustedPlayers++;
	}

	/**
	 * Getter for the finished players
	 * 
	 * @return the number of finished players
	 */
	public int getFinishedPlayers() {
		return finishedPlayers;
	}

	/**
	 * Setter for finished players
	 * 
	 * @param finishedPlayers
	 */
	public void setFinishedPlayers(int finishedPlayers) {
		this.finishedPlayers = finishedPlayers;
	}
	
	/**
	 * Getter for the busted players
	 * @return busted players
	 */

	public int getBustedPlayers() {
		return bustedPlayers;
	}

	/**
	 * Getter for the number of players that have bet
	 * @return the number of bets
	 */
	public int getPlayersBet() {
		return playersBet;
	}

	/**
	 * Increment the number of bets
	 */
	public void playerBet() {
		this.playersBet++;
	}

	/**
	 * Decrement the number of bets
	 */
	public void playerBetLeft() {
		this.playersBet--;
	}
}
