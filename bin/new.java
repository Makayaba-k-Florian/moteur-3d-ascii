import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Moteur graphique 3D ASCII en Java
 * Port du moteur Python original.
 *
 * Compilation : javac Engine3D.java
 * Exécution   : java Engine3D cube.obj
 *
 * Contrôles :
 *   Z / S          → avancer / reculer
 *   Q / D          → strafe gauche / droite
 *   Flèches        → regarder haut/bas/gauche/droite
 *   ESPACE / SHIFT → monter / descendre
 *   ECHAP          → quitter
 *
 * Dépendance clavier : JLine3 (jar dans le classpath).
 * Voir README en bas de ce fichier pour l'installation.
 */
public class Engine3D {

    // =========================================================
    //  MATH
    // =========================================================

    static class Vec2 {
        double x, y;
        Vec2(double x, double y) { this.x = x; this.y = y; }

        Vec2 add(Vec2 v)    { return new Vec2(x + v.x, y + v.y); }
        Vec2 sub(Vec2 v)    { return new Vec2(x - v.x, y - v.y); }
        Vec2 mul(double c)  { return new Vec2(x * c,   y * c);   }
        Vec2 div(double c)  { return new Vec2(x / c,   y / c);   }

        /** Projection NDC → coordonnées écran terminal */
        Vec2 toScreen(int width, int height) {
            double sx = ((29.0 / 13.0) * height / width * x + 1.0) * width  / 2.0;
            double sy = (-y + 1.0) * height / 2.0;
            return new Vec2(sx, sy);
        }
    }

    static class Vec3 {
        double x, y, z;
        Vec3(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }

        Vec3 add(Vec3 v)    { return new Vec3(x + v.x, y + v.y, z + v.z); }
        Vec3 sub(Vec3 v)    { return new Vec3(x - v.x, y - v.y, z - v.z); }
        Vec3 mul(double c)  { return new Vec3(x * c,   y * c,   z * c);   }
        Vec3 div(double c)  { return new Vec3(x / c,   y / c,   z / c);   }
        Vec3 neg()          { return new Vec3(-x, -y, -z); }

        double length() {
            return Math.sqrt(x*x + y*y + z*z);
        }

        Vec3 normalize() {
            double n = length();
            return new Vec3(x/n, y/n, z/n);
        }

        /** Projection perspective → Vec2 NDC */
        Vec2 projection(double focalLength) {
            return new Vec2(x, y).mul(focalLength).div(z);
        }

        Vec3 rotationX(double pitch) {
            double y1 =  Math.cos(pitch)*y - Math.sin(pitch)*z;
            double z1 =  Math.sin(pitch)*y + Math.cos(pitch)*z;
            return new Vec3(x, y1, z1);
        }

        Vec3 rotationY(double yaw) {
            double x1 =  Math.cos(yaw)*x + Math.sin(yaw)*z;
            double z1 = -Math.sin(yaw)*x + Math.cos(yaw)*z;
            return new Vec3(x1, y, z1);
        }
    }

    static double dot(Vec3 a, Vec3 b) {
        return a.x*b.x + a.y*b.y + a.z*b.z;
    }

    static Vec3 cross(Vec3 a, Vec3 b) {
        return new Vec3(
            a.y*b.z - a.z*b.y,
            a.z*b.x - a.x*b.z,
            a.x*b.y - a.y*b.x
        );
    }

    /**
     * Intersection rayon/plan.
     * Renvoie null si le rayon est parallèle au plan.
     */
    static Vec3 linePlaneCollision(Vec3 planeNormal, Vec3 planePoint, Vec3 v1, Vec3 v2) {
        Vec3 u    = v2.sub(v1);
        double dotp = dot(planeNormal, u);
        if (Math.abs(dotp) < 1e-5) return null;
        double si = -dot(planeNormal, v1.sub(planePoint)) / dotp;
        return v1.add(u.mul(si));
    }

    // =========================================================
    //  TRIANGLES
    // =========================================================

    static class Triangle3D {
        Vec3 v1, v2, v3;
        Triangle3D(Vec3 v1, Vec3 v2, Vec3 v3) { this.v1 = v1; this.v2 = v2; this.v3 = v3; }

        Triangle3D translate(Vec3 t) {
            return new Triangle3D(v1.add(t), v2.add(t), v3.add(t));
        }
        Triangle3D rotationX(double pitch) {
            return new Triangle3D(v1.rotationX(pitch), v2.rotationX(pitch), v3.rotationX(pitch));
        }
        Triangle3D rotationY(double yaw) {
            return new Triangle3D(v1.rotationY(yaw), v2.rotationY(yaw), v3.rotationY(yaw));
        }

