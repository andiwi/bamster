package wien.kollektiv.bamster.Game.Util;

import wien.kollektiv.bamster.Game.GameLoopThread;
import wien.kollektiv.bamster.Game.Paths.ManagedPath;

import android.view.MotionEvent;

/**
 * Thread wird gestartet, wenn Bildschirm berührt wird und gestoppt, wenn Finger den Bildschirm nicht mehr berührt<br>
 * und fügt Koordinaten zur aktuellen Linie hinzu
 * 
 * @author Marlon, Andreas
 *
 */
public class OnHoldThread extends Thread implements Runnable {
	
	private ManagedPath path;
	private boolean running;
	private MotionEvent event;
	private int mCounter;
    private GameLoopThread mGameLoopThread;
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param path
	 */
	public OnHoldThread(ManagedPath path, GameLoopThread gameLoopThread) {
		this.path = path;
        mGameLoopThread = gameLoopThread;
		this.running = false;
		mCounter = 0;
	}
	
	/**
	 * wird bei der ersten Toucheingabe ausgefuehrt
	 * speichert den ersten Punkt der Linie in den Path
	 */
	@Override
	public void start(){
		super.start();
		path.moveTo(event.getX(), event.getY());
	}

	@Override
	public void run() {
		while (running) {
            int mSpeed = mGameLoopThread.getSpeed();
            int counterLimit = (mGameLoopThread.getSpeed() / 10) + 3; //abhängig von der Geschwindigkeit werden unterschiedlich viele Events zum zeichnen verwendet.

            if(mCounter >= counterLimit) {
                mCounter = 0;
                path.lineTo(event.getX(), event.getY());
            }else {
                mCounter++;
            }

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
		}
	}
	
	/**
	 * Stoppt den Thread
	 */
	public void stopThread() {
		this.setRunning(false);
		boolean retry = true;
		while (retry) {
			try {
				this.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
	}
	
	/**
	 * Setzt den Thread aktiv
	 * 
	 * @param running
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Setzt ein MotionEvent, welche die Koordinaten der Fingerberühung am Bildschirm enthält
	 * 
	 * @param event
	 */
	public void setEvent(MotionEvent event) {
		this.event = event;
	}
	
}
