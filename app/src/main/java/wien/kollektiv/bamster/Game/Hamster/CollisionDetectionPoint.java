package wien.kollektiv.bamster.Game.Hamster;

import java.util.concurrent.ConcurrentSkipListMap;

import android.graphics.Point;
/**
 * Diese Klasse erweitert die Klasse Point
 * Sie speichert zusätzlich einen Beschleunigungsfaktor ab.
 * @author Andreas
 *
 */
public class CollisionDetectionPoint extends Point {

	private int mAccelerationFactor;
	
	CollisionDetectionPoint(int accelerationFactor) {
		mAccelerationFactor = accelerationFactor;
	}

	/**
	 * 
	 * @return acceleration factor
	 */
	public int getAccelerationFactor() {
		return mAccelerationFactor;
	}

	/**
	 * berechnet die vertikale Distanz zwischen diesem Punkt und dem Punkt mit der selben x-Koordinate aus der Liste
	 * @param calcPoints key: xCoord, value: yCoord
	 * @return
	 */
	public int calcVerticalCollisionDistance(
			ConcurrentSkipListMap<Integer, Integer> calcPoints) {
		if(!calcPoints.containsKey(this.x))
			return Integer.MAX_VALUE;
		
		int yLine = calcPoints.get(this.x);
		return yLine - this.y;
	}
		
	public int calcVerticalCollisionDistance(Integer yCoord) {
		return Math.abs(yCoord - this.y);
	}
}
