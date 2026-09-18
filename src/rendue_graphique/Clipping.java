package rendue_graphique;

import java.util.ArrayList;
import java.util.List;
import math_lib.*;

public class Clipping {

    private static class ClipResult {
        List<Point3d> out = new ArrayList<>();
        List<Point3d> in = new ArrayList<>();
        boolean isInverted;
    }

    private static ClipResult inZ(Point3d planeNormal, Point3d planePoint, Triangle3d tri) {
        ClipResult res = new ClipResult();
        // Python: dot(planePoint - tri.v1, planeNormal)
        double vert1 = planePoint.sub(tri.p1).dot(planeNormal);
        double vert2 = planePoint.sub(tri.p2).dot(planeNormal);
        double vert3 = planePoint.sub(tri.p3).dot(planeNormal);

        if (vert1 > 0) res.out.add(tri.p1); else res.in.add(tri.p1);
        if (vert2 > 0) res.out.add(tri.p2); else res.in.add(tri.p2);
        if (vert3 > 0) res.out.add(tri.p3); else res.in.add(tri.p3);

        res.isInverted = (vert1 * vert3 > 0);
        return res;
    }

    public static List<Triangle3d> clip(Triangle3d triangle, Point3d camPos, Point3d planeNormal) {
        List<Triangle3d> clippedTriangles = new ArrayList<>();
        // zNear = camPos + 0.1 * planeNormal
        Point3d zNear = camPos.add(planeNormal.mul(0.1));
        
        ClipResult res = inZ(planeNormal, zNear, triangle);

        if (res.out.isEmpty()) {
            clippedTriangles.add(triangle);
        } else if (res.out.size() == 3) {
            // Triangle is completely clipped
        } else if (res.out.size() == 1) {
            // One vertex out: splits into two triangles
            Point3d c0 = triangle.p1.linePlaneCollision(planeNormal, zNear, res.out.get(0), res.in.get(0));
            Point3d c1 = triangle.p1.linePlaneCollision(planeNormal, zNear, res.out.get(0), res.in.get(1));
            
            if (res.isInverted) {
                clippedTriangles.add(new Triangle3d(c1, res.in.get(1), c0));
                clippedTriangles.add(new Triangle3d(c0, res.in.get(1), res.in.get(0)));
            } else {
                clippedTriangles.add(new Triangle3d(c0, res.in.get(0), c1));
                clippedTriangles.add(new Triangle3d(c1, res.in.get(0), res.in.get(1)));
            }
        } else if (res.out.size() == 2) {
            // Two vertices out: becomes one smaller triangle
            Point3d c0 = triangle.p1.linePlaneCollision(planeNormal, zNear, res.out.get(0), res.in.get(0));
            Point3d c1 = triangle.p1.linePlaneCollision(planeNormal, zNear, res.out.get(1), res.in.get(0));
            
            if (res.isInverted) {
                clippedTriangles.add(new Triangle3d(c0, res.in.get(0), c1));
            } else {
                clippedTriangles.add(new Triangle3d(c0, c1, res.in.get(0)));
            }
        }
        return clippedTriangles;
    }
}
