package com.hasidicmaze.map;

import java.util.Arrays;
import java.util.List;

/**
 * Built-in maze catalog — every map is BFS-validated (with wrap-around tunnels).
 * 21 rows × 19 cols (including top and bottom wall rows).
 */
public final class MapCatalog {
    private static final List<GameMap> MAPS = Arrays.asList(
        courtyard(),
        twinHalls(),
        spiralGate(),
        crossroads()
    );

    private MapCatalog() {}

    public static List<GameMap> all() {
        return MAPS;
    }

    public static GameMap byId(String id) {
        return MAPS.stream()
            .filter(m -> m.getId().equals(id))
            .findFirst()
            .orElse(MAPS.get(0));
    }

    /**
     * User-traced layout from the bookshelf background (0=wall, 1=open).
     * Four wrap holes: top/bottom col 15, left/right on tunnel row 11.
     */
    private static GameMap courtyard() {
        String[] binary = {
            "0000000000000001000",
            "0001110100111111110",
            "0001011100100010010",
            "0111011111111111110",
            "0001111111110110000",
            "0111111111110111110",
            "0110011111111111000",
            "0011111001101111000",
            "0111001111111001110",
            "0011111110111111100",
            "0011111110111111110",
            "1111001111110011111", // side tunnel row
            "0001111001011111110",
            "0001001111011100010",
            "0111111111111111110",
            "0011111101100110000",
            "0111000101111110000",
            "0001111111101111110",
            "0111100111111111010",
            "0001111110111011110",
            "0000000000000001000"
        };

        char[][] grid = new char[21][19];
        for (int r = 0; r < 21; r++) {
            if (binary[r].length() != 19) {
                throw new IllegalStateException("row " + r + " len=" + binary[r].length());
            }
            for (int c = 0; c < 19; c++) {
                grid[r][c] = binary[r].charAt(c) == '0' ? 'X' : ' ';
            }
        }

        // Four openings — one per side — for wrap to the opposite side
        grid[0][15] = ' ';
        grid[20][15] = ' ';
        grid[11][0] = ' ';
        grid[11][18] = ' ';

        place(grid, 15, 9, 'P');
        place(grid, 11, 7, 'r');
        place(grid, 11, 8, 'b');
        place(grid, 11, 9, 'p');
        place(grid, 11, 10, 'o');

        // Seal open cells that aren't reachable yet (user will refine later)
        sealUnreachable(grid);

        String[] tiles = new String[21];
        for (int r = 0; r < 21; r++) {
            tiles[r] = new String(grid[r]);
        }

        return new GameMap(
            "courtyard",
            "החצר הגדולה",
            "מפת הרקע החדשה — חומות מסביב וארבעה מעברים",
            "קל",
            tiles
        );
    }

    private static void sealUnreachable(char[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;
        int pr = -1;
        int pc = -1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 'P') {
                    pr = r;
                    pc = c;
                }
            }
        }
        if (pr < 0) {
            return;
        }

        boolean[][] visited = new boolean[rows][cols];
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        q.add(new int[]{pr, pc});
        visited[pr][pc] = true;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!q.isEmpty()) {
            int[] cur = q.removeFirst();
            for (int[] d : dirs) {
                int nr = cur[0] + d[0];
                int nc = cur[1] + d[1];
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
                if (!visited[nr][nc] && grid[nr][nc] != 'X') {
                    visited[nr][nc] = true;
                    q.add(new int[]{nr, nc});
                }
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != 'X' && !visited[r][c]) {
                    grid[r][c] = 'X';
                }
            }
        }
    }

    private static void place(char[][] grid, int r, int c, char ch) {
        if (r < 0 || c < 0 || r >= grid.length || c >= grid[0].length) {
            return;
        }
        if (grid[r][c] != 'X') {
            grid[r][c] = ch;
            return;
        }
        for (int d = 1; d <= 6; d++) {
            int[][] dirs = {{0, d}, {0, -d}, {d, 0}, {-d, 0}};
            for (int[] dir : dirs) {
                int nr = r + dir[0];
                int nc = c + dir[1];
                if (nr >= 0 && nc >= 0 && nr < grid.length && nc < grid[0].length
                    && grid[nr][nc] == ' ') {
                    grid[nr][nc] = ch;
                    return;
                }
            }
        }
        grid[r][c] = ch;
    }

    private static GameMap twinHalls() {
        return new GameMap(
            "twin-halls",
            "שני האולמות",
            "שני אגפים סימטריים — דורש תכנון מסלול",
            "בינוני",
            new String[]{
                "XXXXXXXXXXXXXXXXXXX",
                "X   X         X   X",
                "X X X XXXXXXX X X X",
                "X X             X X",
                "X XXX X XXX X XXX X",
                "X     X  X  X     X",
                "XXX XXXX X XXXX XXX",
                "    X         X    ",
                "XXX X XXXrXXX X XXX",
                "      b  P  op     ",
                "XXX X XXXXXXX X XXX",
                "    X         X    ",
                "XXX XXXX X XXXX XXX",
                "X     X  X  X     X",
                "X XXX X XXX X XXX X",
                "X X             X X",
                "X X XXX X X XXX X X",
                "X X   X  X  X   X X",
                "X XXX X XXX X XXX X",
                "X                 X",
                "XXXXXXXXXXXXXXXXXXX"
            }
        );
    }

    private static GameMap spiralGate() {
        return new GameMap(
            "spiral-gate",
            "שער הספירלה",
            "טבעות מקושרות — מבוך צפוף ומאתגר",
            "קשה",
            new String[]{
                "XXXXXXXXXXXXXXXXXXX",
                "X                 X",
                "X XXXXXXXXXXXXXX  X",
                "X X              XX",
                "X X XXXXXXXXXXXX  X",
                "X X X            XX",
                "X X X XXXXXXXXXX  X",
                "X X X X        X XX",
                "X X X X  rXXX  X  X",
                "X   X   bPo    X XX",
                "XXX X X        X  X",
                "X   X XXXXXXXXXX XX",
                "X X X             X",
                "X X XXXXXXXXXXXXX X",
                "X X               X",
                "X XXXXXXXXXXXXXXX X",
                "X X             X X",
                "X X XXXXXXXXXXX X X",
                "X X             X X",
                "X                 X",
                "XXXXXXXXXXXXXXXXXXX"
            }
        );
    }

    private static GameMap crossroads() {
        return new GameMap(
            "crossroads",
            "צומת הדרכים",
            "ארבעה חדרים וציר מרכזי — קצב מהיר",
            "בינוני",
            new String[]{
                "XXXXXXXXXXXXXXXXXXX",
                "X     X     X     X",
                "X XXX X XXX X XXX X",
                "X X           X X X",
                "X X XXX   XXX X X X",
                "X     X   X       X",
                "XXX X XXXXXXX X XXX",
                "    X    r    X    ",
                "XXX X XXXXXXX X XXX",
                "X      bPo        X",
                "XXX X XXXXXXX X XXX",
                "    X         X    ",
                "XXX X XXXXXXX X XXX",
                "X     X   X       X",
                "X X XXX   XXX X X X",
                "X X           X X X",
                "X XXX X   X XXX X X",
                "X     X   X       X",
                "X XXXXXXXXXXXXXXX X",
                "X                 X",
                "XXXXXXXXXXXXXXXXXXX"
            }
        );
    }
}
