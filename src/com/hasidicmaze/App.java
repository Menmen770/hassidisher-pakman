package com.hasidicmaze;

import com.hasidicmaze.assets.AssetManager;

/**
 * Entry point for pac-mandy.
 */
public class App {
    public static void main(String[] args) {
        AssetManager.reset(); // always pick up current assets/backgrounds
        MainFrame.launch();
    }
}
