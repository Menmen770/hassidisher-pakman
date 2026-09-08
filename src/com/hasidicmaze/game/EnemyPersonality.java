package com.hasidicmaze.game;

/**
 * Enemy personalities (classic Pac-Man chase logic), named by color.
 * <ul>
 *   <li>{@link #RED} — always targets the hero</li>
 *   <li>{@link #PINK} — ambushes a few tiles ahead of the hero</li>
 *   <li>{@link #BLUE} — targets from Red's position through a point ahead of the hero</li>
 *   <li>{@link #ORANGE} — chases when far, flees to corner when close</li>
 * </ul>
 */
public enum EnemyPersonality {
    RED("Red"),
    PINK("Pink"),
    BLUE("Blue"),
    ORANGE("Orange");

    private final String displayName;

    EnemyPersonality(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
