package wien.kollektiv.bamster.Game.Hamster;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import wien.kollektiv.bamster.Game.GameActivity;
import wien.kollektiv.bamster.Game.GameLoopThread;
import wien.kollektiv.bamster.Game.Obstacles.Obstacle;
import wien.kollektiv.bamster.Game.Obstacles.ObstacleManager;
import wien.kollektiv.bamster.Game.Paths.ManagedPath;
import wien.kollektiv.bamster.Game.Paths.PathManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Hamster ist eine Sprite-Klasse Berechnet die aktuelle x und y Koordinate des Hamsters und animiert mittles Sprite
 * @author Marlon
 * @author Andreas
 */
public class Hamster {
    private static final int BMP_ROWS = 3;
    private static final int BMP_COLUMNS = 3;
    
    private int mX = 0;
    private int mY = 0;
    private Context mContext;
    private Bitmap mBmp;
    private int mCurrentFrameColumn = 0;
    private int mCurrentFrameRow = 0;
    private int mWidth;
    private int mHeight;
    private int screenWidth;
    private int screenHeight;
    private int updateCounter = 0;
    private int mSpeed;
    private int mLifes;
    private Obstacle mLastCollision;
    
    private int SPEED_MEDIUM;
    
    private ManagedPath mCurrentPath;
    private CopyOnWriteArrayList<CollisionDetectionPoint> mCollisionDetectionPointsLine;
    private CopyOnWriteArrayList<CollisionDetectionPoint> mCollisionDetectionPointsObstacle;

    /**
     * initialisiert "Sprite-Fenster"
     * @param context
     * @param bmp
     */
    public Hamster(Context context, int SPEED_MEDIUM, Bitmap bmp) {
    	mContext = context;
    	mBmp = bmp;
        mWidth = bmp.getWidth() / BMP_COLUMNS;
        mHeight = bmp.getHeight() / BMP_ROWS;
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        this.screenHeight = outSize.y;
        this.screenWidth = outSize.x;
        mX = screenWidth / 15;
        
        this.SPEED_MEDIUM = SPEED_MEDIUM;
        mLifes = 3;
        mLastCollision = null;
        
        initCollisionDetectionPointsLine();
        initCollisionDetectionPointsObstacle();
    }

	/**
     * initialisiert die collisionDetectionPoints des Hamsters mit der eine Collision mit Linien ueberprüft wird
     */
    private void initCollisionDetectionPointsLine() {
    	mCollisionDetectionPointsLine = new CopyOnWriteArrayList<CollisionDetectionPoint>();   	
    	
    	mCollisionDetectionPointsLine.add(createCollisionDetectionPoint(90, 0)); //bottom
		mCollisionDetectionPointsLine.add(createCollisionDetectionPoint(50, -1)); //right
		mCollisionDetectionPointsLine.add(createCollisionDetectionPoint(120, 1)); //left
		mCollisionDetectionPointsLine.add(createCollisionDetectionPoint(-20, Integer.MIN_VALUE));

	}
    
    /**
     * initialisiert die collisionDetectionPoints des Hamsters mit der eine Collision mit Obstacles ueberprüft wird
     */
    private void initCollisionDetectionPointsObstacle() {
    	mCollisionDetectionPointsObstacle = new CopyOnWriteArrayList<CollisionDetectionPoint>();
    	
    	mCollisionDetectionPointsObstacle.add(createCollisionDetectionPoint(-90, Integer.MIN_VALUE));
    	mCollisionDetectionPointsObstacle.add(createCollisionDetectionPoint(-60, Integer.MIN_VALUE));
    	mCollisionDetectionPointsObstacle.add(createCollisionDetectionPoint(-30, Integer.MIN_VALUE));
    	mCollisionDetectionPointsObstacle.add(createCollisionDetectionPoint(0, Integer.MIN_VALUE));
    	mCollisionDetectionPointsObstacle.add(createCollisionDetectionPoint(30, Integer.MIN_VALUE));
    	mCollisionDetectionPointsObstacle.add(createCollisionDetectionPoint(60, Integer.MIN_VALUE));
    	mCollisionDetectionPointsObstacle.add(createCollisionDetectionPoint(90, Integer.MIN_VALUE));
		
	}
    
