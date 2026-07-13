package com.hasidicmaze.score;

import java.util.Objects;

public final class HighScore implements Comparable<HighScore> {
    private final String name;
    private final int score;
    private final String mapId;
    private final long timestamp;

    public HighScore(String name, int score, String mapId, long timestamp) {
        this.name = Objects.requireNonNull(name);
        this.score = score;
        this.mapId = Objects.requireNonNull(mapId);
        this.timestamp = timestamp;
    }

    public String getName() { return name; }
    public int getScore() { return score; }
    public String getMapId() { return mapId; }
    public long getTimestamp() { return timestamp; }

    @Override
    public int compareTo(HighScore other) {
        int byScore = Integer.compare(other.score, this.score);
        if (byScore != 0) {
            return byScore;
        }
        return Long.compare(other.timestamp, this.timestamp);
    }
}
