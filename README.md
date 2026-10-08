# Moteur 3D ASCII dans le Terminal

Un moteur de rendu 3D temps réel écrit en Java qui affiche des modèles 3D au format Wavefront `.obj` dans un terminal avec ombrage et éclairage par caractères ASCII.

## Préréquis
- **Java JDK 21** (ou version >= 17)

## Compilation et Lancement

### Method 1 : Compilation manuelle avec `javac`
1. Créer le dossier pour les fichiers compilés (si inexistant) :
   ```bash
   mkdir -p bin
   ```
2. Compiler le projet :
   ```bash
   javac -d bin src/math_lib/*.java src/rendue_graphique/*.java src/entrer/*.java
   ```
3. Lancer l'application :
   ```bash
   java -cp bin entrer.Main
   ```

### Method 2 : Lancement avec le fichier JAR existant
```bash
java -cp m.jar entrer.Main
```

## Commandes et Contrôles
Au démarrage, choisissez le modèle 3D à afficher dans la console (Cube, Donut, Car, Humain, etc.). Une petite fenêtre Swing s'ouvrira pour capturer les entrées clavier :
- **Flèches Directionnelles** : Orienter l'objet (Pitch / Yaw).
- **Z / S** : Avancer / Reculer (Profondeur Z).
- **Q / D** : Déplacer à gauche / à droite (Axe X).
- **Espace / B** : Déplacer vers le haut / le bas (Axe Y).

## Modèles supportés
Le moteur charge les fichiers `.obj` situés dans `src/entrer/` :
- `cube.obj`
- `denete.obj` (Donut / Torus)
- `car1.obj`
- `humain.obj`
- `plan.obj`