        /** Renvoie le triangle projeté en 2D NDC */
        Triangle2D projection(double focalLength) {
            return new Triangle2D(
                v1.projection(focalLength),
                v2.projection(focalLength),
                v3.projection(focalLength)
            );
        }

        double centerDistanceTo(Vec3 cam) {
            Vec3 center = v1.add(v2).add(v3).div(3.0);
            return center.sub(cam).length();
        }
    }

    static class Triangle2D {
        Vec2 v1, v2, v3;
        Triangle2D(Vec2 v1, Vec2 v2, Vec2 v3) { this.v1 = v1; this.v2 = v2; this.v3 = v3; }

        Triangle2D toScreen(int width, int height) {
            return new Triangle2D(
                v1.toScreen(width, height),
                v2.toScreen(width, height),
                v3.toScreen(width, height)
            );
        }
    }

    // =========================================================
    //  CAMÉRA
    // =========================================================

    static class Camera {
        Vec3   position;
        double pitch, yaw, focalLength;

        Camera(Vec3 position, double pitch, double yaw, double focalLength) {
            this.position    = position;
            this.pitch       = pitch;
            this.yaw         = yaw;
            this.focalLength = focalLength;
        }

        Vec3 getLookAtDirection() {
            return new Vec3(
                -Math.sin(yaw) * Math.cos(pitch),
                 Math.sin(pitch),
                 Math.cos(yaw) * Math.cos(pitch)
            );
        }

        Vec3 getForwardDirection() {
            return new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        }

        Vec3 getRightDirection() {
            return new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        }
    }

    // =========================================================
    //  SOURCE LUMINEUSE
    // =========================================================

    static class LightSource {
        Vec3 position;
        LightSource(Vec3 position) { this.position = position; }
    }

    // =========================================================
    //  RENDU
    // =========================================================

    static final String LIGHT_GRADIENT = ".,;la#@";

    /** Calcule le caractère d'ombrage diffus */
    static char diffuseLight(LightSource light, Vec3 normal, Vec3 vertex) {
        Vec3   lightDir   = light.position.sub(vertex);
        double intensity  = dot(lightDir.normalize(), normal.normalize());
        if (intensity < 0) return '.';
        int idx = (int) Math.round(intensity * (LIGHT_GRADIENT.length() - 1));
        idx = Math.max(0, Math.min(LIGHT_GRADIENT.length() - 1, idx));
        return LIGHT_GRADIENT.charAt(idx);
    }

    static char[] pixelBuffer;
    static int    screenWidth, screenHeight;

    static void initBuffer(int w, int h) {
        screenWidth  = w;
        screenHeight = h;
        pixelBuffer  = new char[w * h];
        Arrays.fill(pixelBuffer, ' ');
    }

    static void clearBuffer(char c) {
        Arrays.fill(pixelBuffer, c);
    }

    static void putPixel(Vec2 v, char c) {
        int px = (int) Math.round(v.x);
        int py = (int) Math.round(v.y);
        if (px >= 0 && px < screenWidth && py >= 0 && py < screenHeight) {
            pixelBuffer[py * screenWidth + px] = c;
        }
    }

    /** Rasterisation d'un triangle 2D par barycentrique entier */
    static void putTriangle(Triangle2D tri, char c) {
        int xmin = (int) Math.round(Math.min(tri.v1.x, Math.min(tri.v2.x, tri.v3.x)));
        int xmax = (int) Math.round(Math.max(tri.v1.x, Math.max(tri.v2.x, tri.v3.x))) + 1;
        int ymin = (int) Math.round(Math.min(tri.v1.y, Math.min(tri.v2.y, tri.v3.y)));
        int ymax = (int) Math.round(Math.max(tri.v1.y, Math.max(tri.v2.y, tri.v3.y))) + 1;

        for (int y = ymin; y < ymax; y++) {
            if (y < 0 || y >= screenHeight) continue;
            for (int x = xmin; x < xmax; x++) {
                if (x < 0 || x >= screenWidth) continue;
                Vec2 p = new Vec2(x, y);
                double w1 = edgeFunc(p, tri.v3, tri.v1);
                double w2 = edgeFunc(p, tri.v1, tri.v2);
                double w3 = edgeFunc(p, tri.v2, tri.v3);
                if ((w1 >= 0 && w2 >= 0 && w3 >= 0) || (-w1 >= 0 && -w2 >= 0 && -w3 >= 0)) {
                    putPixel(p, c);
                }
            }
        }
    }

