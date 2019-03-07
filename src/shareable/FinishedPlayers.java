package shareable;

public class FinishedPlayers {
	
	private int finishedPlayers;
	
	public FinishedPlayers() {
		finishedPlayers = 0;
	}
	
	public void playerFinished() {
		this.finishedPlayers++;
	}

	public int getFinishedPlayers() {
		return finishedPlayers;
	}

	public void setFinishedPlayers(int finishedPlayers) {
		this.finishedPlayers = finishedPlayers;
	}
	
	

}
