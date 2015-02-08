package wien.kollektiv.bamster.Game;

import java.lang.Thread.State;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Die View fuer das Spiel.
 * Verwaltet touch eingaben, Zeichnet objekte im Spiel.
 * @author Andreas
 * @author Marlon
 */
@SuppressLint("WrongCall")
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

	private GameLoopThread mGameLoopThread;
	

	/**
	 * GameView Konstruktor erstellt obstacle/path Manager, gameLoopThread und hamster
	 * @param context
	 * @param attrs
	 */
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		mGameLoopThread = new GameLoopThread(holder, context);
		
		setFocusable(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return mGameLoopThread.doOnTouchEvent(e);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
		if(mGameLoopThread.getState() == State.NEW){
			mGameLoopThread.setRunning(true);
			mGameLoopThread.start();
		} else
			((GameActivity)this.getContext()).goToMenu();
	}

	/* Callback invoked when the surface dimensions change. */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO will not happen ?  
		// mGameLoopThread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		mGameLoopThread.setRunning(false);
		while (retry) {
			try {
				mGameLoopThread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again and again ..
			}
		}
	}

	/**
	 * @return the current gameLoopThread
	 */
	public GameLoopThread getGameLoopThread() {
		return mGameLoopThread;
	}

}
