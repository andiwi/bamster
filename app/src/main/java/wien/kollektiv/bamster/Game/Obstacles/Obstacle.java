package wien.kollektiv.bamster.Game.Obstacles;

import java.util.List;
import java.util.Random;

import wien.kollektiv.bamster.R;
import wien.kollektiv.bamster.Game.Hamster.CollisionDetectionPoint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.view.Display;
import android.view.WindowManager;

/**
 * Verwaltet ein Obstacle 
 * @author Andreas
 */
public class Obstacle extends ShapeDrawable {

	private int xPos;
	private int yPos;
	private int mWidth;
	private int mHeight;

	/**
	 * initialisiert dieses Obstacle, gibt ihm eine passende Größe und Position (Random)
	 * @param context
	 */
	public Obstacle(Context context) {

		super(new RectShape());

		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		Point outSize = new Point();
		display.getSize(outSize);

		Random random = new Random();
		int maxY = outSize.y / 15;
		int minY = 20;
		int maxX = outSize.x / 15;
		int minX = 20;

		mWidth = random.nextInt((maxX - minX) + 1) + minX;
		mHeight = random.nextInt((maxY - minY) + 1) + minY;

		Shape shape = new RectShape();
		shape.resize(mWidth, mHeight);

		getShape().resize(mWidth, mHeight);
		getPaint().setColor(
				context.getResources().getColor(R.color.color_obstacle));

		yPos = random.nextInt(outSize.y);
		xPos = outSize.x;
	}

	/**
	 * 
	 * @return yPos
	 */
	public int getYPos() {
		return this.yPos;
	}

	/**
	 * 
	 * @return xPos
	 */
	public int getXPos() {
		return this.xPos;
	}

	/**
	 * 
	 * @return height
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * 
	 * @return width
	 */
	public int getWidth() {
		return mWidth;
	}
	
	/**
	 * 
	 * @param xPos
	 * @return set new xPos
	 */
	public int setXPos(int xPos) {
		return this.xPos = xPos;
	}

	/**
	 * ueberprueft ob das Obstacle sich mit einen der Obstacles aus der Liste
	 * ueberschneidet
	 * 
	 * @param obstacles
	 * @return true wenn es sich ueberschneidet.
	 */
	public boolean checkCollision(List<Obstacle> obstacles) {
		boolean collisionDetected = false;
		Rect rect1 = new Rect(getXPos(), getYPos(), getXPos() + getWidth(),
				getYPos() + getHeight());

		for (Obstacle o : obstacles) {
			Rect rect2 = new Rect(o.getXPos(), o.getYPos(), o.getXPos()
					+ o.getWidth(), o.getYPos() + o.getHeight());

			if (rect1.intersect(rect2)) {
				collisionDetected = true;
				break;
			}
		}
		return collisionDetected;
	}
	
	/**
	 * ueberprueft ob der Punkt innerhalb des Obstacles liegt.
	 * @param point
	 * @return true wenn collisionDetected
	 */
	public boolean checkCollision(CollisionDetectionPoint point) {
		if(xPos <= point.x && point.x <= (xPos + mWidth)
				&& yPos <= point.y && point.y <= yPos + mHeight) {
			return true;
		}
		return false;
	}
}
