package wien.kollektiv.bamster.Game;

import wien.kollektiv.bamster.R;
import wien.kollektiv.bamster.Game.Hamster.Hamster;
import wien.kollektiv.bamster.Game.Obstacles.ObstacleManager;
import wien.kollektiv.bamster.Game.Paths.ManagedPath;
import wien.kollektiv.bamster.Game.Paths.PathManager;
import wien.kollektiv.bamster.Game.Util.OnHoldThread;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Steuert die Geschwindigkeit des Spiels auf unterschiedlichen Geräten (Frameunabhängig)
 * @author Andreas
 * @author Marlon
 */
public class GameLoopThread extends Thread {

	/*
     * Difficulty setting constants
     */
    public static final int DIFFICULTY_EASY = 1;
    public static final int DIFFICULTY_MEDIUM = 2;
    public static final int DIFFICULTY_HARD = 3;
    
    /*
     * Speed setting constants
     */
    public static final int SPEED_SLOW = 30;
    public static final int SPEED_MEDIUM = 40;
    public static final int SPEED_FAST = 50;
    public static final int SPEED_MAXIMUM = 70;
    
    public static final int GRAVITY = 120; //(GRAVITY + 50 - SPEED_MAXIMUM) >= 100 muss erfüllt sein 
    
    /*
     * State-tracking constants
     */
    public static final int STATE_LOSE = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_READY = 3;
    public static final int STATE_RUNNING = 4;
    public static final int STATE_WIN = 5;
    
    /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
    private int mState;
    
    
    
    private SurfaceHolder mSurfaceHolder;
    private Context mContext;
    
    private long mScore;
    private int mSpeed = SPEED_MEDIUM;
    private ObstacleManager mObstacleManager;
	private Hamster mHamster;
	private PathManager mPathManager;
	private OnHoldThread mOnHoldThread;
	
	/**
     * Current difficulty
     */
    private int mDifficulty = DIFFICULTY_MEDIUM;
	
	/** Indicate whether the surface has been created & is ready to draw */
	private boolean mRun = false;
	private final Object mRunLock = new Object();
	
	private static final long FPS = 33; // ~30 FPS
	
	private int mHamsterLifes;
	private int drawLostLifeAnimation = 0;

	/**
	 * initialisiert das Spiel
	 */
	public GameLoopThread(SurfaceHolder holder, Context context) {
		mSurfaceHolder = holder;
		mContext = context;
		
		//get difficulty
		GameActivity activity = (GameActivity) mContext;
		mDifficulty = activity.getDifficulty();
		if(mDifficulty == DIFFICULTY_EASY) {
			mSpeed = SPEED_SLOW;
		} else if(mDifficulty == DIFFICULTY_MEDIUM) {
			mSpeed = SPEED_MEDIUM;
		} else if(mDifficulty == DIFFICULTY_HARD) {
			mSpeed = SPEED_FAST;
		}
		
		// initialisation of game
		mObstacleManager = new ObstacleManager(context, mDifficulty);
		mPathManager = new PathManager(context);
		mHamster = new Hamster(context, mSpeed, BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.hamster_sprite_klein));
		
