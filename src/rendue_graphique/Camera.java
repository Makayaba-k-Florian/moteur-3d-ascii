package rendue_graphique;

import math_lib.Point3d;

public class Camera {
    public Point3d position;
    public double pitch; // Rotation X (haut/bas)
    public double yaw;   // Rotation Y (gauche/droite)
    public double focalLength;

    public Camera(Point3d position, double pitch, double yaw, double focalLength) {
        this.position = position;
        this.pitch = pitch;
        this.yaw = yaw;
        this.focalLength = focalLength;
    }

    public Point3d getForwardDirection() {
        // Moves along the horizontal plane based on where you look
        return new Point3d(-Math.sin(yaw), 0, Math.cos(yaw));
    }

    public Point3d getRightDirection() {
        // Perpendicular to forward
        return new Point3d(Math.cos(yaw), 0, Math.sin(yaw));
    }

 

   
    public Point3d getLookAt() {
        return position.add(getForwardDirection());
    }
}
