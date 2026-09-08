package com.hasidicmaze.game;

/** Temporary points popup after eating a ghost or bonus. */
public final class FloatingScore {
    int x;
    int y;
    final int points;
    int ticksLeft;

    FloatingScore(int x, int y, int points, int ticksLeft) {
        this.x = x;
        this.y = y;
        this.points = points;
        this.ticksLeft = ticksLeft;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getPoints() { return points; }
    public int getTicksLeft() { return ticksLeft; }
}