    static double edgeFunc(Vec2 p, Vec2 a, Vec2 b) {
        return (a.x - p.x) * (b.y - p.y) - (a.y - p.y) * (b.x - p.x);
    }

    /** Clipping near-plane. Renvoie 0, 1 ou 2 triangles. */
    static List<Triangle3D> clip(Triangle3D tri, Vec3 camPos, Vec3 planeNormal) {
        Vec3 zNear = camPos.add(planeNormal.mul(0.1));

        double d1 = dot(zNear.sub(tri.v1), planeNormal);
        double d2 = dot(zNear.sub(tri.v2), planeNormal);
        double d3 = dot(zNear.sub(tri.v3), planeNormal);

        List<Vec3> outside = new ArrayList<>();
        List<Vec3> inside  = new ArrayList<>();

        if (d1 > 0) outside.add(tri.v1); else inside.add(tri.v1);
        if (d2 > 0) outside.add(tri.v2); else inside.add(tri.v2);
        if (d3 > 0) outside.add(tri.v3); else inside.add(tri.v3);

        boolean isInverted = (d1 * d3 > 0);

        if (outside.isEmpty()) {
            // Triangle entièrement visible
            return Collections.singletonList(tri);
        }
        if (outside.size() == 3) {
            // Triangle entièrement derrière la caméra
            return Collections.emptyList();
        }
        if (outside.size() == 1) {
            // Un sommet dehors → 2 nouveaux triangles
            Vec3 c0 = linePlaneCollision(planeNormal, zNear, outside.get(0), inside.get(0));
            Vec3 c1 = linePlaneCollision(planeNormal, zNear, outside.get(0), inside.get(1));
            if (c0 == null || c1 == null) return Collections.emptyList();
            List<Triangle3D> result = new ArrayList<>();
            if (isInverted) {
                result.add(new Triangle3D(c1, inside.get(1), c0));
                result.add(new Triangle3D(c0, inside.get(1), inside.get(0)));
            } else {
                result.add(new Triangle3D(c0, inside.get(0), c1));
                result.add(new Triangle3D(c1, inside.get(0), inside.get(1)));
            }
            return result;
        }
        // outside.size() == 2 : deux sommets dehors → 1 triangle réduit
        Vec3 c0 = linePlaneCollision(planeNormal, zNear, outside.get(0), inside.get(0));
        Vec3 c1 = linePlaneCollision(planeNormal, zNear, outside.get(1), inside.get(0));
        if (c0 == null || c1 == null) return Collections.emptyList();
        if (isInverted) {
            return Collections.singletonList(new Triangle3D(c0, inside.get(0), c1));
        } else {
            return Collections.singletonList(new Triangle3D(c0, c1, inside.get(0)));
        }
    }

    /** Rendu du mesh complet avec tri painter's-algorithm */
    static void putMesh(List<Triangle3D> mesh, Camera cam, LightSource light) {
        final Vec3 camPos = cam.position;
        mesh.sort((a, b) -> Double.compare(b.centerDistanceTo(camPos),
                                           a.centerDistanceTo(camPos)));

        Vec3 lookAt = cam.getLookAtDirection();

        for (Triangle3D tri : mesh) {
            List<Triangle3D> clipped = clip(tri, camPos, lookAt);
            for (Triangle3D ct : clipped) {
                Vec3 line1      = ct.v2.sub(ct.v1);
                Vec3 line2      = ct.v3.sub(ct.v1);
                Vec3 surfNormal = cross(line1, line2);

                // Back-face culling
                if (dot(surfNormal, ct.v1.sub(camPos)) >= 0) continue;

                char shade = diffuseLight(light, surfNormal, ct.v1);

                Triangle2D projected = ct
                    .translate(camPos.neg())
                    .rotationY(cam.yaw)
                    .rotationX(cam.pitch)
                    .projection(cam.focalLength)
                    .toScreen(screenWidth, screenHeight);

                putTriangle(projected, shade);
            }
        }
    }

    // =========================================================
    //  CHARGEMENT OBJ
    // =========================================================

