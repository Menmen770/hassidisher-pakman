package com.hasidicmaze.assets;

import java.awt.Image;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * Loads game images from {@code assets/} — backgrounds, characters, ui.
 */
public final class AssetManager {
    private static AssetManager instance;

    public static final String BG_IYUNA = "רקע מבוך עיונא.png";
    public static final String BG_GIRSA = "רקע מבוך גירסא.png";

    private static final String DIR_BACKGROUNDS = "backgrounds";
    private static final String DIR_CHARACTERS = "characters";
    private static final String DIR_UI = "ui";

    /** Default menu / fallback backdrop. */
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
        background = loadBackgroundFile(BG_IYUNA);
        if (background != null) {
            backgroundCache.put(BG_IYUNA, background);
        }
        wall = loadUi("wall.png");
        floor = loadUi("floor.png");
        heart = loadUi("heart.png");
        heroUp = loadCharacter("pacmanUp.png");
        heroDown = loadCharacter("pacmanDown.png");
        heroLeft = loadCharacter("pacmanLeft.png");
        heroRight = loadCharacter("pacmanRight.png");
        blueEnemy = loadCharacter("blueGhost.png");
        orangeEnemy = loadCharacter("orangeGhost.png");
        pinkEnemy = loadCharacter("pinkGhost.png");
        redEnemy = loadCharacter("redGhost.png");
        cherry = loadUi("cherry.png");
        powerFood = loadUi("powerFood.png");
    }

    public static synchronized AssetManager get() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    /** Force-reload singleton (e.g. after replacing a background file). */
    public static synchronized void reset() {
        instance = null;
    }

    /** Same-size stage backdrop by file name under {@code assets/backgrounds/}. */
    public Image backgroundFor(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return background;
        }
        return backgroundCache.computeIfAbsent(fileName, AssetManager::loadBackgroundFile);
    }

    private static Image loadBackgroundFile(String fileName) {
        return loadFrom("assets", DIR_BACKGROUNDS, fileName);
    }

    private static Image loadCharacter(String fileName) {
        return loadFrom("assets", DIR_CHARACTERS, fileName);
    }

    private static Image loadUi(String fileName) {
        return loadFrom("assets", DIR_UI, fileName);
    }

    private static Image loadFrom(String... parts) {
        Path path = Paths.get(parts[0], java.util.Arrays.copyOfRange(parts, 1, parts.length));
        Path[] candidates = {
            path,
            Paths.get(System.getProperty("user.dir")).resolve(path)
        };
        for (Path candidate : candidates) {
            File file = candidate.toFile();
            if (file.isFile()) {
                return new ImageIcon(file.getAbsolutePath()).getImage();
            }
        }
        System.err.println("Missing asset: " + path);
        return null;
    }
}
