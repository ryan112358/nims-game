package game.android;
/**
 * Stone Game:
 * 
 * @author Ryan McKenna & Jacob Aimino
 * 
 * TableView: a class that handles the visual representation of the game
 * 		- Extends View
 * 		- Handles touch events
 */

import java.util.ArrayList;

import game.GameState;
import game.GameState.Mode;
import game.NimGame;
import game.NimGame2;
import game.Pile;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

class NoStonesException extends Exception {
	private static final long serialVersionUID = 1L;}
class DiffStonesException extends Exception {
	private static final long serialVersionUID = 1L;}

public class TableView extends View {

	//Screen size which changes once the app is running entirely
	int width;
	int height = 300;

	//Sizes for top bar and skill bar (Will change once the game is running based on scaling)
	int skillBarWidth;
	int skillBarHeight;
	int winLoseBarHeight;
	int topBarHeight = 0;

	
	GameState state;
	
	//Radius of stones
	float radius = 5;
	
	//Whether or not the "Play" button is touched
	boolean goTouched = false;
	
	//Whether or not to draw marbles, or plain stones
	boolean texturedStones = ((GameController)this.getContext()).getTexturedStones();

	//Used when AI takes stones
	int pile = 0;
	int amt = 0;

	ArrayList<Pile> piles;
	ArrayList<ArrayList<Pile>> aiStackPiles = new ArrayList<ArrayList<Pile>>();

	//Defines paints to be initialized later
	Paint stonePaint;
	Paint textPaint;
	Paint textPaint2;
	Paint goPaint;
	Paint goPaint2;
	Paint shadowPaint;
	Paint winlosePaint;

	//Max number of stones in each pile
	int maxStones;
	
	//Determines how many stones are in each row
	int pileWidth;

	//Current win/loss count. Is sent to activity once the player leaves the table view (See updateWinsLosses())
	int winCount;
	int loseCount;

	//Initialize Image Resources
	Bitmap woodBMP;
	Bitmap marble;
	Button goButton;
	Button hintButton;

	//Helps to orient piles on screen.
	float startLocation; //location for first pile.
	
	//Used when AI is making it's turn
	boolean animate = false;
	
	//Representation of AI's move as a short
	short aiMove;

	
	public void win() {
		winCount++;
	}
	public void lose() {
		loseCount++;
	}
	public int getWinCount() {
		return this.winCount;
	}
	public int getLoseCount() {
		return this.loseCount;
	}
	public void resetWinLose() {
		winCount = 0;
		loseCount = 0;
	}

	/**
	 * Takes in a short representation of the AI move, and updates the visual
	 * representation of the game.
	 */
	public void update(short aiMove){
		if(state.getMultHeaps())
			update2h(aiMove);
		else
			update1h(aiMove);
		animate = true;
		this.aiMove = aiMove;
		this.invalidate();
	}
	/**
	 * update function for 1 heap game
	 */
	public void update1h(short aiMove) {
		int pile = aiMove / 10;
		int amt = aiMove % 10;
		for(int i=0; i<amt; i++) {
			aiStackPiles.add(copy(piles));
			aiTakeOne(pile);
		}
		aiStackPiles.add(copy(piles));
		piles.get(pile).setNumTakePile(0);
		aiStackPiles.add(copy(piles));
	}
	/**
	 * update function for multiple heap game
	 */
	public void update2h(short aiMove) {
		int amt = aiMove % 10;
		int[] tPiles = NimGame2.interpMove(aiMove);
		for(int k=0; k<amt; k++) {
			for(int i=0; i<tPiles.length; i++) {
				aiStackPiles.add(copy(piles));
				aiTakeOne(tPiles[i]);
			}
		}
		aiStackPiles.add(copy(piles));
		for(int j=0; j<tPiles.length; j++) {
			piles.get(tPiles[j]).setNumTakePile(0);
		}
		aiStackPiles.add(copy(piles));
	}

	public ArrayList<Pile> copy(ArrayList<Pile> p) {
		ArrayList<Pile> result = new ArrayList<Pile>(p.size());
		for(Pile pile: p)
			result.add(pile.copy());
		return result;
	}
	/**
	 * AI takes one stone from given pile and puts it in the take pile
	 */
	public void aiTakeOne(int pile) {
		piles.get(pile).addStoneTake();
		this.invalidate();
	}

