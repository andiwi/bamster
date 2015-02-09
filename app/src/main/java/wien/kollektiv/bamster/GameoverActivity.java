package wien.kollektiv.bamster;

import wien.kollektiv.bamster.Game.GameActivity;
import wien.kollektiv.bamster.Game.GameLoopThread;
import wien.kollektiv.bamster.Persistenz.HighscoreContentProvider;
import wien.kollektiv.bamster.Persistenz.HighscoreTable;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
// imports of the support library
import android.widget.TextView;

/**
 * Activity, wird gestartet, wenn entweder alle Leben verloren wurden oder der User zu langsam war
 * 
 * @author Marlon
 *
 */
public class GameoverActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private CursorLoader cursorLoader;
	private long mScore;
	private ImageButton mSaveScore;
	private EditText mUserName;
	private Context context;
	private int mDifficulty;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.context = this;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null){
			mScore = extras.getLong("mScore");
			mDifficulty = extras.getInt("mDifficulty");
		}
		
		// Initialize to load the data in a background task
	    getSupportLoaderManager().initLoader(0, null, this);
		
		Log.d("GameoverActivity", "WELCOME");
		
		setContentView(R.layout.activity_gameover);
		
		mSaveScore = (ImageButton)findViewById(R.id.setHighscore1);
		mUserName = (EditText)findViewById(R.id.userName);
		((TextView)findViewById(R.id.scoreText)).setText("Score: "+mScore);

		mSaveScore.setVisibility(View.INVISIBLE);
		mUserName.setVisibility(View.INVISIBLE);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = { HighscoreTable.COLUMN_ID, HighscoreTable.COLUMN_NAME, HighscoreTable.COLUMN_SCORE };
	    cursorLoader = new CursorLoader(this,
	        HighscoreContentProvider.CONTENT_URI, projection, null, null, HighscoreTable.COLUMN_SCORE + " DESC");
	    
	    return cursorLoader;
	}

	/**
	* determine if user reached at least third place and calls letUserSaveNewImput(..) if so
	*/
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		
		Log.d("GameoverActivity", "ON LOAD FINISHED");

		long first = 0;
		long second = 0;
		long third = 0;
		
		int i = 0;
		
		if (data.moveToFirst()){
			do{
				if(i == 0)
					first = Long.parseLong(data.getString(data.getColumnIndex(HighscoreTable.COLUMN_SCORE)));
				else if(i == 1)
					second = Long.parseLong(data.getString(data.getColumnIndex(HighscoreTable.COLUMN_SCORE)));
				else if(i == 2)
					third = Long.parseLong(data.getString(data.getColumnIndex(HighscoreTable.COLUMN_SCORE)));
				i++;
			}while(data.moveToNext());
		}
		
		int place = -1;
		if(mScore > third){
			if(mScore > third && mScore < second)
				place = 3;
			else if(mScore > second && mScore < first)
				place = 2;
			else if(mScore > first)
				place = 1;
		}
		
	    startAnimation(data, place);
	   
	}
	
	/**
	 * Gets called when user reached at least third place<br>
	 * and lets him save his score.
	 * 
	 * @param data cursor to current set of data in db
	 */
	private void letUserSaveNewInput(final Cursor data, int place){
		
		mSaveScore.setVisibility(View.VISIBLE);
		mUserName.setVisibility(View.VISIBLE);
		
		LinearLayout userInputLayout = (LinearLayout) findViewById(R.id.userInputLayout);
		AnimatorSet ani = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.let_userinput_appear);
        ani.setTarget(userInputLayout);
        ani.start();
        
		mSaveScore.setOnClickListener(
	    	new View.OnClickListener(){
	    		public void onClick(View view){
	    			
	    			if(data.getCount() >= 3 && data.moveToLast()){
	    	    		Uri uri = Uri.parse(HighscoreContentProvider.CONTENT_URI+"/"+data.getInt(data.getColumnIndex(HighscoreTable.COLUMN_ID)));
	    	    		getContentResolver().delete(uri, "", null);
	    	    	}
	    			
	    			// Generate values readable by the content provider
	    	    	ContentValues values = new ContentValues();
	    	    	values.put(HighscoreTable.COLUMN_NAME, mUserName.getText().toString());
	    	    	values.put(HighscoreTable.COLUMN_SCORE, mScore);
	    	    	
	    	    	// save the new score in the database
	    	    	getContentResolver().insert(HighscoreContentProvider.CONTENT_URI, values);
	    	    	
	    	    	Intent intent = new Intent(context, MenuActivity.class);
	    	    	startActivity(intent);
	    	    	
	    		}
	    	}
	    );
		
	}
	
	/**
	 * Startet die Animationen der Pokale und lässt nach abschluss der Animationen den User den Highscore eintragen, wenn er 1., 2. oder 3. ist
	 * 
	 * @param data
	 * @param place
	 */
	public void startAnimation(final Cursor data, int place) {
		
		if(place != 1){
	        removeTrophy(R.id.goldTrophy);
			showCrossedTrophpy(R.id.goldTrophyCross);
		}else
	       letTrohpyDance(R.id.goldTrophy);
		
		if(place != 2){
	        removeTrophy(R.id.silverTrophy);
			showCrossedTrophpy(R.id.silverTrophyCross);
		}else
			letTrohpyDance(R.id.silverTrophy);
		
		if(place != 3){
			removeTrophy(R.id.bronzeTrophy);
			showCrossedTrophpy(R.id.bronzeTrophyCross);
		}else
			letTrohpyDance(R.id.bronzeTrophy);
		
		if(place <= 3 && place != -1)		
			letUserSaveNewInput(data, place);
    }
	
	/**
	 * Startet Animation um einen Pokal zu entfernen
	 * 
	 * @param trophyId
	 */
	public void removeTrophy(int trophyId){
		ImageView trophy = (ImageView) findViewById(trophyId);
		AnimatorSet ani = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.remove_trophy_animation);
        ani.setTarget(trophy);
        ani.start();
	}
	
	/**
	 * Startet Animation um einen Pokal tanzen zu lassen<br>
	 * zum Beispiel gold Pokal tanzen lassen, wenn man erster geworden ist
	 *  
	 * @param trophyId
	 */
	public void letTrohpyDance(int trophyId){
		ImageView trophy = (ImageView) findViewById(trophyId);
		AnimatorSet ani = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.let_trophy_dance_animation);
        ani.setTarget(trophy);
        ani.start();
	}
	
	/**
	 * zeigt durchgestrichene Pokale<br>
	 * zum Beispiel wenn man den goldenen Pokal nicht erreicht
	 * 
	 * @param trophyId
	 */
	public void showCrossedTrophpy(int trophyId){
		ImageView trophy = (ImageView) findViewById(trophyId);
		AnimatorSet ani = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.show_trophy_animation);
        ani.setTarget(trophy);
        ani.start();
	}
	
	/**
	 * startet ein neues Spiel mit dem selben Schwierigkeitsgrad wie das letzte
	 * 
	 * @param v
	 */
	public void retry(View v){
		Intent intent = new Intent(this, GameActivity.class);
		switch (mDifficulty) {
	    case (GameLoopThread.DIFFICULTY_EASY):
	    	intent.putExtra("difficulty", GameLoopThread.DIFFICULTY_EASY);
	    	break;
	    case (GameLoopThread.DIFFICULTY_MEDIUM):
	    	intent.putExtra("difficulty", GameLoopThread.DIFFICULTY_MEDIUM);
	    	break;
	    case (GameLoopThread.DIFFICULTY_HARD):
	    	intent.putExtra("difficulty", GameLoopThread.DIFFICULTY_HARD);
	   		break;
	    }
		
		startActivity(intent);
		finish();
	}
	
	@Override
	public void onBackPressed(){
	     goToMenu();
	}
	
	/**
	 * wechselt zur menü activity
	 */
	public void goToMenu(){
		Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
	}
	
	/**
	 * ruft methode zum wechseln zu menü activity aus<br>
	 * wird zum Beispiel aus xml aufgerufen
	 * 
	 * @param v
	 */
	public void menuBtnClicked(View v){
		goToMenu();
	}
	

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {}
	
}