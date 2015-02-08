package wien.kollektiv.bamster.Persistenz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Stellt Verbindung zu DB zur Verfügung
 * Code Großteils von der LVA Multimedia der TU Wien, 2014/15 zur Verfügung gestellt
 * 
 * @author Marlon
 *
 */
public class HighscoreOpenHelper extends SQLiteOpenHelper {
	//private static final String TAG = HighscoreOpenHelper.class.getSimpleName();
	
	private static final int 	DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "highscore.db";
	
	public HighscoreOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// If the database is not created, or should be updated to a newer version
		// Create the highscore table
		HighscoreTable.onCreate(db);
		// TODO: Create other tables here
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Upgrade all tables
	    HighscoreTable.onUpgrade(db, oldVersion, newVersion);
	    // TODO: Code for other tables goes here
	}

}