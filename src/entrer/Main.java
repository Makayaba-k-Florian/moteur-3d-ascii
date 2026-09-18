package entrer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import math_lib.*;
import rendue_graphique.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static boolean[] keys = new boolean[256];

    public static void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame();
        frame.setSize(100, 100);
        frame.setVisible(true);
        frame.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) { if(e.getKeyCode() < 256) keys[e.getKeyCode()] = true; }
            public void keyReleased(KeyEvent e) { if(e.getKeyCode() < 256) keys[e.getKeyCode()] = false; }
            public void keyTyped(KeyEvent e) {}
        });

        Rendue render = new Rendue();
        System.out.print("\033[2J"); 
        
     // Dans ton main, remplace la création de la pyramide/cube par :
       

        //List<Triangle3d> cubeMesh = ObjLoader.loadObj("cube.obj");
        //List<Triangle3d> cubeMesh = ObjLoader.loadObj("car1.obj");
        
        
	     // Inside your main method
	     // 1. Load the car mesh
        /*List<Triangle3d> carMesh = ObjLoader.loadObj("denete.obj");
        System.out.println("Triangles chargés : " + carMesh.size());*/
        
        Scanner sc = new Scanner(System.in);
        List<Triangle3d> mesh = new ArrayList<>();
        List<Triangle3d> humainMesh = new ArrayList<>();

        System.out.println("=== 3D TERMINAL ENGINE ===");
        System.out.println("Choose a model to load:");
        System.out.println("1 : Cube");
        System.out.println("2 : Donut ");
        System.out.println("3 : Car");
        System.out.println("4 : Humain");
        System.out.println("5 : plan");
        System.out.println("6 : humain rotation");
        System.out.print("Choice : ");

        int choice = sc.nextInt();

        // Loading the chosen file
        if (choice == 1) {
            mesh = ObjLoader.loadObj("cube.obj");
        } else if (choice == 2) {
            mesh = ObjLoader.loadObj("denete.obj");
        } else if (choice == 3) {
            mesh = ObjLoader.loadObj("car1.obj");
        }else if (choice == 4) {
            mesh = ObjLoader.loadObj("humain.obj");
        } else if (choice == 5) {
            mesh = ObjLoader.loadObj("plan.obj");
        } else if (choice == 6) {
        	humainMesh= ObjLoader.loadObj("humain.obj");
        }else {
            System.out.println("Invalid choice, loading default pyramid.");
            mesh = ObjLoader.loadObj("denete.obj");
        }

        // --- Start the 3D loop below ---
       



            
       /* // 1. DÉFINITION DE LA GÉOMÉTRIE ORIGINALE (Le modèle)
        double s = 1.0; 
        double h = 2.0; 
        Point3d top = new Point3d(0, h, 0);
        Point3d fl = new Point3d(-s, 0, -s);
        Point3d fr = new Point3d(s, 0, -s);
        Point3d bl = new Point3d(-s, 0, s);
        Point3d br = new Point3d(s, 0, s);*/

       /* List<Triangle3d> pyramidMesh = new ArrayList<>();
        pyramidMesh.add(new Triangle3d(fr, top, fl)); 
        pyramidMesh.add(new Triangle3d(fr, top, br)); 
        pyramidMesh.add(new Triangle3d(br, top, bl)); 
        pyramidMesh.add(new Triangle3d(bl, top, fl)); 
        pyramidMesh.add(new Triangle3d(fl, fr, br));  
        pyramidMesh.add(new Triangle3d(fl, br, bl));  

        
     // --- DÉFINITION DU CUBE ---
        double cs = 1.0; // Cube half-size
        Point3d v0 = new Point3d(-cs, -cs, -cs);
        Point3d v1 = new Point3d(cs, -cs, -cs);
        Point3d v2 = new Point3d(cs, cs, -cs);
        Point3d v3 = new Point3d(-cs, cs, -cs);
        Point3d v4 = new Point3d(-cs, -cs, cs);
        Point3d v5 = new Point3d(cs, -cs, cs);
        Point3d v6 = new Point3d(cs, cs, cs);
        Point3d v7 = new Point3d(-cs, cs, cs);

        /*List<Triangle3d> cubeMesh = new ArrayList<>();
        // Front
        cubeMesh.add(new Triangle3d(v4, v5, v6)); cubeMesh.add(new Triangle3d(v4, v6, v7));
        // Back
        cubeMesh.add(new Triangle3d(v1, v0, v3)); cubeMesh.add(new Triangle3d(v1, v3, v2));
        // Left
        cubeMesh.add(new Triangle3d(v0, v4, v7)); cubeMesh.add(new Triangle3d(v0, v7, v3));
        // Right
        cubeMesh.add(new Triangle3d(v5, v1, v2)); cubeMesh.add(new Triangle3d(v5, v2, v6));
        // Top
        cubeMesh.add(new Triangle3d(v3, v2, v6)); cubeMesh.add(new Triangle3d(v3, v6, v7));
        // Bottom
        cubeMesh.add(new Triangle3d(v0, v1, v5)); cubeMesh.add(new Triangle3d(v0, v5, v4));*/

        // 2. VARIABLES DE TRANSFORMATION (L'objet, pas la caméra)
        Point3d objectPos = new Point3d(0, -1, 400); // Position initiale devant nous
        //Point3d humainPos = new Point3d(0, -1, 10);
        double yaw = 0;
        double yawh = 0;
        double pitch = 0;
        
        //LightSource light = new LightSource(new Point3d(10, 10, -10));
     // High up, slightly to the right and behind the camera
        LightSource light = new LightSource(new Point3d(20, 40, -20));

     // In your Main before the while loop:
        //Point3d startPos = new Point3d(0, 2, -10); // Back 10 units, up 2 units
        //Camera cam = new Camera(startPos, 0, 0, 40.0); // focalLength 40 is safe
        
        Point3d cubeOffset = new Point3d(0, 1, 70);
      
        while (true) {
            render.clear(' '); // On vide l'écran et le zBuffer
            
            // 3. INPUTS (On bouge l'objet)
            double speed = 0.3;
            if (keys[KeyEvent.VK_UP])    pitch += 0.05;
            if (keys[KeyEvent.VK_DOWN])  pitch -= 0.05;
            if (keys[KeyEvent.VK_LEFT])  yaw -= 0.05;
            if (keys[KeyEvent.VK_RIGHT]) yaw += 0.05;

            if (keys[KeyEvent.VK_Z] || keys[KeyEvent.VK_W]) objectPos.z += speed;
            if (keys[KeyEvent.VK_S])                         objectPos.z -= speed;
            if (keys[KeyEvent.VK_Q] || keys[KeyEvent.VK_A]) objectPos.x -= speed;
            if (keys[KeyEvent.VK_D])                         objectPos.x += speed;
            if (keys[KeyEvent.VK_SPACE] ) objectPos.y -= speed;
            if (keys[KeyEvent.VK_B])                         objectPos.y += speed;
            
            yawh += 0.05;
            
            // 4. TRANSFORMATION MANUELLE DU MESH
            /*List<Triangle3d> transformedMesh = new ArrayList<>();
            for (Triangle3d tri : pyramidMesh) {
                // On applique Rotation -> Translation
                Triangle3d t = tri.rotationY(yaw).rotationX(pitch).translation(objectPos);
                transformedMesh.add(t);
            }*/
            
            
         // Inside the while(true) loop
            List<Triangle3d> transformedWorld = new ArrayList<>();
            //List<Triangle3d> transformedmesh = new ArrayList<>();

            // Transform Pyramid
           for (Triangle3d tri : humainMesh) {
        	   	
        	   transformedWorld.add(tri.rotationY(yawh).rotationX(pitch).translation(objectPos.add(cubeOffset)));
            }

            // Transform Cube (Placed 5 units to the right of the pyramid)
            
            
           for (Triangle3d tri : mesh) {
                transformedWorld.add(tri.rotationY(yaw).rotationX(pitch).translation(objectPos.add(cubeOffset)));
            }

            // Render the whole world
            


            // 5. RENDU (On passe un mesh déjà transformé)
            // On utilise une "caméra factice" neutre car l'objet est déjà à la bonne place
            Camera fakeCam = new Camera(new Point3d(0,0,0), 0, 0, 40.0);
        
         

            
            render.putMesh(transformedWorld, fakeCam, light);
            
            
            render.draw();
            Thread.sleep(16);
        }
    }
}
