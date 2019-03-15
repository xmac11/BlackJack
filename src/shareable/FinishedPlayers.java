/**
 * Author: Group21 - Final version
 * Class FinishedPlayers: A selection class that contains simple instance variables
 */
package shareable;

public class FinishedPlayers {
	
	private int finishedPlayers;
	private int bustedPlayers;
	private int playersBet;
	
	public FinishedPlayers() {
		finishedPlayers = 0;
		bustedPlayers = 0;
		playersBet = 0;
	}
	
	public void playerFinished() {
		this.finishedPlayers++;
	}
	
	public void increaseBustedPlayers() {
		bustedPlayers++;
	}
	
	public int getFinishedPlayers() {
		return finishedPlayers;
	}

	public void setFinishedPlayers(int finishedPlayers) {
		this.finishedPlayers = finishedPlayers;
	}

	public int getBustedPlayers() {
		return bustedPlayers;
	}

	public int getPlayersBet() {
		return playersBet;
	}

	public void playerBet() {
		this.playersBet++;
	}	
}