    static List<Triangle3D> loadObj(String path) throws IOException {
        List<Vec3>       vertices  = new ArrayList<>();
        List<int[]>      faces     = new ArrayList<>();
        List<String>     lines     = Files.readAllLines(Paths.get(path));

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            if (parts[0].equals("v") && parts.length >= 4) {
                vertices.add(new Vec3(
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3])
                ));
            } else if (parts[0].equals("f")) {
                // Supporte "f 1 2 3", "f 1/2/3 ..." (on prend seulement l'index sommet)
                int[] face = new int[parts.length - 1];
                for (int i = 1; i < parts.length; i++) {
                    face[i-1] = Integer.parseInt(parts[i].split("/")[0]);
                }
                faces.add(face);
            }
            // On ignore mtllib, o, usemtl, vn, vt, etc.
        }

        List<Triangle3D> triangles = new ArrayList<>();
        for (int[] f : faces) {
            if (f.length == 3) {
                triangles.add(new Triangle3D(
                    vertices.get(f[0]-1),
                    vertices.get(f[1]-1),
                    vertices.get(f[2]-1)
                ));
            } else if (f.length >= 4) {
                // Quad → 2 triangles (fan triangulation)
                triangles.add(new Triangle3D(
                    vertices.get(f[0]-1),
                    vertices.get(f[1]-1),
                    vertices.get(f[2]-1)
                ));
                triangles.add(new Triangle3D(
                    vertices.get(f[2]-1),
                    vertices.get(f[3]-1),
                    vertices.get(f[0]-1)
                ));
            }
        }
        return triangles;
    }

    // =========================================================
    //  BOUCLE PRINCIPALE
    // =========================================================

    public static void main(String[] args) throws Exception {
        String objPath = (args.length > 0) ? args[0] : "cube.obj";

        // Lecture taille terminal (COLUMNS / LINES, ou valeur par défaut)
        int w = getTerminalWidth();
        int h = getTerminalHeight() - 1;
        initBuffer(w, h);

        Camera     cam   = new Camera(new Vec3(0, 0, 0), 0.0, -2.0, 1.0);
        LightSource light = new LightSource(new Vec3(0, 20, 0));
        List<Triangle3D> mesh = loadObj(objPath);

        // Passe le terminal en mode raw via stty pour lire les touches sans [Entrée]
        enterRawMode();

        // Cache le curseur
        System.out.print("\033[?25l");
        System.out.flush();

        // Ensemble des touches actuellement pressées
        Set<Integer> keysDown = new HashSet<>();

        // Thread de lecture clavier non-bloquant
        Thread keyThread = new Thread(() -> {
            try {
                InputStream in = System.in;
                while (!Thread.currentThread().isInterrupted()) {
                    if (in.available() > 0) {
                        int key = in.read();
                        if (key == 27) { // ESC ou séquence d'échappement (flèches)
                            if (in.available() > 0) {
                                int next = in.read();
                                if (next == '[' && in.available() > 0) {
                                    int arrow = in.read();
                                    // Flèches : A=haut B=bas C=droite D=gauche
                                    keysDown.add(1000 + arrow);
                                }
                            } else {
                                keysDown.add(27); // ESC seul → quitter
                            }
                        } else {
                            keysDown.add(key);
                        }
                    } else {
                        keysDown.clear();
                        Thread.sleep(5);
                    }
                }
            } catch (Exception ignored) {}
        });
        keyThread.setDaemon(true);
        keyThread.start();

        // ── Boucle de rendu ──────────────────────────────────────────
        long last = System.nanoTime();
        StringBuilder sb = new StringBuilder(w * h + h);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            exitRawMode();
            System.out.print("\033[?25h"); // réaffiche curseur
            System.out.flush();
        }));

        while (!keysDown.contains(27)) {
            long now = System.nanoTime();
            double dt = (now - last) / 1_000_000.0; // ms
            last = now;

            double speed = 0.01 * dt;

            // ── Entrées ────────────────────────────────────────────
            if (keysDown.contains(1000 + 'B') && cam.pitch > -1.57) cam.pitch -= speed;
            if (keysDown.contains(1000 + 'A') && cam.pitch <  1.57) cam.pitch += speed;
            if (keysDown.contains(1000 + 'D')) cam.yaw += speed;
            if (keysDown.contains(1000 + 'C')) cam.yaw -= speed;

            if (keysDown.contains((int)'z') || keysDown.contains((int)'Z'))
                cam.position = cam.position.add(cam.getForwardDirection().mul(speed));
            if (keysDown.contains((int)'s') || keysDown.contains((int)'S'))
                cam.position = cam.position.add(cam.getForwardDirection().mul(-speed));
            if (keysDown.contains((int)'d') || keysDown.contains((int)'D'))
                cam.position = cam.position.add(cam.getRightDirection().mul(speed));
            if (keysDown.contains((int)'q') || keysDown.contains((int)'Q'))
                cam.position = cam.position.add(cam.getRightDirection().mul(-speed));
            if (keysDown.contains((int)' '))
                cam.position.y += speed;
            // Shift = 0 (certains terminaux envoient des codes différents)
            if (keysDown.contains((int)'\r') || keysDown.contains(14))
                cam.position.y -= speed;

            // ── Rendu ──────────────────────────────────────────────
            clearBuffer(' ');
            putMesh(mesh, cam, light);

            // Construction de la chaîne à afficher
            sb.setLength(0);
            sb.append("\033[H"); // curseur en haut à gauche
            for (int row = 0; row < h; row++) {
                sb.append(pixelBuffer, row * w, row * w + w);
                if (row < h - 1) sb.append('\n');
            }

            System.out.print(sb);
            System.out.flush();

            // ── Limite FPS (≈ 60 FPS) ──────────────────────────────
            long elapsed = System.nanoTime() - now;
            long target  = 16_666_666L; // 16.67 ms
            if (elapsed < target) {
                Thread.sleep((target - elapsed) / 1_000_000L);
            }
        }

        keyThread.interrupt();
        exitRawMode();
        System.out.print("\033[?25h\033[2J\033[H");
        System.out.flush();
        System.out.println("Au revoir !");
    }

    // =========================================================
    //  MODE RAW TERMINAL (Unix/Linux/macOS)
    // =========================================================

    static String savedSttyState = null;

    static void enterRawMode() {
        try {
            // Sauvegarde l'état courant
            Process save = Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty -g < /dev/tty"});
            savedSttyState = new String(save.getInputStream().readAllBytes()).trim();
            // Passe en raw : pas d'écho, lecture caractère par caractère
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty raw -echo < /dev/tty"}).waitFor();
        } catch (Exception e) {
            System.err.println("Impossible de passer en mode raw : " + e.getMessage());
        }
    }

    static void exitRawMode() {
        try {
            if (savedSttyState != null) {
                Runtime.getRuntime()
                    .exec(new String[]{"sh", "-c", "stty " + savedSttyState + " < /dev/tty"})
                    .waitFor();
            }
        } catch (Exception ignored) {}
    }

    static int getTerminalWidth() {
        try {
            String s = System.getenv("COLUMNS");
            if (s != null) return Integer.parseInt(s.trim());
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "tput cols"});
            String r = new String(p.getInputStream().readAllBytes()).trim();
            return Integer.parseInt(r);
        } catch (Exception e) { return 120; }
    }

    static int getTerminalHeight() {
        try {
            String s = System.getenv("LINES");
            if (s != null) return Integer.parseInt(s.trim());
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "tput lines"});
            String r = new String(p.getInputStream().readAllBytes()).trim();
            return Integer.parseInt(r);
        } catch (Exception e) { return 30; }
    }
}

