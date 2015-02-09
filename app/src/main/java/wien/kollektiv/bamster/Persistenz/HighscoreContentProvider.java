package wien.kollektiv.bamster.Persistenz;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Stellt CRUD Methoden für DB zur Verfügung
 * Code Großteils von der LVA Multimedia der TU Wien, 2014/15 zur Verfügung gestellt
 * 
 * @author Marlon
 *
 */
public class HighscoreContentProvider extends ContentProvider {
	// Database helper
	HighscoreOpenHelper dbHelper;
	
	// Needed for the URI matcher
	private static final int HIGHSCORE = 1;	   // fetch content from this table
	private static final int HIGHSCORE_ID = 2; // to fetch a single row from the table

	private static final String AUTHORITY = "wien.kollektiv.bamster.highscoreprovider";
	// The name of the table to be accessed
	private static final String BASE_PATH = "highscore"; 
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, BASE_PATH, HIGHSCORE);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", HIGHSCORE_ID);
	}
	
	@Override
	public boolean onCreate() {
		dbHelper = new HighscoreOpenHelper(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		// Uri ... must be matched to find out which table to access
		// projection ... gives you the columns to fetch for each row
		// selection  ... Either null, or the word the user entered
		// selectionArgs ... Either empty, or the string the user entered
		// sortOrder ... the sort order for the returned rows (ASC, DESC)

		// Using SQLiteQueryBuilder instead of query() method
	    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    // check if the caller has requested a column which does not exists
	    checkColumns(projection); // throws illegal argument exceptions
	    
	    // Set the table ... add switch table if more than one table can be selected
	    queryBuilder.setTables(HighscoreTable.HIGHSCORE_TABLE_NAME);
	    
	    int uriType = sUriMatcher.match(uri);
	    switch (uriType) {
		case HIGHSCORE: // do nothing => we just want to fetch all rows
			break;
		case HIGHSCORE_ID: // add row id to the query => The id is placed in the uri where '#' is placed in the uri matcher
			queryBuilder.appendWhere(HighscoreTable.COLUMN_ID + "=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

	    // open up the connection to the db
	    SQLiteDatabase db = dbHelper.getWritableDatabase();
	    
	    Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	    // make sure that potential listeners are getting notified
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    
		return cursor;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sUriMatcher.match(uri);
		
	    SQLiteDatabase db = dbHelper.getWritableDatabase();
	    
	    int rowsDeleted = 0;
	    switch (uriType) {
		    case HIGHSCORE:
		      rowsDeleted = db.delete(HighscoreTable.HIGHSCORE_TABLE_NAME, selection, selectionArgs);
		      break;
		    case HIGHSCORE_ID:
		    	String id = uri.getLastPathSegment();
		    	if (TextUtils.isEmpty(selection)) {
		    		rowsDeleted = db.delete(HighscoreTable.HIGHSCORE_TABLE_NAME, HighscoreTable.COLUMN_ID + "=" + id, null);
		    	} else {
		    		rowsDeleted = db.delete(HighscoreTable.HIGHSCORE_TABLE_NAME, HighscoreTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
		    	}
		    	break;
		    default:
		    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    
	    // Notify all listeners about the changed entry!
	    getContext().getContentResolver().notifyChange(uri, null);
	    // Return the number of deleted rows
	    return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		// not implemented yet ... you won't need it ; )
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sUriMatcher.match(uri);
	    
		SQLiteDatabase db = dbHelper.getWritableDatabase();
	    
		long id = 0;
	    switch (uriType) {
	    case HIGHSCORE:
	      id = db.insert(HighscoreTable.HIGHSCORE_TABLE_NAME, null, values);
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    
	    // Notify all listeners about the changed entry!
	    getContext().getContentResolver().notifyChange(uri, null);
	    
	    return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// not implemented => my model does not accept updates
		// see http://www.vogella.com/articles/AndroidSQLite/article.html if you want to see how it works
		return 0;
	}
	
	private void checkColumns(String[] projection) {
		String[] available = { HighscoreTable.COLUMN_ID, HighscoreTable.COLUMN_NAME, HighscoreTable.COLUMN_SCORE };
		
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			
			// check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection. I know only: " + available);
			}
		}
	}

}