		mHamsterLifes = mHamster.getLifes();
	}

	/**
	 * Gameloop in run() garantiert Frameunabhängigkeit und führt ruft Game-Update und zeichnen der Elemente auf
	 */
	@SuppressLint("WrongCall")
	@Override
	public void run() {
		long startTime, sleepTime;
		while (mRun) {
			Canvas canvas = null;
			startTime = System.currentTimeMillis();
			if(mState == STATE_RUNNING) {
				try {
					
					canvas = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						updateGame();
						
						if(mSpeed == 0)
					    	((GameActivity)mContext).finishGameThread(mScore);
						
						// Critical section. Do not allow mRun to be set false until
		                // we are sure all canvas draw operations are complete.
		                //
		                // If mRun has been toggled false, inhibit canvas operations.
		                synchronized (mRunLock) {
		                    if (mRun) {
		                    	doDraw(canvas);
		                    }
		                }
					}
				} finally {
					if (canvas != null)
						mSurfaceHolder.unlockCanvasAndPost(canvas);
 				}
			}
			sleepTime = FPS - (System.currentTimeMillis() - startTime);
			try {
				if (sleepTime > 0)
					sleep(sleepTime);
				else
					sleep(0);

			} catch (Exception e) {}
		}
	}

	/**
	 * Setzt den canvas im spiel in jedem frame weiß und zeichnet elemente am spieldisplay<br>
	 * ruft außerdem draw metohden von der obstacles, der linien und des hamsters auf.<br>
	 * Zeichnet Hintergrund bei Kollision mit Hindernis rot.
	 * 
	 * @param canvas
	 */
	private void doDraw(Canvas canvas) {
		
		if(mHamsterLifes > mHamster.getLifes()){
			drawLostLifeAnimation = 1;
			mHamsterLifes = mHamster.getLifes();
		}
		canvas.drawColor(Color.WHITE);
		if(drawLostLifeAnimation != 0){
			canvas.drawColor(Color.argb(drawLostLifeAnimation, 255, 0, 0));
			drawLostLifeAnimation += 25;
			if(drawLostLifeAnimation >= 255)
				drawLostLifeAnimation = 0;
		}
			
		//canvas.drawColor(Color.TRANSPARENT);
		/*
		Bitmap background = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.backround);
		
		int countWidth = (canvas.getWidth() + background.getWidth() - 1) / background.getWidth();
		int countHeight = (canvas.getWidth() + background.getWidth() - 1) / background.getWidth();  
		Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), background.getHeight(), Config.ARGB_8888);  
		
		for(int y = 0; y < countHeight; y += background.getHeight()) {
			for(int x = 0; x < countWidth; ++x){  
			  canvas.drawBitmap(background, x * background.getWidth(), y, null);  
			}
		}
		
		BitmapDrawable backgroundDrawable = (BitmapDrawable)mContext.getResources().getDrawable(R.drawable.background);
		backgroundDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		backgroundDrawable.draw(canvas);
		*/
		
		mObstacleManager.draw(canvas);		
		mHamster.draw(canvas);
		mPathManager.draw(canvas);
		
		final LinearLayout lifeWrapper = (LinearLayout) ((Activity)mContext).getWindow().getDecorView().findViewById(R.id.lifeWrapper);
		if(mHamster.getLifes() == 2) {
			final ImageView life1View = (ImageView) lifeWrapper.findViewById(R.id.life1);
			Activity activity = (Activity) mContext;
			activity.runOnUiThread(new Runnable() {
				public void run() {
					life1View.setVisibility(View.GONE);
				}
			});
		} else if(mHamster.getLifes() == 1) {
			final ImageView life2View = (ImageView) lifeWrapper.findViewById(R.id.life2);
			Activity activity = (Activity) mContext;
			activity.runOnUiThread(new Runnable() {
				public void run() {
					life2View.setVisibility(View.GONE);
				}
			});
			
		} else if(mHamster.getLifes() == 0) {
			final ImageView life3View = (ImageView) lifeWrapper.findViewById(R.id.life3);
			Activity activity = (Activity) mContext;
			activity.runOnUiThread(new Runnable() {
				public void run() {
					life3View.setVisibility(View.GONE);
				}
			});
		}

		// set score to TextView
		final TextView gameScoreTextView = (TextView) ((Activity)mContext).getWindow().getDecorView().findViewById(R.id.gameScoreTextView);
		Activity activity = (Activity) mContext;
		activity.runOnUiThread(new Runnable() {
			public void run() {
				gameScoreTextView.setText(mScore + "");
			}
		});


        //FIXME just for debugging
        final int counterLimit = (mSpeed / 10) + 3;
        // set speed to TextView
        final TextView helperTextView = (TextView) ((Activity)mContext).getWindow().getDecorView().findViewById(R.id.helperTextView);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                helperTextView.setText("speed: " + mSpeed + " - counterLimit: " + counterLimit);
            }
        });
	}
	
	/*public static Bitmap createBackgroundRepeater(int width, Bitmap src){  
		int count = (width + src.getWidth() - 1) / src.getWidth();  
		Bitmap bitmap = Bitmap.createBitmap(width, src.getHeight(), Config.ARGB_8888);  
		Canvas canvas = new Canvas(bitmap);  
		  
		for(int idx = 0; idx < count; ++ idx){  
		  canvas.drawBitmap(src, idx * src.getWidth(), 0, null);  
		}  
		  
		return bitmap;  
	}*/ 

	/**
	 * Aktualisiert die Positionen aller Objekte im Spiel.
	 */
	private void updateGame() {
		mScore+= mDifficulty;
		mObstacleManager.updateObstacles(mSpeed, mScore);
		mPathManager.updatePaths(mSpeed);
		mSpeed = mHamster.update(mSpeed, mPathManager, mObstacleManager);
	}
	
	/**
	 * startet den onHoldThread bei ACTION_DOWN
	 * ACTION_UP beendet den onHoldThread
	 * @param e
	 * @return
	 */
	public boolean doOnTouchEvent(MotionEvent e) {
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			ManagedPath mPath = new ManagedPath();
			mPathManager.addPath(mPath);
			mOnHoldThread = new OnHoldThread(mPath, this);
			mOnHoldThread.setRunning(true);
			mOnHoldThread.setEvent(e);
			mOnHoldThread.start();
			break;
		case MotionEvent.ACTION_UP:
			mOnHoldThread.setRunning(false);
			break;
		default:
			return false;
		}
		return true;	
	}

	/**
	 * set state of the game, can be READY, RUNNING, PAUSE, LOSE, or WIN
	 * @param state
	 */
	public void setState(int state) {
		mState = state;
	}

	/**
	 * starts the game
	 */
	public void startGame() {
		synchronized (mSurfaceHolder) {
			setState(STATE_RUNNING);
		}
	}

	/**
	 * pause the game
	 */
	public void pauseGame() {
		synchronized (mSurfaceHolder) {
            if (mState == STATE_RUNNING)
            	setState(STATE_PAUSE);
        }
	}

	/**
	 * resume the game
	 */
	public void resumeGame() {
		synchronized (mSurfaceHolder) {
            if (mState == STATE_PAUSE)
            	setState(STATE_RUNNING);
        }
	}
	
	public void restoreState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return true if game is on pause
	 */
	public boolean isOnPause(){
		if(mState == STATE_PAUSE) return true;
		return false;
	}
	
	/**
     * Used to signal the thread whether it should be running or not.
     * Passing true allows the thread to run; passing false will shut it
     * down if it's already running. Calling start() after this was most
     * recently called with false will result in an immediate shutdown.
     *
     * @param run true to run, false to shut down
     */
    public void setRunning(boolean run) {
        // Do not allow mRun to be modified while any canvas operations
        // are potentially in-flight. See doDraw().
        synchronized (mRunLock) {
            mRun = run;
        }
    }

	/**
	 * returns the surface size
	 * @param width
	 * @param height
	 */
	public void setSurfaceSize(int width, int height) {
		// TODO Auto-generated method stub
	}

    public int getSpeed() {
        return mSpeed;
    }
}
