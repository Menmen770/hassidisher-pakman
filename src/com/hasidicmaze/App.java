package com.hasidicmaze;

import com.hasidicmaze.assets.AssetManager;

/**
 * Entry point for Hasidic Maze.
 */
public class App {
    public static void main(String[] args) {
        AssetManager.reset(); // always pick up current root bg.png
        MainFrame.launch();
    }
}