    /**
     * erstellt einen CollisionDetectionPoint am Hamsterrad
     * @param angle gibt an auf welcher Position der Punkt erstellt werden soll
     * @param accelerationFactor gibt an wie schnell der Hamster beschleunigen soll (für Liniencollision relevant)
     * @return
     */
    private CollisionDetectionPoint createCollisionDetectionPoint(int angle, int accelerationFactor) {
    	CollisionDetectionPoint point = new CollisionDetectionPoint(accelerationFactor);
    	double x = (mX + mWidth/2) + getRadius() * Math.cos(Math.toRadians(angle));
    	double y = (mY + mHeight/2) + getRadius() * Math.sin(Math.toRadians(angle));
    	point.set((int) Math.round(x), (int) Math.round(y));
    	
    	return point;
    }

	/**
     * updated Sprite und die y-Position des Hamsters
     * @param pathManager
     */
    public int update(int speed, PathManager pathManager, ObstacleManager obstacleManager) {
    	this.mSpeed = speed;
    	//TODO feinjustierung
    	int mod = ((SPEED_MEDIUM - speed) / 10 < 1) ? 1 : ((SPEED_MEDIUM - speed) / 10);
    	if(updateCounter % mod == 0){
	    	mCurrentFrameColumn = ++mCurrentFrameColumn % BMP_COLUMNS;
	        if (mCurrentFrameColumn != 0)
	        	mCurrentFrameRow = ++mCurrentFrameRow % BMP_ROWS;
    	}
        
        updateCounter++;
        
        mSpeed = calculatePosition(speed, pathManager);        
        
        //zieht nur ein leben ab wenn es mit einem neuen Obstacle kollidiert
        Obstacle o = checkObstacleCollision(obstacleManager);
        if(o != null && mLastCollision != o) {
        	mLastCollision = o;
        	if(mLifes > 0) {
        		mLifes--;
        		((GameActivity)mContext).playCollisionSound();
        	} else {
        		mSpeed = 0;
        	}
        }
        return mSpeed;
    }
    
