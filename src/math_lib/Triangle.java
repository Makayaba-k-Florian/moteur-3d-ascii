package math_lib;


public class Triangle {
	public Point2d p1 ;
	public Point2d p2 ;
	public Point2d p3 ;
	// Dans math_lib.Triangle
	public double z1, z2, z3; // Profondeurs après transformation mais avant projection

	public Triangle(Point2d p1,Point2d p2,Point2d p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		
	}
	
	public Triangle toScreen() {
			
		return new Triangle(p1.toScreen(), p2.toScreen(), p3.toScreen());
	}
	
	

}
