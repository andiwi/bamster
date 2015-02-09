package wien.kollektiv.bamster.Game.Obstacles;

import java.util.concurrent.CopyOnWriteArrayList;

import wien.kollektiv.bamster.Game.GameLoopThread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Diese Klasse generiert neue Hindernisse und verwaltet diese. Je laenger das
 * Spiel lueft, desto mehr Hindernisse werden generiert. Am Screen nicht mehr
 * sichtbare Hindernisse werden geloescht.
 * 
 * @author Andreas
 * 
 */
public class ObstacleManager {

	private Context mContext;
	private CopyOnWriteArrayList<Obstacle> mObstacles;
	private int mNumberOfObstacles;
	private int mDifficulty;

	public ObstacleManager(Context context, int difficulty) {
		mContext = context;
		mObstacles = new CopyOnWriteArrayList<Obstacle>();
		mDifficulty = difficulty;
	}

	/**
	 * Entfernt alte Hindernisse. Fuegt neue Hindernisse hinzu, falls zu wenige in Liste. (wird anhand der Zeit berechnet) 
	 * @param mScore 
	 * @param ellapsedTime
	 */
	public void updateObstacles(int speed, long score) {
		removeOldObstacles();
		mNumberOfObstacles = calculateNumberOfObstacles(score);
				
		if (mObstacles.size() < mNumberOfObstacles) {
			Obstacle o = generateNewObstacle();
			mObstacles.add(o);
		}
		updatePositions(speed);
	}

	/**
	 * Verschiebt die Hindernisse nach links
	 * @param speed 
	 */
	private void updatePositions(int speed) {
		for(Obstacle o : mObstacles) {
			o.setXPos(o.getXPos() - speed);
		}
	}

	/**
	 * Berechnet anhand der Spielzeit wie viele Obstacles im Spiel vorhanden sein sollen.
	 * Je laenger gespielt wird, desto mehr Hindernisse
	 */
	private int calculateNumberOfObstacles(long score) {
		double k = 0.0025;
		if(mDifficulty == GameLoopThread.DIFFICULTY_EASY) {
			k = 0.0025;
		} else if(mDifficulty == GameLoopThread.DIFFICULTY_MEDIUM) {
			k = 0.0025;
		} else if(mDifficulty == GameLoopThread.DIFFICULTY_HARD) {
			k = 0.003;
		}
		
		int d = 0;
		double result = k * score + d;
		return Double.valueOf(result).intValue();
	}

	/**
	 * Entfernt nicht mehr sichtbare Obstacles (xPosition < 0)
	 */
	private void removeOldObstacles() {
		for(Obstacle o : mObstacles) {
			if((o.getXPos() + o.getWidth()) < 0)
				mObstacles.remove(o);
		}
	}

	/**
	 * Generiert ein neues Obstacle, dass sich mit keinem der anderen Obstacles
	 * ueberschneidet.
	 */
	private Obstacle generateNewObstacle() {
		Obstacle o = new Obstacle(mContext);

		while (o.checkCollision(mObstacles)) {
			o = new Obstacle(mContext);
		}
		return o;
	}
	
	/**
	 * Konvertiert ein Drawable in ein Bitmap und zeichnet dieses auf der Canvas.
	 * @param drawable
	 * @param widthPixels Pixelanzahl breite
	 * @param heightPixels Pixelanzahl huehe
	 * @return
	 */
	private Bitmap convertToBitmap(Drawable drawable, int widthPixels,
			int heightPixels) {
		Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mutableBitmap);
		drawable.setBounds(0, 0, widthPixels, heightPixels);
		drawable.draw(canvas);

		return mutableBitmap;
	}
	
	/**
	 * 
	 * @return alle Obstacles
	 */
	public CopyOnWriteArrayList<Obstacle> getObstacles() {
		return mObstacles;
	}
	
	/**
	 * Zeichnet die Obstacles auf der Canvas.
	 * @param canvas
	 */
	public void draw(Canvas canvas){
		for (Obstacle obstacle : mObstacles) {
			canvas.drawBitmap(
					convertToBitmap(obstacle, obstacle.getHeight(),
							obstacle.getWidth()), obstacle.getXPos(),
					obstacle.getYPos(), null);
		}
	}

}
