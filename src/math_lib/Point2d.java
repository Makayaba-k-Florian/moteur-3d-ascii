package math_lib;
import rendue_graphique.Rendue;
public class Point2d {
	public double x ;
	public double y ;
	

	
	
	
	public Point2d(double x,double y) {
		this.x = x;
		this.y = y;
		
	}
	public Point2d mul(double c) {
	        return new Point2d(this.x * c, this.y * c);
	    }


	    public Point2d div(double c) {
	        return new Point2d(this.x / c, this.y / c);
	    }


	    public Point2d add(Point2d v) {
	        return new Point2d(this.x + v.x, this.y + v.y);
	    }

	 
	    
	    public void addInPlace(Point2d v) {
	        this.x += v.x;
	        this.y += v.y;
	    }

	    public void mulInPlace(double c) {
	        this.x *= c;
	        this.y *= c;
	    }
	    
	 // Dans Point2d.java
	    public Point2d toScreen() {
	    	// Point2d.java
	    	double aspect = (29.0 / 13.0) * ((double) Rendue.height / Rendue.width);
	    	// Multiply your X coordinate by this 'aspect' factor inside toScreen.
	        return new Point2d(
	            (x + 1) * Rendue.width / 2.0, 
	            (-y + 1) * Rendue.height / 2.0
	        );
	    }


}
