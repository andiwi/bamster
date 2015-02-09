package wien.kollektiv.bamster.Game.Paths;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import android.graphics.Path;
import android.graphics.Point;

/**
 * ManagedPath erweitert Android Path: speichert seine eigenen Punkte ab  (aus performancegründen jedoch nur jeden 5. Punkt)
 * @author Andreas
 *
 */
public class ManagedPath extends Path {
	
	private ConcurrentSkipListMap<Integer, Integer> mPathPoints; //key: xCoord value: yCoord
	private int mCounter;
	
	/**
	 * initialisiert den Path
	 */
	public ManagedPath(){
		mPathPoints = new ConcurrentSkipListMap<Integer, Integer>();
		mCounter = 0;
	}
	
	public ManagedPath(ManagedPath managedPath) {
		super(managedPath);
		mPathPoints = new ConcurrentSkipListMap<Integer, Integer>();
		mCounter = 0;
	}
	
	/**
	 * 
	 * @return die abgespeicherten Punkte in Form einer Map key: xCoord value: yCoord
	 */
	public ConcurrentSkipListMap<Integer, Integer> getPathPoints() {
		return mPathPoints;
	}
	
	@Override
	public void lineTo(float x, float y) {
		super.lineTo(x, y);
		mPathPoints.put((int) x,(int) y);
		//addPointToPathPoints((int) x, (int) y);
	}
	
	@Override
	public void quadTo(float x1, float y1, float x2, float y2) {
		super.quadTo(x1, y1, x2, y2);
		mPathPoints.put((int) x2, (int) y2);
		//addPointToPathPoints((int) x2, (int) y2);
	}

    @Override
    public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
        super.cubicTo(x1, y1, x2, y2, x3, y3);
        mPathPoints.put((int)x3, (int)y3);
    }
	
	private void addPointToPathPoints(int x, int y) {
		//Füge nur jeden 3. Punkt zu mPathPoints hinzu
		if(mCounter == 5) {
			synchronized(mPathPoints) {
				mPathPoints.put(x, y);
				mCounter = 0;
			}
		}
		mCounter++;
	}
	
	@Override
	public void moveTo(float x, float y) {
		super.moveTo(x, y);
		mPathPoints.put((int) x, (int) y);
		mCounter++;
	}
	
	@Override
	public void setLastPoint(float dx, float dy) {
		super.setLastPoint(dx, dy);
		mPathPoints.put((int) dx, (int) dy);
	}
	
	/**
	 * bewegt einen pfad um den offset nach links
	 * @param offset
	 */
	public void moveLeft(int xOffset) {
		this.offset(-xOffset, 0);
		
		for(Entry<Integer, Integer> e : mPathPoints.entrySet()){
			Integer x = e.getKey();
			Integer y = e.getValue();
			mPathPoints.remove(x);
			if(isPointVisible(x - xOffset)) {
				mPathPoints.put(x - xOffset, y);
			}
		}
	}
	
	/**
	 * 
	 * @param x
	 * @return true wenn die x Koordinate des Punkts größer 0 ist.
	 */
	private boolean isPointVisible(int x) {
		if(x > 0)
			return true;
		return false;
	}

	/**
	 * checks if a Path still is visible
	 * @return returns true if path is still visible totally, else returning false
	 */
	public boolean isPathVisible(){
		if(!mPathPoints.keySet().isEmpty()) {
			Integer x = mPathPoints.keySet().last();
			if(x > 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param xCoord
	 * @return Gibt den nächst kleineren gespeicherten Entry auf dem Pfad zurück
	 */
	public Point getFloorPoint(int xCoord) {
		Entry<Integer, Integer> entry = mPathPoints.floorEntry(xCoord);
		
		if(entry != null) {
			Point p = new Point();
			p.set(entry.getKey(), entry.getValue());
			return p;
		}else return null;
	}
	
	/**
	 * 
	 * @param xCoord
	 * @return Gibt den nächst größeren gespeicherten Entry auf dem Pfad zurück
	 */
	public Point getHigherPoint(int xCoord) {
		Entry<Integer, Integer> entry = mPathPoints.higherEntry(xCoord);
		if(entry != null) {
			Point p = new Point();
			p.set(entry.getKey(), entry.getValue());
			return p;
		}else return null;
	}	
	
	/**
	 * 
	 * from http://tech-algorithm.com/articles/drawing-line-using-bresenham-algorithm/
	 * Bresenham Algorithmus
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 * @return eine Liste mit allen Punkten zwischen startpunkt und endpunkt 
	 */
	public ConcurrentSkipListMap<Integer, Integer> bresenham(int x,int y,int x2, int y2) {
		//CopyOnWriteArrayList<Point> line = new CopyOnWriteArrayList<Point>();
		ConcurrentSkipListMap<Integer, Integer> line = new ConcurrentSkipListMap<Integer, Integer>();
		
		int w = x2 - x ;
	    int h = y2 - y ;
	    int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
	    if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
	    if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
	    if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
	    int longest = Math.abs(w) ;
	    int shortest = Math.abs(h) ;
	    if (!(longest>shortest)) {
	        longest = Math.abs(h) ;
	        shortest = Math.abs(w) ;
	        if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
	        dx2 = 0 ;            
	    }
	    int numerator = longest >> 1 ;
	    for (int i=0;i<=longest;i++) {
	        line.put(x, y);
	        	        
	        numerator += shortest ;
	        if (!(numerator<longest)) {
	            numerator -= longest ;
	            x += dx1 ;
	            y += dy1 ;
	        } else {
	            x += dx2 ;
	            y += dy2 ;
	        }
	    }
	    
	    return line;
	}
	
	/**
	 * fügt einen Punkt zu dem Path hinzu.
	 * ACHTUNG! Diese Methode darf nur zum automatischen erstellen der ersten Linie verwendet werden.
	 * @param xCoord
	 * @param yCoord
	 */
	//TODO this method is only for the first line (better solution?)
	public void addPointsOfFirstLineToPathPoints(int xCoord, int yCoord){
		mPathPoints.put(xCoord, yCoord);
	}

	public ConcurrentSkipListMap<Integer, Integer> calcCorrectLine(Point startPoint, Point endPoint) {
		ConcurrentSkipListMap<Integer, Integer> line = new ConcurrentSkipListMap<Integer, Integer>();
		
		Point thisPoint = startPoint;
		Point nextPoint = new Point();
		Entry<Integer, Integer> nextEntry = mPathPoints.higherEntry(startPoint.x);
		
		nextPoint.set(nextEntry.getKey(), nextEntry.getValue());
		
		while(nextPoint.x <= endPoint.x) {
			line.putAll(bresenham(thisPoint.x, thisPoint.y, nextPoint.x, nextPoint.y));
			
			if(nextPoint.x == endPoint.x)
				break;
			
			thisPoint = new Point(nextPoint);
			nextEntry = mPathPoints.higherEntry(thisPoint.x);
			nextPoint.set(nextEntry.getKey(), nextEntry.getValue());
		}
		return line;
	}
}
