package wien.kollektiv.bamster.Game.Paths;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import static java.util.Map.Entry;

/**
 * Managed alle Paths und bestimmt/kennt den Path unter dem Hamster
 * @author Andreas
 * @author Marlon
 *
 */
public class PathManager {
	
	private Context mContext;
	private ManagedPath mCurrentPath;
	private ManagedPath mFirstPath;
	private CopyOnWriteArrayList<ManagedPath> mPaths;
	private Paint mPaint;
	
	
	private int screenHeight;
	private int screenWidth;
	
	/**
	 * initialisier den PathManager, erstellt einen ersten Pfad
	 * @param context
	 */
	public PathManager(Context context){
		mContext = context;
		mPaths = new CopyOnWriteArrayList<ManagedPath>();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(5);
		mPaint.setColor(Color.BLACK);
		this.initFirstPath();
	}
	
	/**
	 * erstellt einen (ersten) Pfad und speichert ihn als current path
	 */
	private void initFirstPath(){
		ManagedPath firstPath = new ManagedPath();
		WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        this.screenHeight = outSize.y;
        this.screenWidth = outSize.x;
		firstPath.moveTo(0, 0);
		
		int x = 1;
		int y = 1;
		while(x < (screenWidth)) {
			firstPath.lineTo(x, y);
			firstPath.addPointsOfFirstLineToPathPoints(x, y);
			
			if(y < (screenHeight / 2))
				y += 3;
			
			x += 10;
		}
		
		while(x < (screenWidth * 1.5)) {
			firstPath.lineTo(x, y);
			firstPath.addPointsOfFirstLineToPathPoints(x, y);
			x += 50;
		}

		mPaths.add(firstPath);
		mCurrentPath = firstPath;
		mFirstPath = firstPath;
	}
	
	/**
	 * fuegt eine Pfad zum Manager hinzu
	 * @param path
	 */
	public void addPath(ManagedPath path){
		if(!mPaths.contains(path)) {
			mPaths.add(path);
			mCurrentPath = path;
		}
	}
	
	/**
	 * updated die Position aller Paths
	 */
	public void updatePaths(int speed){		
		
		for(ManagedPath path : mPaths){
			path.moveLeft(speed);
			if(!path.isPathVisible())
				mPaths.remove(path);
		}		
	}
	
	/**
	 * 
	 * @return den aktuellen Pfad auf dem sich der Hamster befindet
	 */
	public ManagedPath getCurrentPath() {
		return mCurrentPath;
	}
	
	/**
	 * 
	 * @return alle Pfade
	 */
	public CopyOnWriteArrayList<ManagedPath> getPaths() {
		return mPaths;
	}
	
	/**
	 * zeichnet alle Paths
	 * @param canvas
	 */
	public void draw(Canvas canvas){
		for(ManagedPath path : mPaths){
			canvas.drawPath(path, mPaint);
			
			//FIXME NUR ZUM DEBUGGEN
            /*
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(10);
			paint.setColor(Color.RED);
			
			for(Entry<Integer, Integer> e: path.getPathPoints().entrySet()) {
				canvas.drawPoint(e.getKey(), e.getValue(), paint);
			}
			*/
		}
	}

	/**
	 * 
	 * @return den ersten Pfad
	 */
	public ManagedPath getFirstPath() {
		return mFirstPath;
	}
}