    /**
     * berechnet die yPosition des Hamsters neu
     * Hamster wird dabei auf die nächst unter sich befindende Linie gesetzt
     * ist der Abstand zu groß (abhängig von der Geschwindigkeit), so fällt der Hamster langsam
     * @param speed aktueller speed des Hamsters
     * @param pathManager welcher die zu überprüfenden Linien verwaltet
     * @return den neuen speed des Hamsters
     */
    private int calculatePosition(int speed, PathManager pathManager) {
    	//Hole alle Paths von PathManager
    	CopyOnWriteArrayList<ManagedPath> relevantPaths = pathManager.getPaths();
    	/*
    	if(relevantPaths.isEmpty()) {
    		//moveVerticallyTo(screenHeight - (mHeight * 2));
    		moveVerticallyTo(screenHeight);
    		Log.i("Hamster", "Spiel verloren.");
    		return 0; //TODO auf 0 setzen
    	*/
    	/*
   		 *
   		 *
   		 * Überprüfe zuerst den aktuellen Pfad, falls vorhanden
   		 * 
   		 * 
   		 */
    	//}else {//Überprüfe ob Hamster gerade auf einer Linie entlang rollt.
    		//Finde die minimale Distanz zwischen CollisionPoint und Linie
    		int minCollisionDistance = Integer.MAX_VALUE;
    		CollisionDetectionPoint collisionDetectionPoint = null;
    		ManagedPath collisionPath = null;
    		
    		Point floorPoint;
    		Point higherPoint;
    		ConcurrentSkipListMap<Integer, Integer> calcPoints;
    		//hamster um 100 Pixel hinaufsetzen (falls Linie steigt) und distanz zu linie berechnen.
    		//Punkt mit der geringsten Distanz ist die neue Position des Hamsters
    		moveVertically(-100);
    		
    		/*
    		if(mCurrentPath != null && relevantPaths.contains(mCurrentPath)) {
	    		//Finde die minimale Distanz zwischen CollisionPoint und Linie
    			for(CollisionDetectionPoint p : mCollisionDetectionPointsLine) {
	    			//relevante Punkte des Paths berechnen.
	    			int distance;
	    			
	    			Integer yCoord = mCurrentPath.getPathPoints().get(p.x);
	    			
	    			if(yCoord != null){ //bresenham wird nicht benötigt
	    				distance = p.calcVerticalCollisionDistance(yCoord);
	    			}else{
	    				floorPoint = mCurrentPath.getFloorPoint(p.x);
		    			higherPoint = mCurrentPath.getHigherPoint(p.x);
		    			if(floorPoint == null || higherPoint == null) {
		    				distance = Integer.MAX_VALUE;
		    			}else {
			    			calcPoints = mCurrentPath.calcCorrectLine(floorPoint, higherPoint);
			    			distance = p.calcVerticalCollisionDistance(calcPoints);
		    			}
	    			}
	    			if(distance < minCollisionDistance && distance >= 0 && distance <= (GameLoopThread.GRAVITY + 50 - speed)) {
	    				minCollisionDistance = distance;
	    				collisionDetectionPoint = p;
	    				collisionPath = mCurrentPath;
	    			}
	    		}
	    		
	    		if(minCollisionDistance < Integer.MAX_VALUE && collisionDetectionPoint != null) {
	    			moveVertically(minCollisionDistance);
	    			return changeSpeed(speed, collisionDetectionPoint.getAccelerationFactor());
	    		}
    		}
    		*/
    		
    		/*
    		 *
    		 *
    		 * Überprüfe allgemein alle relevanten Paths
    		 * 
    		 * 
    		 */
	    	//überprüfe welche Linie die nächste unter Hamster ist auf der der Hamster aufkommt.
	    	//Für alle relevanten Paths die relvante Linienstelle berechnen
	    	
			for(ManagedPath relevantPath : relevantPaths) {
				//if(relevantPath != mCurrentPath) { //mCurrentPath wurde schon oben überprüft
					for(CollisionDetectionPoint p : mCollisionDetectionPointsLine) {
		    			//relevante Punkte des Paths berechnen.
		    			int distance;
		    			
		    			Integer yCoord = relevantPath.getPathPoints().get(p.x);
		    			
		    			if(yCoord != null){ //bresenham wird nicht benötigt
		    				distance = p.calcVerticalCollisionDistance(yCoord);
		    			}else{
		    				floorPoint = relevantPath.getFloorPoint(p.x);
			    			higherPoint = relevantPath.getHigherPoint(p.x);
			    			if(floorPoint == null || higherPoint == null) {
			    				distance = Integer.MAX_VALUE;
			    			}else {
				    			calcPoints = relevantPath.calcCorrectLine(floorPoint, higherPoint);
				    			distance = p.calcVerticalCollisionDistance(calcPoints);
			    			}
		    			}
		    			if(distance < minCollisionDistance && distance >= 0 && distance <= (GameLoopThread.GRAVITY +50 - speed)) {
		    				minCollisionDistance = distance;
		    				collisionDetectionPoint = p;
		    				collisionPath = relevantPath;
		    			}
		    		}
				//}
			}
			
			
			if(minCollisionDistance == Integer.MAX_VALUE || collisionDetectionPoint == null || collisionPath == null) {
				Log.i("Hamster", "Keine Liniencollision gefunden");
				if(mY < screenHeight) {
					moveVertically(GameLoopThread.GRAVITY);
					return changeSpeed(speed, 0);
				}else {
					return 0;
				}
			}else {
				mCurrentPath = collisionPath;
	    		moveVertically(minCollisionDistance);
	    		if(mCurrentPath == pathManager.getFirstPath()) {
	    			return speed;
	    		}else{
	    			return changeSpeed(speed, collisionDetectionPoint.getAccelerationFactor());
	    		}
			}
    	}
    //}
    
