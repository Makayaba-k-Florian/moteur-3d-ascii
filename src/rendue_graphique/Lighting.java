package rendue_graphique;
import math_lib.*;

public class Lighting {
    private static final String LIGHT_GRADIENT = ".,;la#@";

    public static char diffuseLight(LightSource light, Point3d normal, Point3d vertex) {
        // lightDir = light.position - vertex
        Point3d lightDir = light.position.sub(vertex);
        
        // intensity = dot(lightDir.normalize(), normal.normalize())
        double intensity = lightDir.normalize().dot(normal.normalize());
        
        if (intensity >= 0) {
            // round(intensity * (len - 1))
            int index = (int) Math.round(intensity * (LIGHT_GRADIENT.length() - 1));
            return LIGHT_GRADIENT.charAt(index);
        } else {
            return '.';
        }
    }
}
