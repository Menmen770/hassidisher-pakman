package com.hasidicmaze.game;

import java.awt.Image;

/** Movable / drawable entity on the board. */
public class Entity {
    public int x;
    public int y;
    public int width;
    public int height;
    public Image image;
    public final int startX;
    public final int startY;
    public char direction = 'U';
    public int velocityX;
    public int velocityY;

    /** Non-null only for maze enemies. */
    public EnemyPersonality personality;
    /** Last tile center used for AI decisions (-1 = unset). */
    public int tileRow = -1;
    public int tileCol = -1;
    /** Orange flee hysteresis — avoids dancing at the 8-tile boundary. */
    public boolean orangeFleeing;
    /** Large corner pellet — triggers frightened ghosts. */
    public boolean powerPellet;
    /** Bonus collectible (book / tefillin / hat). */
    public boolean bonusItem;
    /** Base (non-scared) sprite for enemies. */
    public Image normalImage;
    /** True while vulnerable after a power pellet. */
    public boolean frightened;
    /** Already eaten during the current power-pellet period — don't re-frighten on respawn. */
    public boolean eatenThisFright;
    /** Eyes-only retreat to spawn after being eaten. */
    public boolean returningHome;
    /** While > 0, freeze and show earned points instead of the ghost. */
    public int eatenScoreTicks;
    /** Tiles walked since last full BFS chase recalc. */
    public int tilesSincePathRecalc;

    public Entity(Image image, int x, int y, int width, int height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.startX = x;
        this.startY = y;
    }

    public void reset() {
        x = startX;
        y = startY;
        velocityX = 0;
        velocityY = 0;
        tileRow = -1;
        tileCol = -1;
        orangeFleeing = false;
        frightened = false;
        eatenThisFright = false;
        returningHome = false;
        eatenScoreTicks = 0;
        tilesSincePathRecalc = 999;
        if (normalImage != null) {
            image = normalImage;
        }
    }
}
