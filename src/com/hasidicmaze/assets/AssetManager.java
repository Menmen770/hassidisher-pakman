package com.hasidicmaze.assets;

import java.awt.Image;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.ImageIcon;

/**
 * Loads game images. Background is ALWAYS project-root bg.png — nothing else.
 */
public final class AssetManager {
    private static AssetManager instance;

    public final Image background;
    public final Image wall;
    public final Image floor;
    public final Image heart;
    public final Image heroUp;
    public final Image heroDown;
    public final Image heroLeft;
    public final Image heroRight;
    public final Image blueEnemy;
    public final Image orangeEnemy;
    public final Image pinkEnemy;
    public final Image redEnemy;
    public final Image cherry;
    public final Image powerFood;

    private AssetManager() {
        background = loadBackgroundOnly();
        wall = load("wall.png");
        floor = load("floor.png");
        heart = load("heart.png");
        heroUp = load("pacmanUp.png");
        heroDown = load("pacmanDown.png");
        heroLeft = load("pacmanLeft.png");
        heroRight = load("pacmanRight.png");
        blueEnemy = load("blueGhost.png");
        orangeEnemy = load("orangeGhost.png");
        pinkEnemy = load("pinkGhost.png");
        redEnemy = load("redGhost.png");
        cherry = load("cherry.png");
        powerFood = load("powerFood.png");
    }

    public static synchronized AssetManager get() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    /** Force-reload singleton (e.g. after replacing bg.png). */
    public static synchronized void reset() {
        instance = null;
    }

    /** Only the root bg.png file — never assets/ or classpath copies. */
    private static Image loadBackgroundOnly() {
        File file = new File("bg.png");
        if (!file.isFile()) {
            file = new File(System.getProperty("user.dir"), "bg.png");
        }
        if (!file.isFile()) {
            System.err.println("Missing required file: bg.png in project root");
            return null;
        }
        System.out.println("Background: " + file.getAbsolutePath());
        return new ImageIcon(file.getAbsolutePath()).getImage();
    }

    private static Image load(String fileName) {
        Path[] candidates = {
            Paths.get(fileName),
            Paths.get("assets", fileName)
        };
        for (Path path : candidates) {
            File file = path.toFile();
            if (file.isFile()) {
                return new ImageIcon(file.getAbsolutePath()).getImage();
            }
        }
        System.err.println("Missing asset: " + fileName);
        return null;
    }
}