	/**
	 * Updates the game state based on the current view.  Takes away any stones in the upper piles
	 */
	public void finishTurn() throws NoStonesException, DiffStonesException {
		try {
			validMove();
		} catch(NoStonesException e) {
			throw e;
		} catch(DiffStonesException e) {
			throw e;
		}
		for (int i=0;i<piles.size();i++){
			if(piles.get(i).getNumTakePile() > 0) {
				state.take(i,piles.get(i).getNumTakePile());
				piles.get(i).setNumTakePile(0);
			}
		}
		this.invalidate();
	}

	/**
	 * Initializes paints to be used throughout the view.
	 */
	private void initialize() {

		stonePaint = new Paint();
		stonePaint.setARGB(255, 77, 85, 145);
		stonePaint.setStyle(Style.FILL);
		stonePaint.setAntiAlias(true);

		textPaint = new Paint();
		textPaint.setColor(Color.WHITE);
		textPaint.setTypeface(Typeface.SANS_SERIF);
		textPaint.setStyle(Style.FILL);
		textPaint.setAntiAlias(true);

		winlosePaint = new Paint();
		winlosePaint.setColor(Color.WHITE);
		winlosePaint.setTypeface(Typeface.SANS_SERIF);
		winlosePaint.setStyle(Style.FILL);
		winlosePaint.setAntiAlias(true);

		textPaint2 = new Paint();
		textPaint2.setColor(Color.BLACK);
		textPaint2.setTypeface(Typeface.SANS_SERIF);
		textPaint2.setStyle(Style.FILL);
		textPaint2.setAntiAlias(true);

		goPaint = new Paint();
		goPaint.setARGB(255, 255, 255, 255);
		goPaint.setStyle(Style.FILL);
		goPaint.setAntiAlias(true);

		goPaint2 = new Paint();
		goPaint2.setARGB(255, 39, 73, 186);
		goPaint2.setStyle(Style.FILL);
		goPaint2.setAntiAlias(true);

		shadowPaint = new Paint();
		shadowPaint.setARGB(200, 0, 0, 0);
		shadowPaint.setStyle(Style.FILL);
		shadowPaint.setAntiAlias(true);

	}

	/**
	 * Main constructor. Not XML compatible.
	 * @param context
	 */
	public TableView(Context context) {
		super(context);
		initialize();
		maxStones = ((GameController)this.getContext()).getMaxStones();
		if(maxStones <= 9) pileWidth = 3;
		else pileWidth = 4;
	}
	
	/**
	 * Main touch event handler. Calls other methods to determine what the player has done. If the AI is moving, touch is ignored.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent e){

		if(animate) return true;

		if (MotionEvent.ACTION_DOWN == e.getAction()) {
//			if(e.getX() < 50)
//				showHint(); //make button here!

			if(state.getMultHeaps())
				touchMultHeaps(e);
			else
				touch1Heap(e);

			if (goButton.onButton(e.getX(), e.getY())){
				goTouched = true;
			}
			else 
				goTouched = false;
		}

		if (MotionEvent.ACTION_UP == e.getAction()){
			if (goButton.onButton(e.getX(), e.getY())){
				goTouched = false;
				((GameController)(super.getContext())).nextTurn();
			}
		}

		if (MotionEvent.ACTION_MOVE == e.getAction()){
			goTouched = false;
		}
		this.invalidate();

		return true;
	}

	/**
	 * Determines whether the player has touched a pile in single-heap mode.
	 * @param e
	 */
	public void touch1Heap(MotionEvent e) {
		int tkPile = this.maxInPile();
		for(Pile p: piles) {
			if(p.onPile(e.getX(), e.getY(), radius) && p.getNumTakePile() == tkPile) {
				p.addStoneTake();
			}
			if(p.onTakePile(e.getX(), e.getY(), radius)) {
				p.removeStoneTake();
			}
		}
	}

	/**
	 * Determines whether the player has touched a pile in multi-heap mode.
	 * @param e
	 */
	public void touchMultHeaps(MotionEvent e) {
		for(Pile p: piles){
			if(p.onPile(e.getX(),e.getY(), radius)) {
				p.addStoneTake();
			}
			else if (p.onTakePile(e.getX(),e.getY(), radius))
				p.removeStoneTake();
		}
	}

