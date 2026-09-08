package com.hasidicmaze.score;

import java.util.Objects;

public final class HighScore implements Comparable<HighScore> {
    private final String name;
    private final int score;

    public HighScore(String name, int score) {
        this.name = Objects.requireNonNull(name);
        this.score = score;
    }

    public String getName() { return name; }
    public int getScore() { return score; }

    @Override
    public int compareTo(HighScore other) {
        int byScore = Integer.compare(other.score, this.score);
        if (byScore != 0) {
            return byScore;
        }
        return name.compareToIgnoreCase(other.name);
    }
}
