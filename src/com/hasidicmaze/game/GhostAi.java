package com.hasidicmaze.game;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;

/**
 * Ghost AI — classic-style personalities, scatter/chase, sticky corridors.
 * Not all ghosts perfect-BFS the hero (that felt unfair after warmup).
 */
public final class GhostAi {
    private final Maze maze;
    private final Random random;

    public GhostAi(Maze maze, Random random) {
        this.maze = maze;
        this.random = random;
    }

    public void chooseEnemyDirection(
        Entity enemy,
        Entity hero,
        List<Entity> enemies,
        int huntWarmupTicksLeft,
        int pathRecalcTiles,
        boolean scattering
    ) {
        chooseEnemyDirection(enemy, hero, enemies, huntWarmupTicksLeft, pathRecalcTiles, scattering, false);
    }

    public void chooseEnemyDirection(
        Entity enemy,
        Entity hero,
        List<Entity> enemies,
        int huntWarmupTicksLeft,
        int pathRecalcTiles,
        boolean scattering,
        boolean forceRecalc
    ) {
        if (enemy.personality == null) {
            return;
        }
        int row = maze.rowAtCenter(enemy);
        int col = maze.colAtCenter(enemy);
        boolean newTile = row != enemy.tileRow || col != enemy.tileCol;
        if (!newTile && !forceRecalc) {
            return;
        }
        if (newTile) {
            enemy.tileRow = row;
            enemy.tileCol = col;
            if (!forceRecalc && huntWarmupTicksLeft <= 0 && !enemy.frightened) {
                enemy.tilesSincePathRecalc++;
            }
        }

        char best;
        if (huntWarmupTicksLeft > 0) {
            best = warmUpDirection(enemy, row, col);
            enemy.tilesSincePathRecalc = 0;
        } else if (enemy.frightened) {
            best = frightenedDirection(enemy, row, col);
            enemy.tilesSincePathRecalc = 0;
        } else {
            int[] ahead = maze.stepTile(row, col, enemy.direction);
            boolean aheadBlocked = !maze.isWalkable(ahead[0], ahead[1]);
            boolean junction = walkableExitCount(row, col) >= 3;
            boolean nearSpawn = Math.abs(row - maze.spawnRow(enemy)) + Math.abs(col - maze.spawnCol(enemy)) <= 4;

            // Sticky paths = more mistakes / more fun. Only smart-repath sometimes at junctions.
            boolean due = forceRecalc
                || enemy.tilesSincePathRecalc >= pathRecalcTiles
                || aheadBlocked
                || (junction && nearSpawn)
                || (junction && random.nextFloat() < 0.30f);

            if (due) {
                int[] target = enemyTargetTile(enemy, hero, enemies, scattering);
                best = shortestPathStep(enemy, row, col, target[0], target[1]);
                enemy.tilesSincePathRecalc = 0;
            } else {
                best = enemy.direction;
            }
        }

        if (best != enemy.direction && maze.isPerpendicular(enemy.direction, best)) {
            enemy.x = maze.laneX(col);
            enemy.y = maze.laneY(row);
        } else {
            maze.lockToLane(enemy);
        }
        enemy.direction = best;
        maze.updateVelocity(enemy);
    }

    public char shortestPathStep(Entity enemy, int row, int col, int targetRow, int targetCol) {
        int[] goal = nearestWalkable(targetRow, targetCol);
        int gr = goal[0];
        int gc = goal[1];
        char[] order = {'U', 'L', 'D', 'R'};

        if (row == gr && col == gc) {
            int[] ahead = maze.stepTile(row, col, enemy.direction);
            if (maze.isWalkable(ahead[0], ahead[1])) {
                return enemy.direction;
            }
            for (char dir : order) {
                if (dir == maze.opposite(enemy.direction)) {
                    continue;
                }
                int[] n = maze.stepTile(row, col, dir);
                if (maze.isWalkable(n[0], n[1])) {
                    return dir;
                }
            }
            return maze.opposite(enemy.direction);
        }

        char step = bfsFirstStep(row, col, gr, gc, enemy.direction, false);
        if (step != 0) {
            return step;
        }
        step = bfsFirstStep(row, col, gr, gc, enemy.direction, true);
        if (step != 0) {
            return step;
        }

        for (char dir : order) {
            if (dir == maze.opposite(enemy.direction)) {
                continue;
            }
            int[] next = maze.stepTile(row, col, dir);
            if (maze.isWalkable(next[0], next[1])) {
                return dir;
            }
        }
        return maze.opposite(enemy.direction);
    }

    private int[] enemyTargetTile(Entity enemy, Entity hero, List<Entity> enemies, boolean scattering) {
        int rows = maze.getRowCount();
        int cols = maze.getColumnCount();
        int hr = maze.rowAtCenter(hero);
        int hc = maze.colAtCenter(hero);

        if (scattering) {
            return scatterCorner(enemy.personality, rows, cols);
        }

        return switch (enemy.personality) {
            case RED -> new int[]{hr, hc};
            case PINK -> tileAhead(hero, 4);
            case BLUE -> {
                int[] ahead = tileAhead(hero, 2);
                Entity red = findRed(enemies);
                if (red == null) {
                    yield ahead;
                }
                int rr = maze.rowAtCenter(red);
                int rc = maze.colAtCenter(red);
                yield new int[]{2 * ahead[0] - rr, 2 * ahead[1] - rc};
            }
            case ORANGE -> {
                int er = maze.rowAtCenter(enemy);
                int ec = maze.colAtCenter(enemy);
                int dist = Math.abs(er - hr) + Math.abs(ec - hc);
                if (dist < 8) {
                    enemy.orangeFleeing = true;
                } else if (dist > 10) {
                    enemy.orangeFleeing = false;
                }
                if (enemy.orangeFleeing) {
                    yield scatterCorner(EnemyPersonality.ORANGE, rows, cols);
                }
                yield new int[]{hr, hc};
            }
        };
    }

