package com.hasidicmaze.map;

import java.util.ArrayDeque;
import java.util.Objects;

/**
 * Immutable tile map definition with connectivity validation.
 * Legend: X wall | space food | O empty path | P player | b o p r enemies
 */
public final class GameMap {
    private final String id;
    private final String title;
    private final String subtitle;
    private final String difficulty;
    private final String[] tiles;

    public GameMap(String id, String title, String subtitle, String difficulty, String[] tiles) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.subtitle = Objects.requireNonNull(subtitle);
        this.difficulty = Objects.requireNonNull(difficulty);
        this.tiles = tiles.clone();
        validate();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getDifficulty() { return difficulty; }
    public String[] getTiles() { return tiles.clone(); }
    public int getRows() { return tiles.length; }
    public int getCols() { return tiles[0].length(); }

    public char charAt(int row, int col) {
        return tiles[row].charAt(col);
    }

    public boolean isWalkable(int row, int col) {
        if (row < 0 || col < 0 || row >= getRows() || col >= getCols()) {
            return false;
        }
        return tiles[row].charAt(col) != 'X';
    }

    private void validate() {
        if (tiles.length == 0) {
            throw new IllegalArgumentException(id + ": empty map");
        }
        int cols = tiles[0].length();
        int playerCount = 0;
        int playerR = -1;
        int playerC = -1;

        for (int r = 0; r < tiles.length; r++) {
            if (tiles[r].length() != cols) {
                throw new IllegalArgumentException(id + ": jagged row " + r);
            }
            for (int c = 0; c < cols; c++) {
                char ch = tiles[r].charAt(c);
                if (ch == 'P') {
                    playerCount++;
                    playerR = r;
                    playerC = c;
                }
            }
        }
        if (playerCount != 1) {
            throw new IllegalArgumentException(id + ": expected 1 player, found " + playerCount);
        }

        boolean[][] visited = new boolean[tiles.length][cols];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{playerR, playerC});
        visited[playerR][playerC] = true;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int rows = tiles.length;

        while (!queue.isEmpty()) {
            int[] cur = queue.removeFirst();
            for (int[] d : dirs) {
                int nr = cur[0] + d[0];
                int nc = cur[1] + d[1];
                // Wrap-around tunnels (open edges connect opposite sides)
                if (nr < 0) {
                    nr = rows - 1;
                } else if (nr >= rows) {
                    nr = 0;
                }
                if (nc < 0) {
                    nc = cols - 1;
                } else if (nc >= cols) {
                    nc = 0;
                }
                if (!visited[nr][nc] && tiles[nr].charAt(nc) != 'X') {
                    visited[nr][nc] = true;
                    queue.add(new int[]{nr, nc});
                }
            }
        }

        for (int r = 0; r < tiles.length; r++) {
            for (int c = 0; c < cols; c++) {
                char ch = tiles[r].charAt(c);
                if (ch != 'X' && !visited[r][c]) {
                    throw new IllegalArgumentException(
                        id + ": unreachable cell at (" + r + "," + c + ")='" + ch + "'");
                }
            }
        }
    }
}