    /**
     * Überprüft ob eine Collision mit einem Obstacle aus dem obstacleManager vorhanden ist
     * @param obstacleManager
     * @return true wenn Collision entdeckt
     */
    private Obstacle checkObstacleCollision(ObstacleManager obstacleManager) {
    	for(CollisionDetectionPoint p : mCollisionDetectionPointsObstacle)
    	{
    		for(Obstacle o : obstacleManager.getObstacles()) {
    			if(o.checkCollision(p)) {
    				Log.i("hamster", "collisionDetected");
    				return o;
    			}
    		}
    	}
		return null;
	}
    
    /**
     * 0 <= result <= GameLoopThread.SPEED_MAXIMUM
     * @param currentSpeed
     * @param accelerationFactor
     * @return currentSpeed += accelerationFactor 
     */
    private int changeSpeed(int currentSpeed, int accelerationFactor) {
    	currentSpeed += accelerationFactor;
    	if(currentSpeed > GameLoopThread.SPEED_MAXIMUM)
    		return GameLoopThread.SPEED_MAXIMUM;
    	if(currentSpeed >= 0)
    		return currentSpeed;
    	return 0;
    }

    /**
     * versetzt den Hamster (inkl. CollisionDetectionPoints) vertikal um die distance
     * @param distance
     */
	private void moveVertically(int distance) {
		mY += distance;
		
		for(CollisionDetectionPoint p : mCollisionDetectionPointsLine) {
			p.y += distance;
		}
		
		for(CollisionDetectionPoint p : mCollisionDetectionPointsObstacle) {
			p.y += distance;
		}
	}

	/**
     * zeichnet den richtigen Ausschnitt des Sprites/den Hamster
     * @param canvas
     */    
    public void draw(Canvas canvas) {
    	// inverse from current column to turn hasters direction
    	int srcX = (BMP_COLUMNS - 1 - mCurrentFrameColumn) * mWidth;
        int srcY = (BMP_ROWS - 1 - mCurrentFrameRow) * mHeight;
        Rect src = new Rect(srcX, srcY, srcX + mWidth, srcY + mHeight);
        Rect dst = new Rect(mX, mY, mX + mWidth, mY + mHeight);
        
        canvas.drawBitmap(mBmp, src, dst, null);
       
        //FIXME just for debugging
    	/*
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(2);
		mPaint.setColor(Color.BLACK);
    	
    	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(10);
		paint.setColor(Color.RED);
    	
    	for(Point p : mCollisionDetectionPointsLine) {
    		canvas.drawPoint(p.x, p.y, paint);
    	}
    	
    	paint.setColor(Color.MAGENTA);
    	
    	for(Point p : mCollisionDetectionPointsObstacle) {
    		canvas.drawPoint(p.x, p.y, paint);
    	}
    	*/
    	
    }
    
    /**
     * @return x-Koordinate des Hamsters
     */
    public int getXCoord(){
    	return mX;
    }
    
    /**
     * @return y-Koordinate des Hamsters
     */
    public int getYCoord(){
    	return mY;
    }
    
    /**
     * @return Höhe des Hamsters
     */
    public int getHeight(){
    	return mHeight;
    }
    
    /**
     * @return Breite des Hamsters (== Höhe!)
     */
    public int getWidth(){
    	return mWidth;
    }
    
    /**
     * @return Radius des Hamsters
     */
    public int getRadius(){
    	return mHeight / 2;
    }

	/**
	 * @return noch verfügbare Leben
	 */
	public int getLifes() {
		return mLifes;
	}
}