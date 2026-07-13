package com.hasidicmaze.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Six stages — each uses a same-size background so grid scale/origin stay shared.
 * 0 = wall, 1 = open, 8 = player, 9 = ghost (placed as r/b/p/o in order).
 */
public final class MapCatalog {
    private static final List<GameMap> MAPS = Arrays.asList(
        sederIyuna(),
        sederGirsa(),
        lockedSlot(3, "סדר שלישי"),
        lockedSlot(4, "סדר רביעי"),
        lockedSlot(5, "סדר חמישי"),
        lockedSlot(6, "סדר שישי")
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

    /** סדר עיונא — קל — bg2.png (המפה שעבדת עליה) */
    private static GameMap sederIyuna() {
        String[] digits = {
            "0000000000000000000",
            "0111111110111111110",
            "0100100010100010010",
            "0111111111111111110",
            "0100101000001010010",
            "0111101111111011110",
            "0000101000001001000",
            "0000101111111111000",
            "0111111100001001110",
            "0001001011101111000",
            "0001111990991001000",
            "1111001001001001111",
            "0001111111111111000",
            "0001000101010001000",
            "0111111118111111110",
            "0010010001000100100",
            "0111110111110111110",
            "0001011100011101000",
            "0111000110110001110",
            "0001111100011111000",
            "0000000000000000000"
        };
        return fromDigits(
            "iyuna",
            "סדר עיונא",
            "הרקע והמחסומים של bg2",
            "קל",
            "bg2.png",
            false,
            digits,
            null,
            -1, -1
        );
    }

    /** סדר גירסא — קשה — bg.png + המפה הראשונה שהתאמנו */
    private static GameMap sederGirsa() {
        String[] digits = {
            "0000000000000001000",
            "0001110100111111110",
            "0001011100100010010",
            "0111011111111111110",
            "0001111001110110000",
            "0111111111110111110",
            "0110011111111111000",
            "0011111001101111000",
            "0111001111111001110",
            "0011111110111111100",
            "0011111110111111110",
            "1111001111110011111",
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
        return fromDigits(
            "girsa",
            "סדר גירסא",
            "החצר הגדולה — המפה הראשונה",
            "קשה",
            "bg.png",
            false,
            digits,
            new int[][]{{11, 7}, {11, 8}, {11, 9}, {11, 10}},
            15, 9
        );
    }

    private static GameMap lockedSlot(int index, String title) {
        return lockedSlot(index, title, "סגור");
    }

    private static GameMap lockedSlot(int index, String title, String difficulty) {
        String[] stub = {
            "XXXXXXXXXXXXXXXXXXX",
            "X                 X",
            "X XXXXXXXXXXXXXXX X",
            "X X             X X",
            "X X XXXXXXXXXXX X X",
            "X X X         X X X",
            "X X X XXXXXXX X X X",
            "X X X X     X X X X",
            "X X X X  P  X X X X",
            "X X X X     X X X X",
            "X X X XXXXXXX X X X",
            "X X X         X X X",
            "X X XXXXXXXXXXX X X",
            "X X             X X",
            "X XXXXXXXXXXXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX",
            "XXXXXXXXXXXXXXXXXXX",
            "XXXXXXXXXXXXXXXXXXX",
            "XXXXXXXXXXXXXXXXXXX",
            "XXXXXXXXXXXXXXXXXXX"
        };
        return new GameMap(
            "locked-" + index,
            title,
            "בקרוב — השלב עדיין לא פתוח",
            difficulty,
            "bg2.png",
            true,
            stub
        );
    }

    /**
     * @param ghostCells optional explicit ghost cells; if null, read 9s from digits
     * @param playerR/C  optional explicit player; if &lt;0, read 8 from digits
     */
    private static GameMap fromDigits(
        String id,
        String title,
        String subtitle,
        String difficulty,
        String backgroundFile,
        boolean locked,
        String[] digits,
        int[][] ghostCells,
        int playerR,
        int playerC
    ) {
        char[][] grid = new char[digits.length][digits[0].length()];
        List<int[]> nines = new ArrayList<>();
        int foundPR = -1;
        int foundPC = -1;

        for (int r = 0; r < digits.length; r++) {
            if (digits[r].length() != digits[0].length()) {
                throw new IllegalStateException(id + " row " + r + " bad length");
            }
            for (int c = 0; c < digits[r].length(); c++) {
                char ch = digits[r].charAt(c);
                switch (ch) {
                    case '0' -> grid[r][c] = 'X';
                    case '1' -> grid[r][c] = ' ';
                    case '8' -> {
                        grid[r][c] = ' ';
                        foundPR = r;
                        foundPC = c;
                    }
                    case '9' -> {
                        grid[r][c] = ' ';
                        nines.add(new int[]{r, c});
                    }
                    default -> throw new IllegalStateException(
                        id + " bad char '" + ch + "' at (" + r + "," + c + ")");
                }
            }
        }

        if (playerR >= 0 && playerC >= 0) {
            place(grid, playerR, playerC, 'P');
        } else if (foundPR >= 0) {
            place(grid, foundPR, foundPC, 'P');
        } else {
            throw new IllegalStateException(id + ": missing player");
        }

        char[] ghostChars = {'r', 'b', 'p', 'o'};
        if (ghostCells != null) {
            for (int i = 0; i < ghostCells.length; i++) {
                place(grid, ghostCells[i][0], ghostCells[i][1], ghostChars[i % ghostChars.length]);
            }
        } else {
            for (int i = 0; i < nines.size(); i++) {
                int[] cell = nines.get(i);
                place(grid, cell[0], cell[1], ghostChars[i % ghostChars.length]);
            }
        }

        sealUnreachable(grid);

        String[] tiles = new String[grid.length];
        for (int r = 0; r < grid.length; r++) {
            tiles[r] = new String(grid[r]);
        }

        return new GameMap(id, title, subtitle, difficulty, backgroundFile, locked, tiles);
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
}
