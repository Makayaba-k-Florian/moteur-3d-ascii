package math_lib;

public class Triangle3d {
	public Point3d p1 ;
	public Point3d p2 ;
	public Point3d p3 ;
	
	public Triangle3d(Point3d p1,Point3d p2,Point3d p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		
	}
	public Triangle projection(double focalLength) {
        
		Point2d p1 = this.p1.projection(focalLength);
		Point2d p2 = this.p2.projection(focalLength);
		Point2d p3 = this.p3.projection(focalLength);

       
        return new Triangle(p1, p2, p3);
    }
	public Triangle3d rotationX(double pitch) {
        return new Triangle3d(
            p1.rotationX(pitch), 
            p2.rotationX(pitch), 
            p3.rotationX(pitch)
        );
    }
	
	public Triangle3d rotationY(double yaw) {
        return new Triangle3d(
            p1.rotationY(yaw), 
            p2.rotationY(yaw), 
            p3.rotationY(yaw)
        );
    }
	
	
	public Triangle3d translation(Point3d v) {
	   
	    return new Triangle3d(
	        this.p1.add(v), 
	        this.p2.add(v), 
	        this.p3.add(v)
	    );
	}
	
	public Point3d getMidPoint() {
	    return new Point3d((p1.x+p2.x+p3.x)/3, (p1.y+p2.y+p3.y)/3, (p1.z+p2.z+p3.z)/3);
	}
	public Point3d getNormal() {
	    // Vector p2 - p1
	    Point3d line1 = p2.sub(p1);
	    // Vector p3 - p1
	    Point3d line2 = p3.sub(p1);
	    
	    // The cross product of the two edges gives the face normal
	    return line1.cross(line2);
	}


	

}


