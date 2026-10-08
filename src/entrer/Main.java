package entrer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JFrame;

import math_lib.*;
import rendue_graphique.*;

public class Main {
    private static boolean[] keys = new boolean[256];

    public static void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame("ASCII 3D Engine Control");
        frame.setSize(200, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() < 256) keys[e.getKeyCode()] = true; }
            public void keyReleased(KeyEvent e) { if (e.getKeyCode() < 256) keys[e.getKeyCode()] = false; }
            public void keyTyped(KeyEvent e) {}
        });

        Rendue render = new Rendue();
        System.out.print("\033[2J"); 
        
        Scanner sc = new Scanner(System.in);
        List<Triangle3d> mesh = new ArrayList<>();
        List<Triangle3d> humainMesh = new ArrayList<>();

        System.out.println("=== 3D TERMINAL ENGINE ===");
        System.out.println("Choose a model to load:");
        System.out.println("1 : Cube");
        System.out.println("2 : Donut");
        System.out.println("3 : Car");
        System.out.println("4 : Humain");
        System.out.println("5 : Plan");
        System.out.println("6 : Humain rotation");
        System.out.print("Choice : ");

        int choice = sc.hasNextInt() ? sc.nextInt() : 1;

        // Loading the chosen file
        if (choice == 1) {
            mesh = ObjLoader.loadObj("cube.obj");
        } else if (choice == 2) {
            mesh = ObjLoader.loadObj("denete.obj");
        } else if (choice == 3) {
            mesh = ObjLoader.loadObj("car1.obj");
        } else if (choice == 4) {
            mesh = ObjLoader.loadObj("humain.obj");
        } else if (choice == 5) {
            mesh = ObjLoader.loadObj("plan.obj");
        } else if (choice == 6) {
            humainMesh = ObjLoader.loadObj("humain.obj");
        } else {
            System.out.println("Invalid choice, loading default model (Donut).");
            mesh = ObjLoader.loadObj("denete.obj");
        }

        Point3d objectPos = new Point3d(0, -1, 400); // Position initiale devant nous
        double yaw = 0;
        double yawh = 0;
        double pitch = 0;
        
        LightSource light = new LightSource(new Point3d(20, 40, -20));
        Point3d cubeOffset = new Point3d(0, 1, 70);
      
        System.out.println("\nFocus the small Swing window to use keyboard controls:");
        System.out.println("  Arrows : Rotate Pitch/Yaw");
        System.out.println("  Z / S  : Zoom In / Zoom Out (Z depth)");
        System.out.println("  Q / D  : Move Left / Right (X position)");
        System.out.println("  Space / B : Move Up / Down (Y position)\n");

        while (true) {
            render.clear(' '); // On vide l'écran
            
            // INPUTS
            double speed = 0.3;
            if (keys[KeyEvent.VK_UP])    pitch += 0.05;
            if (keys[KeyEvent.VK_DOWN])  pitch -= 0.05;
            if (keys[KeyEvent.VK_LEFT])  yaw -= 0.05;
            if (keys[KeyEvent.VK_RIGHT]) yaw += 0.05;

            if (keys[KeyEvent.VK_Z] || keys[KeyEvent.VK_W]) objectPos.z += speed;
            if (keys[KeyEvent.VK_S])                         objectPos.z -= speed;
            if (keys[KeyEvent.VK_Q] || keys[KeyEvent.VK_A]) objectPos.x -= speed;
            if (keys[KeyEvent.VK_D])                         objectPos.x += speed;
            if (keys[KeyEvent.VK_SPACE])                     objectPos.y -= speed;
            if (keys[KeyEvent.VK_B])                         objectPos.y += speed;
            
            yawh += 0.05;
            
            List<Triangle3d> transformedWorld = new ArrayList<>();

            for (Triangle3d tri : humainMesh) {
                transformedWorld.add(tri.rotationY(yawh).rotationX(pitch).translation(objectPos.add(cubeOffset)));
            }

            for (Triangle3d tri : mesh) {
                transformedWorld.add(tri.rotationY(yaw).rotationX(pitch).translation(objectPos.add(cubeOffset)));
            }

            Camera fakeCam = new Camera(new Point3d(0, 0, 0), 0, 0, 40.0);
            
            render.putMesh(transformedWorld, fakeCam, light);
            render.draw();

            Thread.sleep(16);
        }
    }
}
