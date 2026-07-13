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
    }
}
