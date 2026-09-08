package com.hasidicmaze.assets;

import com.hasidicmaze.AppPaths;
import java.awt.Image;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * Loads game images from {@code assets/} — backgrounds, characters, ui.
 */
public final class AssetManager {
    private static AssetManager instance;

    public static final String BG_IYUNA = "maze-bg-iyuna.png";
    public static final String BG_GIRSA = "maze-bg-girsa.png";
    public static final String BG_SHLISHI = "1234567.png";
    public static final String BG_REVIYI = "maze-bg-reviyi.png";

    private static final String DIR_BACKGROUNDS = "backgrounds";
    private static final String DIR_CHARACTERS = "characters";
    private static final String DIR_UI = "ui";

    /** Default menu / fallback backdrop. */
    public final Image background;
    public final Image wall;
    public final Image heart;
    public final Image heroUp;
    public final Image heroDown;
    public final Image heroLeft;
    public final Image heroRight;
    public final Image blueEnemy;
    public final Image orangeEnemy;
    public final Image pinkEnemy;
    public final Image redEnemy;
    public final Image scaredEnemy;
    public final Image scaredEnemyFlash;
    public final Image ghostEyes;
    public final Image coffee;
    /** Stage bonuses: book1, book2, tefillin bag (13), hat (14). */
    public final Image book1;
    public final Image book2;
    public final Image tefillinBag;
    public final Image hat;
    /** Full-screen main menu art (logo baked into BG.png). */
    public final Image menuBackground;
    /** Window / taskbar / app icon. */
    public final Image appIcon;

    private final Map<String, Image> backgroundCache = new HashMap<>();

    private AssetManager() {
        background = loadBackgroundFile(BG_IYUNA);
        if (background != null) {
            backgroundCache.put(BG_IYUNA, background);
        }
        wall = loadUi("wall.png");
        heart = loadUi("heart.png");
        heroUp = loadCharacter("pacmanUp.png");
        heroDown = loadCharacter("pacmanDown.png");
        heroLeft = loadCharacter("pacmanLeft.png");
        heroRight = loadCharacter("pacmanRight.png");
        blueEnemy = loadCharacter("blueGhost.png");
        orangeEnemy = loadCharacter("orangeGhost.png");
        pinkEnemy = loadCharacter("pinkGhost.png");
        redEnemy = loadCharacter("redGhost.png");
        scaredEnemy = loadCharacter("scaredGhost.png");
        scaredEnemyFlash = loadCharacter("scaredGhost2.png");
        ghostEyes = loadCharacter("ghostEyes.png");
        coffee = loadCharacter("coffee.png");
        book1 = loadUi("book1.png");
        book2 = loadUi("book2.png");
        tefillinBag = loadUi("tefillin.png");
        hat = loadUi("hat.png");
        menuBackground = loadBackgroundFile("BG.png");
        // PNG — OpenJDK does not reliably decode .ico for window icons
        appIcon = loadUi("logo_ico.png");
    }

    /** One collectible per stage: שחרית→תפילין, עיונא→ספר1, גירסא→ספר2, ערב→כובע. */
    public Image bonusForStage(int stageIndex) {
        return switch (Math.floorMod(stageIndex, 4)) {
            case 0 -> tefillinBag;
            case 1 -> book1;
            case 2 -> book2;
            default -> hat;
        };
    }

    /** Points for the stage collectible. */
    public int bonusPointsForStage(int stageIndex) {
        return switch (Math.floorMod(stageIndex, 4)) {
            case 0 -> 500;
            case 1 -> 100;
            case 2 -> 300;
            default -> 700;
        };
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
        Path path = AppPaths.resolve(parts[0], java.util.Arrays.copyOfRange(parts, 1, parts.length));
        File file = path.toFile();
        if (file.isFile()) {
            return new ImageIcon(file.getAbsolutePath()).getImage();
        }
        System.err.println("Missing asset: " + path);
        return null;
    }
}
