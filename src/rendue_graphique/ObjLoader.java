package rendue_graphique;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import math_lib.Point3d;
import math_lib.Triangle3d;

public class ObjLoader {
    public static List<Triangle3d> loadObj(String filePath) {
        List<Point3d> vertices = new ArrayList<>();
        List<Triangle3d> triangles = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue; // Ignorer commentaires

                String[] parts = line.split("\\s+");
                if (parts.length == 0) continue; 
                if (parts[0].equals("v")) {
                    // Sommet : v x y z
                    vertices.add(new Point3d(
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2]),
                        Double.parseDouble(parts[3])
                    ));
                } else if (parts[0].equals("f")) {
              
                	
                    // Face : f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
                    // On ne garde que l'index du sommet (le premier nombre avant le '/')
                    int[] vIndices = new int[parts.length - 1];
                    for (int i = 1; i < parts.length; i++) {
                        vIndices[i - 1] = Integer.parseInt(parts[i].split("/")[0]) - 1;
                    }

                    // Triangulation simple si la face a plus de 3 sommets (ex: Quads)
                    for (int i = 1; i < vIndices.length - 1; i++) {
                        triangles.add(new Triangle3d(
                            vertices.get(vIndices[0]),
                            vertices.get(vIndices[i]),
                            vertices.get(vIndices[i + 1])
                        ));
                    }
                 

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return triangles;
    }
}
