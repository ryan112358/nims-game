package game;
/**
 * Stone Game;
 * @author Ryan McKenna & Jacob Aimino
 * 
 * Pile: To be used in the TableView class.  
 * 	Attributes:
 * 		- x: x position
 * 		- y: y position
 * 		- numStones: number of stones in the main pile
 * 		- numTakePile: number of stones in the tentative pile to be taken away by user
 */

public class Pile {
	private float x;
	private float y;
	private int numStones;
	private int numTakePile;
	
	private int pileWidth;
	
	public Pile copy() {
		Pile result = new Pile(this.x, this.y, this.numStones, this.pileWidth);
		result.setNumTakePile(this.numTakePile);
		return result;
	}
	
	public String toString() {
		return "[" + numStones + ", " + numTakePile + "]";
	}
	
	/**
	 * returns all stones back to orginal pile
	 */
	public void undo() {
		numStones += numTakePile;
		numTakePile = 0;
	}

	public Pile(float x, float y, int numStones, int pileWidth) {
		this.x = x;
		this.y = y;
		this.numStones = numStones;
		this.pileWidth = pileWidth;
	}
	/**
	 * returns true if the cursor is anywhere within the domain of the pile
	 */
	//Note, x and y correspond to top, RIGHT corner
	public boolean onPile(float x, float y, float radius){
		if (numStones == 0 && numTakePile == 0)
			return false;
		if (this.x <= x && this.x + pileWidth*2*radius >= x)
			if(this.y <= y && this.y + pileWidth*2*radius >= y)
				return true;
		return false;
	}
	
	public boolean onTakePile(float x, float y, float radius){
		if (numStones == 0 && numTakePile == 0)
			return false;
		if (this.x + pileWidth*2*radius >= x && this.x <= x)
			if(this.y - pileWidth*2*radius < y && this.y - 2*radius > y)
				return true;
		return false;
	}
	
	/**
	 * removes a stone from main pile and adds it to the upper pile
	 */
	public void addStoneTake(){
		if (numStones > 0 && numTakePile<3){
			this.numStones-=1;
			this.numTakePile+=1;
		}
	}
	public void addStonesTake(int amt) {
		if(numStones >= amt && numTakePile < 3) {
			this.numStones -= amt;
			this.numTakePile += amt;
		}
		
	}
	/**
	 * removes a stone from the upper pile and returns it to the original pile
	 */
	public void removeStoneTake(){
		if (numTakePile > 0){
			this.numTakePile-=1;
			this.numStones+=1;
		}
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public int getNumStones() {
		return numStones;
	}

	public void setNumStones(int numStones) {
		this.numStones = numStones;
	}

	public int getNumTakePile() {
		return numTakePile;
	}

	public void setNumTakePile(int numTakePile) {
		this.numTakePile = numTakePile;
	}	

}
