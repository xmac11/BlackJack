package shareable;

/**
 * Class to create an object for threads to share. Determines whether a game should begin
 *
 * @author George
 *
 */
public class GameStart {

	private boolean gameStart; // boolean method whether a game has been initiated or not

	/**
	 * Should a game begin?
	 * @return game should begin true/false
	 */
	public boolean isGameStart() {
		return gameStart;
	}

	/**
	 * Setter for the game start variable
	 * @param gameStart new value
	 */
	public void setGameStart(boolean gameStart) {
		this.gameStart = gameStart;
	}
	
	
}
