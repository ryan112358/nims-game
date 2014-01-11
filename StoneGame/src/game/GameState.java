package game;
/**
 * Stone Game:
 * Ryan McKenna & Jacob Aimino
 * 
 * GameState: This is a class that handles different variations of Nim Games.
 * Attributes: 
 *  - piles (int[]) - each element of this array represents the number of items in a pile
 *  - lvl (int) - difficulty level [0, 20].  Number corresponds to how many total stones must be 
 *  				present before the AI will make an optimal move.
 *  - gameMode (mode) - either single player or two player
 *
 */

public abstract class GameState {

	public enum Mode { ONEPLAYER, TWOPLAYER };
	
	private int[] piles;
	private int lvl;
	private Mode gameMode;
	private boolean multHeaps; //true if you are allowed to take from multiple heaps
	
	public static final int ELITE = 20;
	
	//if sum of pile sizes < lvl, the AI will make an optimum move
	public abstract short aiMove(int lvl);
	public abstract boolean validMove(int[] pile, int amt);
	public abstract short getHint();
	public abstract void aiGo(short move);
	
	//takes amt stones from given pile
	public void take(int pile, int amt) {
		if(amt <= piles[pile] && pile >= 0) piles[pile] -= amt;
		//else System.out.println("Illegal move!");
	}
	//amount to take from each pile
	public void take(int[] pAmts) {
		for(int k=0; k<piles.length; k++)
			piles[k] -= pAmts[k];
	}
	public void take(int[] piles, int amt) {
		for(int pile: piles) 
			if(pile >= 0) take(pile, amt);
	}
	//return true if all piles are empty
	public boolean gameOver() {
		for(int p: piles)
			if(p != 0) return false;
		return true;
	}
	//return pile size if pile exists
	//returns 0 if it doesn't exist (so we can play with n piles without code breaking)
	public int getPileSize(int i) {
		if(i < getPiles().length && i >= 0) return piles[i];
		else return 0; //index doesnt exist in array
	}
	//creates an instance of a gamestate with given number of piles,
	//and piles sizes generated randomly on the interval [1, mxAmt]
	//and a level of level
	public GameState(int numPiles, int minAmt, int mxAmt, int lvl) {
		int[] temp = new int[numPiles];
		for(int i=0; i<numPiles; i++) {
			temp[i] = minAmt + (int) (Math.random()*(mxAmt - minAmt + 1));
		}
		this.piles = temp;
		this.lvl = lvl;
	}
	
	public int sumPileSizes() {
		int result = 0;
		for(int x: piles)
			result += x;
		return result;
	}
	
	public int getLevel() {
		return this.lvl;
	}
	public void setLevel(int l) {
		this.lvl = l;
	}
	public int[] getPiles() {
		return piles;
	}
	public void setPiles(int[] piles) {
		this.piles = piles;
	}	
	public Mode getMode() {
		return gameMode;
	}	
	public void setMode(Mode gm) {
		this.gameMode = gm;
	}
	public void setMultHeaps(boolean b) {
		this.multHeaps = b;
	}
	public boolean getMultHeaps() {
		return this.multHeaps;
	}

}
