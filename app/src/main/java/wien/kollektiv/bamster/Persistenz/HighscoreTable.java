package wien.kollektiv.bamster.Persistenz;
import android.database.sqlite.SQLiteDatabase;

import android.util.Log;
/**
 * Repräsentiert eine Highscoretabelle
 * Code Großteils von der LVA Multimedia der TU Wien, 2014/15 zur Verfügung gestellt
 * 
 * @author Marlon
 *
 */
public class HighscoreTable {
	private static final String TAG = HighscoreTable.class.getSimpleName();
	
    // Define the table
	public static final String HIGHSCORE_TABLE_NAME = "highscore";
    public static final String COLUMN_ID = "_id"; // id name by convention
    public static final String COLUMN_NAME = "name";
    public static final String	COLUMN_SCORE = "score";
    
    // Define the create command
    private static final String HIGHSCORE_TABLE_CREATE =
                "CREATE TABLE " + HIGHSCORE_TABLE_NAME + " (" +
                COLUMN_ID + " integer primary key autoincrement, " +
                COLUMN_NAME + " TEXT not null, " +
                COLUMN_SCORE + " integer);";

	public static void onCreate(SQLiteDatabase db) {
		// If the database is not created, or should be updated to a newer version
		// Create the highscore table
		db.execSQL(HIGHSCORE_TABLE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                + newVersion + ", which will destroy all old data");
        
	    // Delete the Table if it already exists
	    db.execSQL("DROP TABLE IF EXISTS " + HIGHSCORE_TABLE_NAME);
	    // Create the new table
        onCreate(db);
	}
}