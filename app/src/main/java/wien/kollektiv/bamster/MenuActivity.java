package wien.kollektiv.bamster;

import wien.kollektiv.bamster.Game.GameActivity;
import wien.kollektiv.bamster.Game.GameLoopThread;
import wien.kollektiv.bamster.Persistenz.HighscoreContentProvider;
import wien.kollektiv.bamster.Persistenz.HighscoreTable;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * Menu erweitert Funktionialität von Activity und lässt Game starten
 * @author Andreas
 * @author Marlon
 *
 */
public class MenuActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private ListView highscoreTable;
	private CursorLoader cursorLoader;
	private SimpleCursorAdapter dbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		
		// Get the needed views
		highscoreTable = (ListView) findViewById(R.id.highscoreTable);
		// Set a view to be shown if the list is empty => in our case it is just a text view
		highscoreTable.setEmptyView(findViewById(R.id.emptyHighscoreMessage));

	    // fetch data from the db and show it in the list
		showData();
		
	    // Register the context menu
	    registerForContextMenu(highscoreTable);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * wechselt Activity zur Game Activity
	 * @param v
	 */
	public void startGame(View v) {
		Intent intent = new Intent(this, GameActivity.class);
		switch (v.getId()) {
	    case (R.id.btn_play_easy):
	    	intent.putExtra("difficulty", GameLoopThread.DIFFICULTY_EASY);
	    	break;
	    case (R.id.btn_play_medium):
	    	intent.putExtra("difficulty", GameLoopThread.DIFFICULTY_MEDIUM);
	    	break;
	    case (R.id.btn_play_hard):
	    	intent.putExtra("difficulty", GameLoopThread.DIFFICULTY_HARD);
	   		break;
	    }
		
		startActivity(intent);
	}
	
	/**
	 * gets data from db and displays it
	 */
	public void showData() {		
		// Initialize to load the data in a background task
	    getSupportLoaderManager().initLoader(0, null, this);
	    		
		// Table columns from where we want to get the info
	    String[] from = new String[] { HighscoreTable.COLUMN_NAME, HighscoreTable.COLUMN_SCORE };
	    // Labels of the TextViews to which we want to map
	    int[] to = new int[] { R.id.lblName, R.id.lblScore };
	    	    
	    dbAdapter = new SimpleCursorAdapter(this, R.layout.highscoreentry_layout, null, from, to, 0);
	    
	    highscoreTable.setAdapter(dbAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = { HighscoreTable.COLUMN_ID, HighscoreTable.COLUMN_NAME, HighscoreTable.COLUMN_SCORE };
	    cursorLoader = new CursorLoader(this,
	        HighscoreContentProvider.CONTENT_URI, projection, null, null, HighscoreTable.COLUMN_SCORE + " DESC");
	    
	    return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Show the newly created data
		dbAdapter.swapCursor(data);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		dbAdapter.swapCursor(null);
	}
	
	@Override
	public void onBackPressed(){
	     //do nothing
		finish();
	}
}
