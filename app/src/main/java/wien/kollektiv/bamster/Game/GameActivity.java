package wien.kollektiv.bamster.Game;

import java.io.IOException;

import wien.kollektiv.bamster.GameoverActivity;
import wien.kollektiv.bamster.MenuActivity;
import wien.kollektiv.bamster.R;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Game Activity erweitert Activity, startet die Game View und bietet "on"-Methoden
 * @author Andreas
 * @author Marlon
 *
 */
public class GameActivity extends Activity implements OnLoadCompleteListener{

	private GameView mGameView;
	private GameLoopThread mGameLoopThread;
	private int mDifficulty;
	private MediaPlayer mMediaPlayer;
	private SoundPool mSoundPool;
	private boolean mIsSoundOff;
	private int bumpSound;
	
	/**
	 * initialisiert GameView, SoundManager, MediaPlayer
	 * startet GameLoopThread
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//get difficulty from MenuActivity
		mDifficulty = getIntent().getExtras().getInt("difficulty");
		super.onCreate(savedInstanceState);

		 // tell system to use the layout defined in our XML file
		setContentView(R.layout.activity_game);
		
		// get handles to the gameView from XML, and its gameLoopThread
		View layout = this.findViewById(R.id.gameLayout);
		mGameView = (GameView) layout.findViewById(R.id.gameView);
		mGameLoopThread = mGameView.getGameLoopThread();
		
		// init mediaPlayer for background music
		mIsSoundOff = false;
		//MediaPlayer erzeugen
		mMediaPlayer = MediaPlayer.create(this,R.raw.zebraphone_loop);
		try {
			mMediaPlayer.prepare();
		} catch (IllegalStateException e) {
			Log.e("exception", e.getStackTrace().toString());
		} catch (IOException e) {
			Log.e("exception", e.getStackTrace().toString());
		}
		mMediaPlayer.setLooping(true);
		mMediaPlayer.start();
		
		// init soundPool
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		mSoundPool.setOnLoadCompleteListener(this);
		//mLoopSound = mSoundPool.load(this, R.raw.zebraphone_loop, 1);
		bumpSound = mSoundPool.load(this, R.raw.crash, 1);
		
		//init sound-mute-button as not clicked
		((ImageButton)findViewById(R.id.note_button_02)).setTag(Boolean.valueOf(false));
		
		if (savedInstanceState == null) {
            // we were just launched: set up a new game
            mGameLoopThread.setState(GameLoopThread.STATE_RUNNING);
            Log.w(this.getClass().getName(), "SIS is null");
        } else {
            // we are being restored: resume a previous game
        	mGameLoopThread.restoreState(savedInstanceState);
            Log.w(this.getClass().getName(), "SIS is nonnull");
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}
	
	/**
	 * pausiert das Spiel inklusive Musik
	 */
	@Override
	protected void onPause() {
		mGameLoopThread.pauseGame();
		mSoundPool.autoPause();
		mMediaPlayer.pause();
		super.onPause();
	}
	
	/**
	 * startet das Spiel wieder
	 */
	@Override
	protected void onResume() {
		mGameLoopThread.resumeGame();
		if(!mIsSoundOff) {
			mMediaPlayer.start();
			mSoundPool.autoResume();
		}
		super.onResume();
	}
	
	/**
	 * stoppt das Spiel
	 */
	@Override
	protected void onStop() {
		mGameLoopThread.setRunning(false);
    	mSoundPool.release();
    	mMediaPlayer.stop();
    	mMediaPlayer.release();
    	
    	super.onStop();
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
	 * tracked Button-Aktivität
	 * @param v aktuelle button-view
	 */
	public void pauseClicked(View v) {
		ImageButton button = (ImageButton) v;
		if(!mGameLoopThread.isOnPause()) {
			onPause();
			button.setImageResource(R.drawable.play_button_01);
			((ImageButton)findViewById(R.id.note_button_02)).setVisibility(View.INVISIBLE);
		}else {
			onResume();
			button.setImageResource(R.drawable.pause_button_04);
			((ImageButton)findViewById(R.id.note_button_02)).setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * wechselt musik an/aus
	 * 
	 * @param v
	 */
	public void noteClicked(View v){
		ImageButton button = (ImageButton) v;
		if(!(Boolean)button.getTag()) {
			mSoundPool.autoPause();
			mMediaPlayer.pause();
			mIsSoundOff = true;
			button.setImageResource(R.drawable.note_off_button_03);
			button.setTag(Boolean.valueOf(true));
		}else {
			mSoundPool.autoResume();
			mMediaPlayer.start();
			mIsSoundOff = false;
			button.setImageResource(R.drawable.note_button_02);
			button.setTag(Boolean.valueOf(false));
		}
	}
	
	/**
	 * zeigt/versteckt hilfedialog und pausiert/startet spiel
	 * 
	 * @param v
	 */
	public void helpClicked(View v){
		pauseClicked(findViewById(R.id.btn_pause));
		LinearLayout helpingTextOverlay = (LinearLayout)findViewById(R.id.helpingTextOverlay);
		if(mGameLoopThread.isOnPause()) {
			helpingTextOverlay.setVisibility(LinearLayout.VISIBLE);
		}else {
			helpingTextOverlay.setVisibility(LinearLayout.INVISIBLE);
		}
	}
	
	/**
	 * lässt das spiel mit einem score beenden
	 * 
	 * @param mScore
	 */
	public void finishGameThread(long mScore){
    	Intent intent = new Intent(this, GameoverActivity.class);
    	intent.putExtra("mScore", mScore);
    	intent.putExtra("mDifficulty", mDifficulty);
		startActivity(intent);
	}
	
	/**
	 * wechselt zur menü activity
	 */
	public void goToMenu(){
		Intent intent = new Intent(this, MenuActivity.class);
		startActivity(intent);
	}

	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
		mSoundPool = soundPool;
	}

	/**
	 * @return aktueller Schwierigkeitsgrad
	 */
	public int getDifficulty() {
		return mDifficulty;
	}
	
	/**
	 * spielt einen sound für Kollision mit Hindernis
	 */
	public void playCollisionSound(){
		if(!mIsSoundOff)
			mSoundPool.play(bumpSound, 5, 5, 2, 1, 1.0f);
	}
}
