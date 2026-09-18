package rendue_graphique;



import java.util.Arrays;
import java.util.List;


import math_lib.Point2d;
import math_lib.Point3d;
import math_lib.Triangle;
import math_lib.Triangle3d;
public class Rendue {
	public static int width = 80*2 + 9+16+5; 
	public static int height = 37+14+7;
	public static char[] pixelBuffer = new char[width * height];
	//public static double[] zBuffer = new double[width * height];
  
   

    public void draw() {
       
        System.out.print("\033[H"); //remonte le curseur
        //System.out.print(new String(pixelBuffer));
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
        //Arrays.fill(zBuffer, Double.MAX_VALUE); // On reset avec une distance infinie
    }

 
    public boolean isInTriange(Point2d pos,Triangle tri) {
    	

       
        double det = (tri.p2.y - tri.p3.y) * (tri.p1.x - tri.p3.x) + (tri.p3.x - tri.p2.x) * (tri.p1.y - tri.p3.y);

  
        double w1 = ((tri.p2.y - tri.p3.y) * (pos.x - tri.p3.x) + (tri.p3.x - tri.p2.x) * (pos.y - tri.p3.y)) / det;
        double w2 = ((tri.p3.y - tri.p1.y) * (pos.x - tri.p3.x) + (tri.p1.x - tri.p3.x) * (pos.y - tri.p3.y)) / det;
        double w3 = 1.0 - w1 - w2;

   
        return w1 >= 0 && w2 >= 0 && w3 >= 0;
		
	}
    
