package rendue_graphique;

import java.util.Arrays;
import java.util.List;

import math_lib.Point2d;
import math_lib.Point3d;
import math_lib.Triangle;
import math_lib.Triangle3d;

public class Rendue {
    public static int width = 80 * 2 + 9 + 16 + 5;
    public static int height = 37 + 14 + 7;
    public static char[] pixelBuffer = new char[width * height];

    public void draw() {
        System.out.print("\033[H"); // remonte le curseur
        StringBuilder frame = new StringBuilder();
        for (int i = 0; i < width * height; i++) {
            frame.append(i % width == 0 && i > 0 ? "\n" : "").append(pixelBuffer[i]);
        }
        System.out.print(frame);
    }

    public void putPixel(Point2d p, char c) {
        int px = (int) Math.round(p.x);
        int py = (int) Math.round(p.y);

        if (px >= 0 && px < width && py >= 0 && py < height) {
            pixelBuffer[py * width + px] = c;
        }
    }

    public void clear(char c) {
        Arrays.fill(pixelBuffer, c);
    }

    public boolean isInTriange(Point2d pos, Triangle tri) {
        double det = (tri.p2.y - tri.p3.y) * (tri.p1.x - tri.p3.x) + (tri.p3.x - tri.p2.x) * (tri.p1.y - tri.p3.y);

        double w1 = ((tri.p2.y - tri.p3.y) * (pos.x - tri.p3.x) + (tri.p3.x - tri.p2.x) * (pos.y - tri.p3.y)) / det;
        double w2 = ((tri.p3.y - tri.p1.y) * (pos.x - tri.p3.x) + (tri.p1.x - tri.p3.x) * (pos.y - tri.p3.y)) / det;
        double w3 = 1.0 - w1 - w2;

        return w1 >= 0 && w2 >= 0 && w3 >= 0;
    }
    
    public void putTriangle(Triangle tri, char c) {
        int xmin = (int) Math.max(0, Math.min(tri.p1.x, Math.min(tri.p2.x, tri.p3.x)));
        int xmax = (int) Math.min(width - 1, Math.max(tri.p1.x, Math.max(tri.p2.x, tri.p3.x))) + 1;
        
        int ymin = (int) Math.max(0, Math.min(tri.p1.y, Math.min(tri.p2.y, tri.p3.y)));
        int ymax = (int) Math.min(height - 1, Math.max(tri.p1.y, Math.max(tri.p2.y, tri.p3.y))) + 1;

        for (int y = ymin; y <= ymax; y++) {
            for (int x = xmin; x <= xmax; x++) { 
                Point2d pos = new Point2d(x, y);
                if (isInTriange(pos, tri)) {
                    putPixel(pos, c);
                }
            }
        }
    }

    public void putMesh(List<Triangle3d> mesh, Camera cam, LightSource light) {
        // 1. Z-SORTING (Painter's Algorithm)
        mesh.sort((t1, t2) -> {
            double d1 = t1.getMidPoint().sub(cam.position).length();
            double d2 = t2.getMidPoint().sub(cam.position).length();
            return Double.compare(d2, d1); // Furthest first
        });

        for (Triangle3d tri : mesh) {
            // 2. CAMERA TRANSFORMATION
            Triangle3d viewTri = tri.translation(cam.position.mul(-1.0))
                                     .rotationY(-cam.yaw)
                                     .rotationX(-cam.pitch);

            // 3. BACK-FACE CULLING
            Point3d vLine1 = viewTri.p2.sub(viewTri.p1);
            Point3d vLine2 = viewTri.p3.sub(viewTri.p1);
            Point3d vNormal = vLine1.cross(vLine2);

            // Dot product < 0 means the face is looking at the camera
            if (vNormal.dot(viewTri.p1) < 0) {
                // 4. LIGHTING
                Point3d worldNormal = tri.getNormal().normalize();
                Point3d lightDir = light.position.sub(tri.p1).normalize();
                double intensity = worldNormal.dot(lightDir);

                char shade;
                if (intensity > 0.9) {
                    shade = '@';
                } else if (intensity > 0.7) {
                    shade = '#';
                } else if (intensity > 0.5) {
                    shade = '&';
                } else if (intensity > 0.3) {
                    shade = '*';
                } else if (intensity > 0.1) {
                    shade = ':';
                } else {
                    shade = '.'; 
                }

                // 5. DRAWING
                if (viewTri.p1.z > 0.1 && viewTri.p2.z > 0.1 && viewTri.p3.z > 0.1) {
                    putTriangle(viewTri.projection(cam.focalLength).toScreen(), shade);
                }
            }
        }
    }
}