	/**
	 * Determines whether the player has made a valid move. Throws an exception otherwise, which will result in an alert appearing on the screen
	 * telling the player which mistake was made. Does not allow the player's turn to end unless the move is valid
	 * @return
	 * @throws DiffStonesException
	 * @throws NoStonesException
	 */
	public boolean validMove() throws DiffStonesException, NoStonesException {
		if(state.getMultHeaps()) {
			int mr = 0;
			for(Pile pile: piles) {
				int temp = pile.getNumTakePile();
				if(mr != 0 && temp != 0 && temp != mr)
					throw new DiffStonesException();
				else if(temp != 0) mr = temp;
			}
			if(mr != 0) 
				return true;
			else 
				throw new NoStonesException();
		}
		else {
			int mr = 0;
			for(Pile pile: piles) {
				int temp = pile.getNumTakePile();
				if(temp != 0) {
					if(mr != 0) return false;
					else mr = temp;
				}
			}
			if(mr != 0)
				return true;
			else
				throw new NoStonesException();
		}
	}
	/**
	 * returns the number of stones in the take pile for the pile
	 * that has stones in the take pile
	 */
	public int maxInPile() {
		for(Pile pile: piles) 
			if(pile.getNumTakePile() > 0) 
				return pile.getNumTakePile();
		return 0;
	}

	/**
	 * (non-Javadoc)
	 * Main drawing function which uses other drawing methods to represent the table and all graphical elements
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(woodBMP, 0, 0, null);
		drawGoButton(canvas);
		drawWinLose(canvas);

		if(animate) {
			drawPiles(canvas, aiStackPiles.remove(0));
			try {
				Thread.sleep(200L);
			} catch (InterruptedException e) {}
			if(aiStackPiles.size()==0) {
				animate = false;
				state.aiGo(aiMove);
				if (state.gameOver()) {
					lose();
					((GameController)(this.getContext())).updateSkill(false);
					((GameController)(this.getContext())).alertPlayAgain("You lose!");
					return; }
			}
			this.invalidate();
		}
		else
			drawPiles(canvas, piles);
	}

	/**
	 * Draws the player's current wins and losses count in singleplayer. In two player, it draws each player's win count.
	 * Also draws the skill level of both players in single player
	 * 
	 */
	public void drawWinLose(Canvas canvas) {

		//This draws your wins and losses count
		String win, lose;
		if(state.getMode() == Mode.ONEPLAYER) {
			win = "Wins: ";
			lose = "Losses: ";
		}
		else {
			win = "Player 1: ";
			lose = "Player 2: ";
		}

		lose += loseCount;
		win += winCount;

		Rect bounds = new Rect();
		winlosePaint.getTextBounds(lose,0,lose.length(),bounds);

		topBarHeight = bounds.height() + (height/15);

		canvas.drawRect(width, 0, 0, topBarHeight, shadowPaint);

		canvas.drawText(win, width/20, height/15, winlosePaint);
		canvas.drawText(lose, 19*width/20 - bounds.width(), height/15, winlosePaint);


		//This part puts the skill level of both players in the middle of the black bar
		if(state.getMode() == Mode.ONEPLAYER) {
			String skill = "You: " + ((GameController)(this.getContext())).skillLevel(true, true);
			String aiLevel = "AI: " + ((GameController)(this.getContext())).skillLevel(false, true);
			String result = skill + "          " + aiLevel;

			Rect bounds2 = new Rect();
			winlosePaint.getTextBounds(result,0,result.length(),bounds2);

			canvas.drawText(result, width/2 - (bounds2.width()/2), height/15, textPaint);
		}
	}

	/**
	 * Draws piles on screen based on positions set during placePiles(), as well as draws stones based on pile's coordinates
	 * @param canvas
	 * @param piles
	 */
	public void drawPiles(Canvas canvas, ArrayList<Pile> piles) {
		for (Pile p : piles) {
			if (texturedStones){
				for(int i=0; i<p.getNumStones(); i++) {
					canvas.drawBitmap(marble, p.getX()+(i%pileWidth)*2*radius, p.getY()+(i/pileWidth)*2*radius, new Paint());
				}

				for (int i=0;i<p.getNumTakePile();i++){
					canvas.drawBitmap(marble, p.getX()+(i%pileWidth)*2*radius, p.getY()-4*radius, new Paint());
				}
			}
			else {
				for(int i=0; i<p.getNumStones(); i++) {
					canvas.drawCircle(p.getX()+(i%pileWidth)*2*radius + 2, p.getY()+(i/pileWidth)*2*radius + 2, radius, shadowPaint);
					canvas.drawCircle(p.getX()+(i%pileWidth)*2*radius, p.getY()+(i/pileWidth)*2*radius, radius, stonePaint);
				}

				for (int i=0;i<p.getNumTakePile();i++){
					canvas.drawCircle(p.getX()+(i%pileWidth)*2*radius + 2, p.getY()-3*radius + 2, radius, shadowPaint);
					canvas.drawCircle(p.getX()+(i%pileWidth)*2*radius, p.getY()-3*radius, radius, stonePaint);
				}
			}
		}
	}

