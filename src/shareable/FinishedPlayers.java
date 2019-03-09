package shareable;

public class FinishedPlayers {
	
	private int finishedPlayers;
	private int bustedPlayers;
	
	public FinishedPlayers() {
		finishedPlayers = 0;
		bustedPlayers = 0;
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
}
