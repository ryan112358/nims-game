package game.android;
/**
 * Stone Game:
 * @author Ryan McKenna & Jacob Aimino
 * 
 * GameController: This class handles all of our buttons, menus, and views
 * 		- Extends Activity
 */

import game.GameState;
import game.GameState.Mode;
import game.NimGame;
import game.NimGame2;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class GameController extends Activity {

	//Saved File
	public static final String PREFS_NAME = "StoneGameSave";

	LinearLayout gameLayout;
	GameState game;
	TableView gameView;

	//settings
	RadioGroup numHeaps;
	SeekBar diffBar;
	SeekBar numStonesBar;
	TextView numStonesValue;
	CheckBox multiHeap;
	//CheckBox texturedStonesBox;
	TextView aidiff;

	boolean multiHeapStats = false;

	//Saved Settings
	int difficulty = 0;
	Mode gameMode = Mode.ONEPLAYER;
	int maxStones = 9;
	int numPiles = 1;
	boolean multiHeapMode = false;
	float currentSkill = 0;
	float currentSkill2 = 0;
	boolean texturedStones = true;

	boolean inGame = false;

	//Selected Radio Button Ids
	int heapId = R.id.heap1;

	//Arrays which contain number of wins and losses on each difficulty. Lowest difficulty to highest
	int[] wins = new int[5];
	int[] losses = new int[5];

	int[] wins2 = new int[5];
	int[] losses2 = new int[5];

	boolean player1Turn = true;

	public final int maxPileSize = 16;
	public final int minPileSize = 3;
	public int totalStones;

	public float getSkillLevel(boolean inGame) {
		boolean b;
		if(inGame) b = multiHeapMode;
		else b = multiHeapStats;
		if(b)
			return getSkillLevel2();
		else
			return getSkillLevel1();
	}
	
	public float getSkillLevel1() {
		return currentSkill;
	}
	
	public float getSkillLevel2() {
		return currentSkill2;
	}

	public void updateSkill(boolean win) {
		if(game.getMultHeaps())
			updateSkill2(win);
		else
			updateSkill1(win);
	}
	
	/** parameters: win/lose (boolean) */
	public void updateSkill1(boolean win) {
		if (game.getMode() == GameState.Mode.TWOPLAYER) return;
		double probWin, winRatio, loseRatio;
		float diff = Math.min(totalStones, difficulty);
		float lvl = Math.min(currentSkill, totalStones);
		if(diff >= lvl)
			probWin = 0.75*Math.pow(0.25, (diff - lvl)/4.0);
		else 
			probWin = 0.75 + 0.25*(1.0 - Math.pow(0.25, (lvl - diff)/4.0));
		winRatio = probWin / (1.0 - probWin);
		loseRatio = 1.0 / winRatio;
		if(win)
			currentSkill += Math.cbrt(loseRatio);
		else
			currentSkill -= Math.cbrt(winRatio);
		if(currentSkill < 0) 
			currentSkill = 0;
	}
	public void updateSkill2(boolean win) {
		if (game.getMode() == GameState.Mode.TWOPLAYER) return;
		double probWin, winRatio, loseRatio;
		float diff = Math.min(totalStones, difficulty);
		float lvl = Math.min(currentSkill2, totalStones);
		if(diff >= lvl)
			probWin = 0.75*Math.pow(0.25, (diff - lvl)/4.0);
		else 
			probWin = 0.75 + 0.25*(1.0 - Math.pow(0.25, (lvl - diff)/4.0));
		winRatio = probWin / (1.0 - probWin);
		loseRatio = 1.0 / winRatio;
		if(win)
			currentSkill2 += Math.cbrt(loseRatio);
		else
			currentSkill2 -= Math.cbrt(winRatio);
		if(currentSkill2 < 0) 
			currentSkill2 = 0;
	}
	public int getMaxStones() {
		return maxStones;
	}

	public boolean getTexturedStones(){
		return this.texturedStones;
	}
	@Override
	public void onBackPressed(){
		if (inGame)
			updateWinsLosses();
		mainMenu();
	}

	@Override
	public void onStop(){
		super.onStop();
		
		//Stuff below responsible for saving basic information into a "preference file" for simple long term storage
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("difficulty", difficulty);
		editor.putInt("gameMode", gameModeToInt());
		editor.putInt("maxStones", maxStones);
		editor.putInt("numPiles", numPiles);
		editor.putBoolean("multiHeapMode", multiHeapMode);
		editor.putFloat("currentSkill", currentSkill);
		editor.putFloat("currentSkill2", currentSkill2);
		editor.putInt("heapId", heapId);
		editor.putBoolean("texturedStones", texturedStones);

		for (int i=0;i<wins.length;i++){
			editor.putInt("w"+i, wins[i]);
		}
		for (int i=0;i<losses.length;i++){
			editor.putInt("l"+i, losses[i]);
		}

		for (int i=0;i<wins2.length;i++){
			editor.putInt("2w"+i, wins2[i]);
		}

		for (int i=0;i<losses2.length;i++){
			editor.putInt("2l"+i, losses2[i]);
		}

		// Commit the edits!
		editor.commit();
	}

	@Override
	public void onResume(){
		super.onResume();
		
		//Stuff below responsible for retrieving basic information from "preference file".
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		difficulty = settings.getInt("difficulty", 0);
		setGameMode(settings.getInt("gameMode", 0));
		maxStones = settings.getInt("maxStones", 9);
		numPiles = settings.getInt("numPiles", 1);
		multiHeapMode = settings.getBoolean("multiHeapMode", false);
		currentSkill = settings.getFloat("currentSkill", 0);
		currentSkill2 = settings.getFloat("currentSkill2", 0);
		heapId = settings.getInt("heapId", R.id.heap1);
		texturedStones = settings.getBoolean("texturedStones", true);

		for (int i=0;i<wins.length;i++){
			wins[i] = settings.getInt("w"+i, 0);
		}
		for (int i=0;i<losses.length;i++){
			losses[i] = settings.getInt("l"+i, 0);
		}

		for (int i=0;i<wins2.length;i++){
			wins2[i] = settings.getInt("2w"+i, 0);
		}
		for (int i=0;i<losses2.length;i++){
			losses2[i] = settings.getInt("2l"+i, 0);
		}

	}

	/**
	 * Needed to represent game mode as basic type to store in preference file
	 * @return
	 */
	private int gameModeToInt(){
		switch (gameMode){
		case ONEPLAYER : return 0;
		default : return 1;
		}
	}

	/**
	 * Uses primitive version of game mode and turns back into enum type for use
	 * @param i
	 */
	private void setGameMode(int i){
		switch (i){
		case 0 : gameMode = Mode.ONEPLAYER;
		break;
		default : gameMode = Mode.TWOPLAYER;
		break;
		}
	}




	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Makes app full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		mainMenu();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * Puts an alert box on the screen asking if the player would like to play again
	 * @param message
	 */
	public void alertPlayAgain(String message){
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(message);
		alertDialog.setMessage("Play again?");
		alertDialog.setCancelable(false);
		alertDialog.setButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(game.getMode() == Mode.TWOPLAYER) {
					alertNextPlayer();
				}
				gameView();
			}
		});
		alertDialog.setButton2("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				updateWinsLosses();
				mainMenu();
			}
		});
		//		alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();
	}
	
	public void alertResetStats(){
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Are you sure you want to reset your stats?");
		alertDialog.setMessage("Warning: this will reset your stats for both versions of game play!");
		alertDialog.setCancelable(false);
		alertDialog.setButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				resetStats();
				return;
			}
		});
		alertDialog.setButton2("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		alertDialog.show();
	}

	/**
	 * Updates wins/losses arrays with information from latest game
	 */
	public void updateWinsLosses() {
		if(gameMode == GameState.Mode.TWOPLAYER) 
			return;

		String diff = skillLevel(false, true);
		if (game.getMultHeaps()){
			if(diff.equals("Beginner")) {
				wins2[0] += gameView.getWinCount();
				losses2[0] += gameView.getLoseCount();
			}
			else if(diff.equals("Intermediate")) {
				wins2[1] += gameView.getWinCount();
				losses2[1] += gameView.getLoseCount();
			}
			else if(diff.equals("Advanced")){
				wins2[2] += gameView.getWinCount();
				losses2[2] += gameView.getLoseCount();
			}
			else if(diff.equals("Expert")){
				wins2[3] += gameView.getWinCount();
				losses2[3] += gameView.getLoseCount();
			}
			else if(diff.equals("Master")){
				wins2[4] += gameView.getWinCount();
				losses2[4] += gameView.getLoseCount();
			}
		}
		else
		{
			if(diff.equals("Beginner")) {
				wins[0] += gameView.getWinCount();
				losses[0] += gameView.getLoseCount();
			}
			else if(diff.equals("Intermediate")) {
				wins[1] += gameView.getWinCount();
				losses[1] += gameView.getLoseCount();
			}
			else if(diff.equals("Advanced")){
				wins[2] += gameView.getWinCount();
				losses[2] += gameView.getLoseCount();
			}
			else if(diff.equals("Expert")){
				wins[3] += gameView.getWinCount();
				losses[3] += gameView.getLoseCount();
			}
			else if(diff.equals("Master")){
				wins[4] += gameView.getWinCount();
				losses[4] += gameView.getLoseCount();
			}
		}
	}

	/**
	 * Displays an alert box on the screen telling the player to pass the phone to the appropriate player
	 */
	public void alertNextPlayer(){
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setCancelable(false);
		//alertDialog.setTitle(message);
		if (player1Turn)
			alertDialog.setMessage("Pass the device to Player 2");
		else
			alertDialog.setMessage("Pass the device to Player 1");
		alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		//		alertDialog.setIcon(R.drawable.icon);
		player1Turn = ! player1Turn;
		alertDialog.show();
	}

	/**
	 * Uses exception thrown by valid move function in tableview to tell the player what he/she has done incorrectly
	 * @param msg
	 */
	public void alertIllegalMove(String msg){
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Oops!");
		alertDialog.setMessage(msg);
		alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		//		alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();
	}

	/**
	 * Sets up gameview for use by player
	 */
	public void gameView() {
		inGame = true;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		if (multiHeapMode)
			game = new NimGame2(numPiles, minPileSize, maxStones, difficulty);
		else
			game = new NimGame(numPiles, minPileSize, maxStones, difficulty);

		game.setMode(gameMode);
		gameView.newGame(game);
		totalStones = game.sumPileSizes();
		setContentView(gameView);
	}

	/**
	 * Time for AI to make it's move
	 */
	public void nextTurn(){
		try {
			gameView.finishTurn();
		} catch(NoStonesException e) {
			alertIllegalMove("You must take at least 1 stone from any pile");
			return;
		} catch(DiffStonesException e) {
			alertIllegalMove("You must take the same number of stones from each pile");
			return;
		}
		if (game.gameOver()) {
			if(game.getMode() == Mode.TWOPLAYER) {
				if(player1Turn) {
					alertPlayAgain("Player 1 wins!");
					gameView.win(); }
				else {
					alertPlayAgain("Player 2 wins!");
					gameView.lose(); }
			}
			else {
				gameView.win();
				updateSkill(true);
				alertPlayAgain("You win!");
			}
			return;
		}

		if(game.getMode() == GameState.Mode.ONEPLAYER) {
			short aiMove = game.aiMove(difficulty);

			gameView.update(aiMove);
		}
		else {
			alertNextPlayer();
		}
	}

	/**
	 * Sets up help menu view by loading XML layout
	 */
	public void helpMenu() {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		setContentView(R.layout.help);
	}

	/**
	 * Sets up settings menu by loading xml layout and adding ontouch methods
	 */
	public void settingsMenu() {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		setContentView(R.layout.settings);
		
		diffBar = (SeekBar) findViewById(R.id.difBar);
		numStonesBar = (SeekBar) findViewById(R.id.numStonesBar2);
		numStonesValue = (TextView) findViewById(R.id.numStonesValue);
		numHeaps = (RadioGroup) findViewById(R.id.numHeaps);
		multiHeap = (CheckBox) findViewById(R.id.checkBox1);
		//texturedStonesBox = (CheckBox) findViewById(R.id.CheckBox01);

		numStonesBar.setMax(maxPileSize - minPileSize);
		numStonesBar.setProgress(maxStones - minPileSize);

		numStonesValue.setText(""+maxStones);

		multiHeap.setChecked(multiHeapMode);
		//texturedStonesBox.setChecked(texturedStones);

		diffBar.setMax(20);
		diffBar.setProgress(difficulty);

		numHeaps.check(heapId);
		aidiff = (TextView) findViewById(R.id.aidiff);		
		aidiff.setText("Difficulty: " + skillLevel(false, true) + " " + difficulty);

		multiHeap.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				multiHeapMode = ! multiHeapMode;
			}
		});

