# Hassidisher Pakman

A Pac-Man–style maze game built with **Java Swing**, featuring Hebrew UI, custom bookshelf backgrounds, multiple map stages, and a high-score board.

---

## Overview

Players collect pellets while avoiding enemies across themed mazes (“סדרים”). The game keeps a fixed window size across all screens, scales the board when resized, and locks movement to the tile grid so gameplay stays aligned with the background art.

---

## Architecture

```
src/com/hasidicmaze/
├── App.java                 Entry point
├── MainFrame.java           Window + CardLayout navigation
├── Theme.java               Colors, fonts, shared window size
├── assets/
│   └── AssetManager.java    Loads images from assets/
├── game/
│   ├── GamePanel.java       Game loop, rendering, input, HUD
│   └── Entity.java          Player / enemy / wall / food entities
├── map/
│   ├── GameMap.java         Immutable map definition
│   └── MapCatalog.java      Stage list (Iyuna, Girsa, locked slots)
├── score/
│   ├── HighScore.java       Score entry model
│   └── HighScoreManager.java  Top-10 persistence
└── ui/
    ├── MenuPanel.java
    ├── MapSelectPanel.java
    ├── HighScorePanel.java
    ├── NewRecordPanel.java
    ├── AtmospherePanel.java
    └── StyledButton.java

assets/
├── backgrounds/             Stage backdrops (same pixel size)
├── characters/              Player & ghost sprites
└── ui/                      Hearts, walls, pickups, etc.

data/
└── scores.txt               Saved high scores
```

### How it fits together

| Layer | Role |
|--------|------|
| **MainFrame** | Owns one window; switches MENU → MAP_SELECT → GAME / HIGH_SCORES / NEW_RECORD |
| **MapCatalog** | Defines each stage (tiles, difficulty, background file, locked flag) |
| **GamePanel** | Runs the timer loop, collisions, scoring, and paints board + side HUD |
| **AssetManager** | Single place for image loading (`assets/…`) |
| **HighScoreManager** | Reads/writes `data/scores.txt` |

Gameplay math (grid origin, tile size, collisions) lives in **GamePanel** and is independent of window chrome — resizing only scales the finished canvas.

---

## Run

**Requirements:** JDK 17+ (project targets a modern Java release)

```bash
# Compile
javac -encoding UTF-8 -d bin src/com/hasidicmaze/*.java src/com/hasidicmaze/*/*.java

# Launch (from project root so assets/ and data/ resolve)
java -cp bin com.hasidicmaze.App
```

Or use `run.bat` / the VS Code launch configuration **Run Hassidisher Pakman**.

---

## Controls

| Key | Action |
|-----|--------|
| Arrow keys | Move |
| ESC | Quit to menu |
| Enter | Confirm (game over) |

---

## Credits

Special thanks to the original Pac-Man Java tutorial this project grew from:

- [Kenny Yip Coding — Pacman in Java](https://youtu.be/lB_J-VNMVpE)

The maze art, Hebrew menus, multi-stage flow, and UI redesign are original to Hassidisher Pakman.

—

**menmen770**

---

## License

Private project © menmen770