/*
 * ═══════════════════════════════════════════════════════════════
 *  README – Compilation & Exécution
 * ═══════════════════════════════════════════════════════════════
 *
 *  Pré-requis : JDK 11+ (testé JDK 17/21), terminal Unix/Linux/macOS.
 *               Fonctionne aussi sous Windows avec WSL.
 *
 *  1. Placer Engine3D.java et cube.obj dans le même dossier.
 *
 *  2. Compiler :
 *       javac Engine3D.java
 *
 *  3. Lancer :
 *       java Engine3D cube.obj
 *
 *     Ou avec un autre fichier .obj :
 *       java Engine3D monModele.obj
 *
 *  Contrôles clavier :
 *    Z / S            → avancer / reculer
 *    Q / D            → strafe gauche / droite
 *    Flèche Haut/Bas  → inclinaison verticale
 *    Flèche G/D       → rotation horizontale
 *    ESPACE           → monter
 *    ECHAP            → quitter
 *
 *  Notes :
 *  - Le moteur utilise stty pour le mode raw (lecture immédiate des touches).
 *    Aucune dépendance externe n'est nécessaire.
 *  - L'algorithme du peintre (painter's algorithm) trie les triangles par
 *    distance à la caméra avant le rendu, comme dans l'original Python.
 *  - Le clipping near-plane évite la division par zéro lors de la projection.
 * ═══════════════════════════════════════════════════════════════
 */