//		texturedStonesBox.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View arg0) {
//				texturedStones = ! texturedStones;
//			}
//		});

		numStonesBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				numStonesBar.setProgress(progress);
				numStonesValue.setText(""+(numStonesBar.getProgress() + minPileSize));
				maxStones = numStonesBar.getProgress() + minPileSize;
			}
		});

		numHeaps.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup arg0, int selected) {
				switch(selected) {
				case R.id.heap1:
					numPiles = 1;
					break;
				case R.id.heap2:
					numPiles = 2;
					break;
				default:
					numPiles = 3;
				}
				heapId = selected;
			}
		});

		diffBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				diffBar.setProgress(progress);				
				difficulty = progress;
				aidiff.setText("Difficulty: " + skillLevel(false,true));
			}
		});
	}

	/**
	 * Sets up main menu by loading xml layout and setting ontouch methods
	 */
	public void mainMenu() {
		inGame = false;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		setContentView(R.layout.main);
		gameView = new TableView(this);

		View player1 = findViewById(R.id.play1);
		View player2 = findViewById(R.id.play2);
		View stats = findViewById(R.id.stats);
		View settings = findViewById(R.id.settings);
		View help = findViewById(R.id.help);
		player1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				gameMode = GameState.Mode.ONEPLAYER;
				gameView();
			}
		});
		player2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				gameMode = GameState.Mode.TWOPLAYER;
				gameView();
			}
		});
		stats.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				statsMenu();
			}
		});
		settings.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				settingsMenu();
			}
		}); 
		help.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				helpMenu();        
			}
		});

	}
	
	public void resetStats() {
		currentSkill = 0;
		currentSkill2 = 0;
		for (int i=0;i<wins.length;i++){
			wins[i] = 0;
			losses[i] = 0;
			wins2[i] = 0;
			losses2[i] = 0;
		}
	}

	/**
	 * Sets up stats menu by loading xml and setting ontouch methods
	 */
	public void statsMenu(){

		setContentView(R.layout.stats);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		ProgressBar skillBar = (ProgressBar) findViewById(R.id.skillbar);

		View switchGameType = findViewById(R.id.switchtype);
		
		View resetButton = findViewById(R.id.reset);
		
		skillBar.setMax(20);
		
		if (multiHeapStats) {
			((TextView)switchGameType).setText("Traditional");
			skillBar.setProgress((int)currentSkill2); }
		else {
			((TextView)switchGameType).setText("Multi-Heap ");
			skillBar.setProgress((int)currentSkill); }

		switchGameType.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				multiHeapStats = ! multiHeapStats;
				statsMenu();
			}
		});

		resetButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				alertResetStats();
				statsMenu();       
			}
		});

		TextView a1 = (TextView) findViewById(R.id.a1);
		TextView a2 = (TextView) findViewById(R.id.a2);
		TextView a3 = (TextView) findViewById(R.id.a3);

		TextView b1 = (TextView) findViewById(R.id.b1);
		TextView b2 = (TextView) findViewById(R.id.b2);
		TextView b3 = (TextView) findViewById(R.id.b3);

		TextView c1 = (TextView) findViewById(R.id.c1);
		TextView c2 = (TextView) findViewById(R.id.c2);
		TextView c3 = (TextView) findViewById(R.id.c3);

		TextView d1 = (TextView) findViewById(R.id.d1);
		TextView d2 = (TextView) findViewById(R.id.d2);
		TextView d3 = (TextView) findViewById(R.id.d3);

		TextView e1 = (TextView) findViewById(R.id.e1);
		TextView e2 = (TextView) findViewById(R.id.e2);
		TextView e3 = (TextView) findViewById(R.id.e3);

		TextView f1 = (TextView) findViewById(R.id.f1);
		TextView f2 = (TextView) findViewById(R.id.f2);
		TextView f3 = (TextView) findViewById(R.id.f3);

		TextView skillLevel = (TextView) findViewById(R.id.skillLevel);

		skillLevel.setText("Skill Level: " + skillLevel(true, false));

		//Below stuff sets up table of statistics depending upon the game type selected
		if (! multiHeapStats){
			a1.setText("" + wins[0]);
			a2.setText("" + losses[0]);
			if (losses[0]+wins[0] == 0)
				a3.setText("-");
			else
				a3.setText(((int)wins[0]*100/(wins[0]+losses[0])) + "%");

			b1.setText("" + wins[1]);
			b2.setText("" + losses[1]);
			if (losses[1]+wins[1] == 0)
				b3.setText("-");
			else
				b3.setText((wins[1]*100/(wins[1]+losses[1])) + "%");

			c1.setText("" + wins[2]);
			c2.setText("" + losses[2]);
			if (losses[2]+wins[2] == 0)
				c3.setText("-");
			else
				c3.setText((wins[2]*100/(wins[2]+losses[2])) + "%");

			d1.setText("" + wins[3]);
			d2.setText("" + losses[3]);
			if (losses[3]+wins[3] == 0)
				d3.setText("-");
			else
				d3.setText((wins[3]*100/(wins[3]+losses[3])) + "%");

			e1.setText("" + wins[4]);
			e2.setText("" + losses[4]);
			if (losses[4]+wins[4] == 0)
				e3.setText("-");
			else
				e3.setText((wins[4]*100/(wins[4]+losses[4])) + "%");

			int totalWins = wins[0] + wins[1] + wins[2] + wins[3] + wins[4];
			int totalLosses = losses[0] + losses[1] + losses[2] + losses[3] + losses[4];
			f1.setText("" + (totalWins));
			f2.setText(""+ (totalLosses));
			if (totalLosses + totalWins == 0)
				f3.setText("-");
			else
				f3.setText("" + ((totalWins * 100) / (totalLosses + totalWins)) + "%");
		}
		else
		{
			a1.setText("" + wins2[0]);
			a2.setText("" + losses2[0]);
			if (losses2[0]+wins2[0] == 0)
				a3.setText("-");
			else
				a3.setText(((int)wins2[0]*100/(wins2[0]+losses2[0])) + "%");

			b1.setText("" + wins2[1]);
			b2.setText("" + losses2[1]);
			if (losses2[1]+wins2[1] == 0)
				b3.setText("-");
			else
				b3.setText((wins2[1]*100/(wins2[1]+losses2[1])) + "%");

			c1.setText("" + wins2[2]);
			c2.setText("" + losses2[2]);
			if (losses2[2]+wins2[2] == 0)
				c3.setText("-");
			else
				c3.setText((wins2[2]*100/(wins2[2]+losses2[2])) + "%");

			d1.setText("" + wins2[3]);
			d2.setText("" + losses2[3]);
			if (losses2[3]+wins2[3] == 0)
				d3.setText("-");
			else
				d3.setText((wins2[3]*100/(wins2[3]+losses2[3])) + "%");

			e1.setText("" + wins2[4]);
			e2.setText("" + losses2[4]);
			if (losses2[4]+wins2[4] == 0)
				e3.setText("-");
			else
				e3.setText((wins2[4]*100/(wins2[4]+losses2[4])) + "%");

			int totalWins = wins2[0] + wins2[1] + wins2[2] + wins2[3] + wins2[4];
			int totalLosses = losses2[0] + losses2[1] + losses2[2] + losses2[3] + losses2[4];
			f1.setText("" + (totalWins));
			f2.setText(""+ (totalLosses));
			if (totalLosses + totalWins == 0)
				f3.setText("-");
			else
				f3.setText("" + ((totalWins * 100) / (totalLosses + totalWins)) + "%");
		}
	}

	/**
	 * Takes number representation of player's level and returns string representation
	 * @param yourLevel
	 * @return
	 */
	public String skillLevel(boolean yourLevel, boolean inGame){
		int level = 0;
		if (yourLevel)
			level = (int) getSkillLevel(inGame);
		else
			level = difficulty;
		if (level <= 4)
			return "Beginner";
		else if(level <= 8)
			return "Intermediate";
		else if(level <= 13)
			return "Advanced";
		else if(level < 20)
			return "Expert";
		else
			return "Master";
	}
}


