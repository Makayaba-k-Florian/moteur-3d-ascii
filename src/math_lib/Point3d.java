package math_lib;

public class Point3d {
	public double x ;
	public double y ;
	public double z ;
	
	
	
	public Point3d(double x,double y,double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		
	}
	
	public Point3d add(Point3d v) {
	    return new Point3d(this.x + v.x, this.y + v.y, this.z + v.z);
	}
	
	public Point3d mul(double scalar) {
	    return new Point3d(this.x * scalar, this.y * scalar, this.z * scalar);
	}
	
	public Point3d rotationY(double yaw) {
	    double cos = Math.cos(yaw);
	    double sin = Math.sin(yaw);
	    return new Point3d(cos * x + sin * z, y, -sin * x + cos * z);
	}

	public Point3d rotationX(double pitch) {
	    double cos = Math.cos(pitch);
	    double sin = Math.sin(pitch);
	    return new Point3d(x, cos * y - sin * z, sin * y + cos * z);
	}

	public Point2d projection(double focalLength) {
	 
	    if (this.z == 0) return new Point2d(0, 0);
	    return new Point2d((focalLength * this.x) / this.z, (focalLength * this.y) / this.z);
	}

	public Point3d sub(Point3d v) {
	    return new Point3d(this.x - v.x, this.y - v.y, this.z - v.z);
	}

	public double dot(Point3d v) {
	    return this.x * v.x + this.y * v.y + this.z * v.z;
	}

	public Point3d cross(Point3d v) {
	    return new Point3d(
	        this.y * v.z - this.z * v.y,
	        this.z * v.x - this.x * v.z,
	        this.x * v.y - this.y * v.x
	    );
	}
	
	public Point3d linePlaneCollision(Point3d planeNormal, Point3d planePoint, Point3d v1, Point3d v2) {
	    // u = v2 - v1
	    Point3d u = v2.sub(v1);
	    
	    // dotp = dot(planeNormal, u)
	    double dotp = planeNormal.dot(u);
	    
	    // if abs(dotp) < 1e-5: return (0,0,0)
	    if (Math.abs(dotp) < 1e-5) {
	        return new Point3d(0, 0, 0); 
	    }
	    
	    // w = v1 - planePoint
	    Point3d w = v1.sub(planePoint);
	    
	    // si = -dot(planeNormal, w) / dotp
	    double si = -planeNormal.dot(w) / dotp;
	    
	    // return v1 + (si * u)
	    return v1.add(u.mul(si));
	}
	
	public double length() {
	    return Math.sqrt(x * x + y * y + z * z);
	}

	public Point3d normalize() {
	    double len = length();
	    if (len == 0) return new Point3d(0, 0, 0);
	    return new Point3d(x / len, y / len, z / len);
	}


    
   

}