    private int[] tileAhead(Entity hero, int tiles) {
        int r = maze.rowAtCenter(hero);
        int c = maze.colAtCenter(hero);
        for (int i = 0; i < tiles; i++) {
            int[] next = maze.stepTile(r, c, hero.direction);
            if (!maze.isWalkable(next[0], next[1])) {
                break;
            }
            r = next[0];
            c = next[1];
        }
        return new int[]{r, c};
    }

    private Entity findRed(List<Entity> enemies) {
        for (Entity e : enemies) {
            if (e.personality == EnemyPersonality.RED) {
                return e;
            }
        }
        return null;
    }

    private int[] scatterCorner(EnemyPersonality p, int rows, int cols) {
        return switch (p) {
            case RED -> new int[]{0, cols - 1};
            case PINK -> new int[]{0, 0};
            case BLUE -> new int[]{rows - 1, cols - 1};
            case ORANGE -> new int[]{rows - 1, 0};
        };
    }

    private char frightenedDirection(Entity enemy, int row, int col) {
        char[] order = {'U', 'L', 'D', 'R'};
        for (int i = order.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = order[i];
            order[i] = order[j];
            order[j] = tmp;
        }
        for (char dir : order) {
            if (dir == maze.opposite(enemy.direction)) {
                continue;
            }
            int[] next = maze.stepTile(row, col, dir);
            if (maze.isWalkable(next[0], next[1])) {
                return dir;
            }
        }
        return maze.opposite(enemy.direction);
    }

    private int walkableExitCount(int row, int col) {
        int n = 0;
        for (char dir : new char[]{'U', 'D', 'L', 'R'}) {
            int[] next = maze.stepTile(row, col, dir);
            if (maze.isWalkable(next[0], next[1])) {
                n++;
            }
        }
        return n;
    }

    private char warmUpDirection(Entity enemy, int row, int col) {
        char dir = enemy.direction;
        if (dir != 'L' && dir != 'R') {
            dir = (enemy.personality == EnemyPersonality.RED
                || enemy.personality == EnemyPersonality.PINK) ? 'L' : 'R';
        }
        int[] next = maze.stepTile(row, col, dir);
        if (!maze.isWalkable(next[0], next[1])) {
            dir = maze.opposite(dir);
            next = maze.stepTile(row, col, dir);
            if (!maze.isWalkable(next[0], next[1])) {
                for (char d : new char[]{'U', 'D', 'L', 'R'}) {
                    int[] n = maze.stepTile(row, col, d);
                    if (maze.isWalkable(n[0], n[1])) {
                        return d;
                    }
                }
            }
        }
        return dir;
    }

    private char bfsFirstStep(int row, int col, int gr, int gc, char currentDir, boolean allowReverse) {
        int rowCount = maze.getRowCount();
        int columnCount = maze.getColumnCount();
        boolean[][] visited = new boolean[rowCount][columnCount];
        char[][] firstStep = new char[rowCount][columnCount];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        visited[row][col] = true;

        char[] order = {'U', 'L', 'D', 'R'};
        for (char dir : order) {
            if (!allowReverse && dir == maze.opposite(currentDir)) {
                continue;
            }
            int[] next = maze.stepTile(row, col, dir);
            int nr = next[0];
            int nc = next[1];
            if (!maze.isWalkable(nr, nc) || visited[nr][nc]) {
                continue;
            }
            visited[nr][nc] = true;
            firstStep[nr][nc] = dir;
            if (nr == gr && nc == gc) {
                return dir;
            }
            queue.add(new int[]{nr, nc});
        }

        while (!queue.isEmpty()) {
            int[] cur = queue.removeFirst();
            for (char dir : order) {
                int[] next = maze.stepTile(cur[0], cur[1], dir);
                int nr = next[0];
                int nc = next[1];
                if (!maze.isWalkable(nr, nc) || visited[nr][nc]) {
                    continue;
                }
                visited[nr][nc] = true;
                firstStep[nr][nc] = firstStep[cur[0]][cur[1]];
                if (nr == gr && nc == gc) {
                    return firstStep[nr][nc];
                }
                queue.add(new int[]{nr, nc});
            }
        }
        return 0;
    }

    private int[] nearestWalkable(int row, int col) {
        int rowCount = maze.getRowCount();
        int columnCount = maze.getColumnCount();
        row = Math.max(0, Math.min(rowCount - 1, row));
        col = Math.max(0, Math.min(columnCount - 1, col));
        if (maze.isWalkable(row, col)) {
            return new int[]{row, col};
        }
        boolean[][] visited = new boolean[rowCount][columnCount];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{row, col});
        visited[row][col] = true;
        char[] order = {'U', 'L', 'D', 'R'};
        while (!queue.isEmpty()) {
            int[] cur = queue.removeFirst();
            for (char dir : order) {
                int[] next = maze.stepTile(cur[0], cur[1], dir);
                int nr = next[0];
                int nc = next[1];
                if (visited[nr][nc]) {
                    continue;
                }
                visited[nr][nc] = true;
                if (maze.isWalkable(nr, nc)) {
                    return new int[]{nr, nc};
                }
                queue.add(new int[]{nr, nc});
            }
        }
        return new int[]{row, col};
    }
}
