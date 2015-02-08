package wien.kollektiv.bamster.Game.Util;

import wien.kollektiv.bamster.Game.Paths.ManagedPath;
import android.graphics.Point;
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
	private Point mLastPoint;
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param path
	 */
	public OnHoldThread(ManagedPath path) {
		this.path = path;
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
		mLastPoint = new Point((int) event.getX(), (int) event.getY());
		mCounter++;
	}
	
	/**
	 * jedes 5te Touchevent wird abgefragt und der Punkt in Path abgespeichert
	 * Die Punkte dazwischen werden mittels quadTo berechnet
	 */
	@Override
	public void run() {
		while (running) {
			//path.lineTo(event.getX(), event.getY());
			
			if(mCounter == 5) {
				mCounter = 0;
				path.quadTo(mLastPoint.x, mLastPoint.y , event.getX(), event.getY());
				mLastPoint = new Point((int) event.getX(), (int)event.getY());
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