	/**
	 * Draws the "Play" button in the center on the button of the screen. (Originally called the go button)
	 * 
	 */
	public void drawGoButton(Canvas c){
		Paint bPaint, tPaint;
		if (goTouched){
			bPaint = goPaint2;
			tPaint = textPaint;
		}
		else {
			bPaint = goPaint;
			tPaint = textPaint2;
		}
		c.drawRoundRect(new RectF(goButton.getX() + 3, goButton.getY() + 3, goButton.getX() + goButton.getWidth() + 3, goButton.getY() + goButton.getHeight() + 3), 5, 5, shadowPaint);
		c.drawRoundRect(new RectF(goButton.getX(), goButton.getY(), goButton.getX() + goButton.getWidth(), goButton.getY() + goButton.getHeight()), 5, 5, bPaint);

		//Sets up exact position of Play
		Rect bounds = new Rect();
		textPaint.getTextBounds(goButton.getText(),0,goButton.getText().length(), bounds);

		c.drawText(goButton.getText(), goButton.getX() + (goButton.getWidth() - bounds.width())/2, goButton.getY() + (goButton.getHeight() + bounds.height())/2, tPaint);
		//			goTouched = false; //Stops highlighting of go button
		this.invalidate();
	}


	/**
	 * Assigns an x and y to each pile, which will determine where each marble is drawn in the piles
	 */
	public void placePiles(){
		startLocation = (width - (state.getPiles().length*(1+pileWidth) - 1)*radius*2) / 2.0F;
		piles = new ArrayList<Pile>();
		for (int i=0;i<state.getPiles().length;i++){
			Pile temp =
					new Pile(
							(float) (startLocation + i * (pileWidth + 1) * 2 * radius),
							(float) (height / 3) + 10, 
							state.getPileSize(i),
							pileWidth);
			piles.add(temp);
		}
	}

	/**
	 * Method which makes optimal move for player if he/she requests a hint. (Not currently used)
	 */
	public void showHint() {
		short hint = state.getHint();
		if(hint == -1)
			return;
		for(Pile p: piles)
			p.undo();
		int amt = hint % 10;
		int[] piles;
		if(state.getMultHeaps())
			piles = NimGame2.interpMove(hint);
		else
			piles = new int[] { hint / 10 };
		for(int j=0; j<amt; j++)
			for(int i=0; i<piles.length; i++)
				aiTakeOne(piles[i]);
		this.invalidate();
	}

	/**
	 * This method is called by the Android platform when the app window size changes.
	 * We store the initial setting of these so that we can compute the exact locations
	 * to draw the components of our View.
	 */
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		width = w;
		height = h;
		float scale = width < height ? width : height;

		//Scale relative object sizes
		float buttonWidth = (scale/2.1F);
		float buttonHeight = (scale/7);
		radius = (scale*3/(18*pileWidth));
		goButton = new Button((width - buttonWidth)/2, height - buttonHeight - 10, "Play", buttonWidth, buttonHeight);

		textPaint.setTextSize(scale/20);
		textPaint2.setTextSize(scale/20);
		winlosePaint.setTextSize(scale/18);

		skillBarWidth = width/2;
		skillBarHeight = (int) (scale/20);


		woodBMP = BitmapFactory.decodeResource(getResources(), R.drawable.table);
		woodBMP = Bitmap.createScaledBitmap(woodBMP, width, height, true);

		marble = BitmapFactory.decodeResource(getResources(), randMarble());
		marble = Bitmap.createScaledBitmap(marble, (int)(2*radius), (int)(2*radius), true);

		placePiles();
		this.invalidate();
	}

	
	/**
	 * Responsible for choosing a random marble image which will be used to represent each marble on the screen
	 */
	private int randMarble() {
		switch((int) (5*Math.random())) {
		case 0:
			return R.drawable.marble_blue;
		case 1:
			return R.drawable.marble_green;
		case 2:
			return R.drawable.marble_orange;
		case 3:
			return R.drawable.marble_red;
		default:
			return R.drawable.marble_purple;
		}
	}

	/**
	 * Resets the game, allowing the player to play again
	 */
	public void newGame(GameState state) {
		this.state = state;
		placePiles();
		this.invalidate();
	}
}