    public void putTriangle(Triangle tri, char c) {
    	//System.out.println("triangle call");
        int xmin = (int) Math.max(0, Math.min(tri.p1.x, Math.min(tri.p2.x, tri.p3.x)));
        int xmax = (int) Math.min(width - 1, Math.max(tri.p1.x, Math.max(tri.p2.x, tri.p3.x)))+1;
        
        int ymin = (int) Math.max(0, Math.min(tri.p1.y, Math.min(tri.p2.y, tri.p3.y)));
        int ymax = (int) Math.min(height - 1, Math.max(tri.p1.y, Math.max(tri.p2.y, tri.p3.y)))+1;

  
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
        // We sort the list so that triangles further away are drawn first, 
        // and closer ones are drawn on top.
        mesh.sort((t1, t2) -> {
            double d1 = t1.getMidPoint().sub(cam.position).length();
            double d2 = t2.getMidPoint().sub(cam.position).length();
            return Double.compare(d2, d1); // Reverse order (furthest first)
        });

        for (Triangle3d tri : mesh) {
            // --- 2. CAMERA TRANSFORMATION ---
            Triangle3d viewTri = tri.translation(cam.position.mul(-1.0))
                                     .rotationY(-cam.yaw)
                                     .rotationX(-cam.pitch);

            // --- 3. BACK-FACE CULLING ---
            // We calculate the normal of the triangle in view-space.
            // If it points away from the camera, we don't draw it.
            Point3d vLine1 = viewTri.p2.sub(viewTri.p1);
            Point3d vLine2 = viewTri.p3.sub(viewTri.p1);
            Point3d vNormal = vLine1.cross(vLine2);

            // Dot product < 0 means the face is looking at the camera
            if (vNormal.dot(viewTri.p1) < 0) {
                
                // --- 4. LIGHTING ---
                Point3d worldNormal = tri.getNormal().normalize();
                Point3d lightDir = light.position.sub(tri.p1).normalize();
                double intensity = worldNormal.dot(lightDir);

               /* char shade;
                if (intensity > 0.8)      shade = '@';
                else if (intensity > 0.5) shade = '#';
                else if (intensity > 0.2) shade = '*';
                else                      shade = '.';
                */
                
             

                // 3. Sélection du caractère avec '_' pour l'ombre
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
                    // Si l'intensité est faible ou négative (face cachée du soleil)
                    shade = '.'; 
                }

                // --- 5. DRAWING ---
                if (viewTri.p1.z > 0.1 && viewTri.p2.z > 0.1 && viewTri.p3.z > 0.1) {
                    putTriangle(viewTri.projection(cam.focalLength).toScreen(), shade);
                }
            }
        }
    }



    
  

    /**
     * 
    public void putMesh(List<Triangle3d> mesh, Camera cam, LightSource light) {
        for (Triangle3d tri : mesh) {
            // --- 1. Camera Transformation ---
            Triangle3d viewTri = tri.translation(cam.position.mul(-1.0))
                                     .rotationY(-cam.yaw)
                                     .rotationX(-cam.pitch);

            // --- 2. Light & Face Logic ---
            Point3d normal = tri.getNormal().normalize();
            Point3d lightDir = light.position.sub(tri.p1).normalize();
            double intensity = normal.dot(lightDir);

            // --- 3. Unique Character Mapping ---
            // We use the normal components (x, y, z) to pick a unique character "style"
            char shade;
            if (intensity < -0.1) {
                // Shadow logic: different characters based on orientation
                if (Math.abs(normal.y) > 0.8) shade = '-';      // Top/Bottom shadow
                else if (Math.abs(normal.x) > 0.8) shade = '|'; // Side shadow
                else shade = '.';                               // Back shadow
            } else {
                // Lit faces: use your diversified shades
                if (intensity > 0.9)      shade = '@';
                else if (intensity > 0.7) shade = '#';
                else if (intensity > 0.5) shade = '&';
                else if (intensity > 0.3) shade = '%';
                else if (intensity > 0.1) shade = '*';
                else                      shade = ':';
                
                // TRICK: Change the character slightly based on the face direction
                // so the front face '@' looks different from the top face '@'
                if (Math.abs(normal.y) > 0.6 && shade == '#') shade = '=';
                if (Math.abs(normal.x) > 0.6 && shade == '#') shade = 'H';
            }

            // --- 4. Back-face Culling (Crucial so faces don't mix) ---
            // Calculate normal in view space
            Point3d vNormal = viewTri.getNormal();
            if (vNormal.dot(viewTri.p1) < 0) {
                if (viewTri.p1.z > 0.1 && viewTri.p2.z > 0.1 && viewTri.p3.z > 0.1) {
                    putTriangle(viewTri.projection(cam.focalLength).toScreen(), shade);
                }
            }
        }
    }
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
    public void putMesh(List<Triangle3d> mesh, Camera cam, LightSource light) {
        // The gradient from darkest to brightest
        String gradient = ".:!*oe&#@"; 

        for (Triangle3d tri : mesh) {
            // 1. Camera Transformation
            Triangle3d viewTri = tri.translation(cam.position.mul(-1.0))
                                     .rotationY(-cam.yaw)
                                     .rotationX(-cam.pitch);

         // 2. Light Calculation
            Point3d normal = tri.getNormal().normalize();
            Point3d lightDir = light.position.sub(tri.p1).normalize();
            double intensity = normal.dot(lightDir);

            // 3. Diversified Shade Selection
            char shade;
            if (intensity > 0.9) {
                shade = '@'; // Direct highlight
            } else if (intensity > 0.7) {
                shade = '#'; // Bright surface
            } else if (intensity > 0.5) {
                shade = '&'; // Well lit
            } else if (intensity > 0.3) {
                shade = '*'; // Mid-tone
            } else if (intensity > 0.1) {
                shade = '!'; // Dimly lit
            } else if (intensity > 0.0) {
                shade = ':'; // Edge of shadow
            } else if (intensity > -0.3) {
                shade = '.'; // Soft shadow
            } else {
                shade = '$'; // Deep shadow (or use ' ' to make it invisible)
            }


            // 4. Draw (Only if in front of camera)
            if (viewTri.p1.z > 0.1 && viewTri.p2.z > 0.1 && viewTri.p3.z > 0.1) {
                putTriangle(viewTri.projection(cam.focalLength).toScreen(), shade);
            }
        }
    }

     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     *   public void putMesh(List<Triangle3d> mesh, Camera cam, LightSource light) {
        for (Triangle3d tri : mesh) {
            // 1. Position the triangle relative to the camera
            Triangle3d viewTri = tri.translation(cam.position.mul(-1.0));

            // 2. Rotate the world INVERSELY (This creates the "Camera" effect)
            viewTri = viewTri.rotationY(-cam.yaw);
            viewTri = viewTri.rotationX(-cam.pitch);

            // 3. Simple Z-check (Forget complex clipping for a second to see if it works)
            if (viewTri.p1.z > 0.1 && viewTri.p2.z > 0.1 && viewTri.p3.z > 0.1) {
                // Use '#' for now to ensure we see pixels
                putTriangle(viewTri.projection(cam.focalLength).toScreen(), '#');
                
            }
        }
    }
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * public void putMesh(List<Triangle3d> mesh, Camera cam, LightSource light) {
        for (Triangle3d tri : mesh) {
            // 1. Position the triangle relative to the camera
            Triangle3d viewTri = tri.translation(cam.position.mul(-1.0));

            // 2. Rotate the world INVERSELY (This creates the "Camera" effect)
            viewTri = viewTri.rotationY(-cam.yaw);
            viewTri = viewTri.rotationX(-cam.pitch);

            // 3. Simple Z-check (Forget complex clipping for a second to see if it works)
            if (viewTri.p1.z > 0.1 && viewTri.p2.z > 0.1 && viewTri.p3.z > 0.1) {
                // Use '#' for now to ensure we see pixels
                putTriangle(viewTri.projection(cam.focalLength).toScreen(), '#');
            }
        }
    }
     * @param mesh
     * @param cam
     * @param light
     */
    

    
   
    
    
    
    
    
    
    /**
     * 
     * @param mesh
     * @param cam
     * @param light
     */
    
    
    /*
     *  public void putMesh(List<Triangle3d> mesh, Camera cam, LightSource light) {
        // ... (sorting code) ...

        for (Triangle3d triangle : mesh) {
            // 1. Transform to View Space FIRST
            Triangle3d viewTri = triangle.translation(cam.position.mul(-1.0))
                                         .rotationY(-cam.yaw)
                                         .rotationX(-cam.pitch);

            // 2. Clip against a fixed plane (Z = 0.1)
            // planeNormal = (0,0,1), planePoint = (0,0,0.1)
            List<Triangle3d> clippedTriangles = Clipping.clip(viewTri, new Point3d(0,0,0), new Point3d(0,0,1));

            for (Triangle3d clipped : clippedTriangles) {
                // 3. Draw the clipped pieces
                Point3d normal = clipped.getNormal(); // Normal of the view-space triangle
                char shade = Lighting.diffuseLight(light, normal, clipped.p1);
                
                putTriangle(clipped.projection(cam.focalLength).toScreen(), shade);
            }
        }
    }

    
     * 
     * public void putMesh(List<Triangle3d> mesh, Camera cam, LightSource light) {
        // 1. Z-Sorting (Painter's Algorithm)
        // We sort the mesh based on distance to the camera (furthest first)
    	
        mesh.sort((t1, t2) -> {
            double d1 = t1.getMidPoint().sub(cam.position).length();
            double d2 = t2.getMidPoint().sub(cam.position).length();
            return Double.compare(d2, d1);
        });

        Point3d lookAt = cam.getLookAt();
       
        for (Triangle3d triangle : mesh) {
            // 2. Clipping
            // Prevents triangles from stretching/crashing when they go behind the camera
        	List<Triangle3d> clippedTriangles = Clipping.clip(triangle, cam.position, lookAt);
        	if (clippedTriangles.isEmpty()) {
        		System.out.println("mesh call");
        	}


            for (Triangle3d clipped : clippedTriangles) {
                // 3. Back-face Culling
                // We calculate the normal to check if the triangle is facing the camera
                Point3d surfaceNorm = clipped.getNormal();
                
                char shade = Lighting.diffuseLight(light, surfaceNorm, clipped.p1);

                // 5. Final Transformation & Draw
                // Note: We use -cam.yaw and -cam.pitch for the camera view logic
                Triangle projected = clipped
                            .translation(cam.position.mul(-1.0))
                            .rotationY(-cam.yaw)
                            .rotationX(-cam.pitch)
                            .projection(cam.focalLength)
                            .toScreen();
                
                putTriangle(projected, shade);
                
                if (clipped.translation(cam.position.mul(-1.0)).rotationY(-cam.yaw).rotationX(-cam.pitch).p1.z > 0.1) {
                    putTriangle(projected, shade);
                    System.out.println("mesh call");
                    
                }
             

                
            }
        }
    }
    
     * 
     * 
     * 
     * 
     * public void putMesh(List<Triangle3d> mesh, Camera cam, LightSource light) {
    for (Triangle3d triangle : mesh) {
        // 1. Transformation manuelle simple pour tester
        Triangle projected = triangle
                    .translation(cam.position.mul(-1.0))
                    .rotationY(-cam.yaw)
                    .rotationX(-cam.pitch)
                    .projection(cam.focalLength)
                    .toScreen();

        // 2. Appel direct sans clipping ni culling
        System.out.println("Tentative de dessin face..."); // Si ça s'affiche dans la console, putTriangle est appelé
        putTriangle(projected, '#'); 
    }
}
*/
    
}



