package com.hasidicmaze.assets;

import java.awt.Image;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * Loads game images. Map backgrounds are same-size files in the project root (bg.png, bg2.png, …).
 */
public final class AssetManager {
    private static AssetManager instance;

    /** Default menu / first-stage backdrop. */
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

    private final Map<String, Image> backgroundCache = new HashMap<>();

    private AssetManager() {
        background = loadBackgroundFile("bg.png");
        if (background != null) {
            backgroundCache.put("bg.png", background);
        }
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

    /** Same-size stage backdrop by file name (bg.png, bg2.png, …). */
    public Image backgroundFor(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return background;
        }
        return backgroundCache.computeIfAbsent(fileName, AssetManager::loadBackgroundFile);
    }

    private static Image loadBackgroundFile(String fileName) {
        Path[] candidates = {
            Paths.get(fileName),
            Paths.get(System.getProperty("user.dir"), fileName),
            Paths.get("assets", fileName)
        };
        for (Path path : candidates) {
            File file = path.toFile();
            if (file.isFile()) {
                System.out.println("Background: " + file.getAbsolutePath());
                return new ImageIcon(file.getAbsolutePath()).getImage();
            }
        }
        System.err.println("Missing background: " + fileName);
        return null;
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
