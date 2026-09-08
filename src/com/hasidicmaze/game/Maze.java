package com.hasidicmaze.game;

import com.hasidicmaze.assets.AssetManager;
import com.hasidicmaze.map.GameMap;
import java.awt.Image;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tile board: spawn, collisions, wrap, and lane math. No scoring or Swing paint.
 */
public final class Maze {
    private final AssetManager assets;
    private GameMap gameMap;
    private int rowCount;
    private int columnCount;
    private int tileSize = 30;
    private int mapOriginX = 8;
    private int mapOriginY = 10;
    private int gameAreaWidth = 640;
    private int boardHeight = 720;

    private final int wallShrinkPx = 0;
    private final int wallRaisePx = 0;
    private final int actorInset = 4;

    private Set<Entity> walls = new HashSet<>();
    private Set<Entity> foods = new HashSet<>();
    private List<Entity> enemies = new ArrayList<>();
    private Entity hero;

    public Maze(AssetManager assets) {
        this.assets = assets;
    }

    public void configureCanvas(int gameAreaWidth, int boardHeight) {
        this.gameAreaWidth = gameAreaWidth;
        this.boardHeight = boardHeight;
    }

    public void applyDefaultGridLayout() {
        int maxTile = Math.min(
            Math.max(12, gameAreaWidth / Math.max(1, columnCount)),
            Math.max(12, boardHeight / Math.max(1, rowCount))
        );
        tileSize = maxTile;
        mapOriginX = Math.max(0, (gameAreaWidth - columnCount * tileSize) / 2);
        int slackY = Math.max(0, boardHeight - rowCount * tileSize);
        mapOriginY = slackY;
    }

    public void bind(GameMap map) {
        this.gameMap = map;
        this.rowCount = map.getRows();
        this.columnCount = map.getCols();
    }

    public void load(GameMap map) {
        bind(map);
        walls = new HashSet<>();
        foods = new HashSet<>();
        enemies = new ArrayList<>();
        String[] tiles = map.getTiles();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char ch = tiles[r].charAt(c);
                int x = c * tileSize + mapOriginX;
                int y = r * tileSize + mapOriginY;

                switch (ch) {
                    case 'X' -> {
                        int wallSize = tileSize - wallShrinkPx;
                        int wallX = x + wallShrinkPx / 2;
                        int wallY = y + wallShrinkPx / 2 - wallRaisePx;
                        walls.add(new Entity(assets.wall, wallX, wallY, wallSize, wallSize));
                    }
                    case 'b' -> enemies.add(createEnemy(assets.blueEnemy, x, y, EnemyPersonality.BLUE));
                    case 'o' -> enemies.add(createEnemy(assets.orangeEnemy, x, y, EnemyPersonality.ORANGE));
                    case 'p' -> enemies.add(createEnemy(assets.pinkEnemy, x, y, EnemyPersonality.PINK));
                    case 'r' -> enemies.add(createEnemy(assets.redEnemy, x, y, EnemyPersonality.RED));
                    case 'P' -> hero = createActor(assets.heroRight, x, y);
                    case ' ' -> {
                        if (!isCabinetFrameCell(r, c)) {
                            foods.add(new Entity(null, x + tileSize / 2 - 2, y + tileSize / 2 - 2, 4, 4));
                        }
                    }
                    default -> { }
                }
            }
        }
        placePowerPelletsAtCorners();
    }

    public boolean nudgeLayout(int dTile, int dOx, int dOy) {
        int maxTileW = Math.max(12, gameAreaWidth / Math.max(1, columnCount));
        int maxTileH = Math.max(12, boardHeight / Math.max(1, rowCount));
        int maxTile = Math.min(maxTileW, maxTileH);
        int newTile = Math.max(12, Math.min(maxTile, tileSize + dTile));
        int newOx = Math.max(0, Math.min(Math.max(0, gameAreaWidth - columnCount * newTile), mapOriginX + dOx));
        int newOy = Math.max(0, Math.min(Math.max(0, boardHeight - rowCount * newTile), mapOriginY + dOy));
        if (newTile == tileSize && newOx == mapOriginX && newOy == mapOriginY) {
            return false;
        }
        tileSize = newTile;
        mapOriginX = newOx;
        mapOriginY = newOy;
        return true;
    }

    private void placePowerPelletsAtCorners() {
        int[][] seeds = {
            {0, 0},
            {0, columnCount - 1},
            {rowCount - 1, 0},
            {rowCount - 1, columnCount - 1}
        };
        int size = Math.max(20, tileSize - 4);
        Image coffeeImg = assets.coffee;
        for (int[] seed : seeds) {
            int[] cell = nearestInnerPathCell(seed[0], seed[1]);
            if (cell == null) {
                continue;
            }
            int r = cell[0];
            int c = cell[1];
            foods.removeIf(f -> foodInCell(f, r, c));
            int x = mapOriginX + c * tileSize + (tileSize - size) / 2;
            int y = mapOriginY + r * tileSize + (tileSize - size) / 2;
            Entity pellet = new Entity(coffeeImg, x, y, size, size);
            pellet.powerPellet = true;
            foods.add(pellet);
        }
    }

    private boolean foodInCell(Entity food, int row, int col) {
        int fr = Math.max(0, Math.min(rowCount - 1,
            (int) Math.floor((food.y + food.height / 2f - mapOriginY) / tileSize)));
        int fc = Math.max(0, Math.min(columnCount - 1,
            (int) Math.floor((food.x + food.width / 2f - mapOriginX) / tileSize)));
        return fr == row && fc == col;
    }

    private int[] nearestInnerPathCell(int startR, int startC) {
        boolean[][] visited = new boolean[rowCount][columnCount];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startR, startC});
        visited[startR][startC] = true;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        while (!queue.isEmpty()) {
            int[] cur = queue.removeFirst();
            int r = cur[0];
            int c = cur[1];
            if (!isCabinetFrameCell(r, c) && gameMap.isWalkable(r, c)) {
                return cur;
            }
            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (nr < 0 || nc < 0 || nr >= rowCount || nc >= columnCount || visited[nr][nc]) {
                    continue;
                }
                visited[nr][nc] = true;
                queue.add(new int[]{nr, nc});
            }
        }
        return null;
    }

    public boolean isCabinetFrameCell(int r, int c) {
        return r == 0 || r == rowCount - 1 || c == 0 || c == columnCount - 1;
    }

    public boolean isWallTile(int row, int col) {
        if (gameMap == null || row < 0 || col < 0 || row >= rowCount || col >= columnCount) {
            return true;
        }
        return gameMap.getTiles()[row].charAt(col) == 'X';
    }

    public boolean isWalkable(int row, int col) {
        return gameMap != null && gameMap.isWalkable(row, col);
    }

    public int[] stepTile(int row, int col, char dir) {
        int nr = row;
        int nc = col;
        switch (dir) {
            case 'U' -> nr--;
            case 'D' -> nr++;
            case 'L' -> nc--;
            case 'R' -> nc++;
            default -> { }
        }
        if (nr < 0) {
            nr = rowCount - 1;
        } else if (nr >= rowCount) {
            nr = 0;
        }
        if (nc < 0) {
            nc = columnCount - 1;
        } else if (nc >= columnCount) {
            nc = 0;
        }
        return new int[]{nr, nc};
    }

    private Entity createEnemy(Image image, int cellX, int cellY, EnemyPersonality personality) {
        Entity e = createActor(image, cellX, cellY);
        e.personality = personality;
        e.normalImage = image;
        return e;
    }

    private Entity createActor(Image image, int cellX, int cellY) {
        int size = actorSize();
        int pad = actorPad();
        return new Entity(image, cellX + pad, cellY + pad, size, size);
    }

    public int actorSize() {
        return Math.max(12, tileSize - actorInset * 2);
    }

    public int actorPad() {
        return (tileSize - actorSize()) / 2;
    }

    public int moveStep() {
        int step = Math.max(1, tileSize / 4);
        while (step > 1 && tileSize % step != 0) {
            step--;
        }
        return step;
    }

    public boolean hitsWallAt(int x, int y, int w, int h) {
        for (Entity wall : walls) {
            if (x < wall.x + wall.width && x + w > wall.x
                && y < wall.y + wall.height && y + h > wall.y) {
                return true;
            }
        }
        return false;
    }

    public int laneX(int col) {
        return mapOriginX + col * tileSize + actorPad();
    }

    public int laneY(int row) {
        return mapOriginY + row * tileSize + actorPad();
    }

    public int colAtCenter(Entity actor) {
        float centerX = actor.x + actor.width / 2f;
        return Math.max(0, Math.min(columnCount - 1,
            (int) Math.floor((centerX - mapOriginX) / tileSize)));
    }

    public int rowAtCenter(Entity actor) {
        float centerY = actor.y + actor.height / 2f;
        return Math.max(0, Math.min(rowCount - 1,
            (int) Math.floor((centerY - mapOriginY) / tileSize)));
    }

    public int spawnRow(Entity actor) {
        return Math.max(0, Math.min(rowCount - 1,
            (int) Math.floor((actor.startY + actor.height / 2f - mapOriginY) / tileSize)));
    }

    public int spawnCol(Entity actor) {
        return Math.max(0, Math.min(columnCount - 1,
            (int) Math.floor((actor.startX + actor.width / 2f - mapOriginX) / tileSize)));
    }

    public void lockToLane(Entity actor) {
        if (actor.velocityX != 0 && actor.velocityY == 0) {
            actor.y = laneY(rowAtCenter(actor));
        } else if (actor.velocityY != 0 && actor.velocityX == 0) {
            actor.x = laneX(colAtCenter(actor));
        }
    }

    public boolean canTurn(Entity actor, char turn) {
        int threshold = Math.max(2, moveStep() / 2);
        if (turn == 'U' || turn == 'D') {
            return Math.abs(actor.x - laneX(colAtCenter(actor))) <= threshold;
        }
        if (turn == 'L' || turn == 'R') {
            return Math.abs(actor.y - laneY(rowAtCenter(actor))) <= threshold;
        }
        return false;
    }

    public void snapToLaneForTurn(Entity actor, char turn) {
        if (turn == 'U' || turn == 'D') {
            actor.x = laneX(colAtCenter(actor));
        } else if (turn == 'L' || turn == 'R') {
            actor.y = laneY(rowAtCenter(actor));
        }
    }

    public void updateVelocity(Entity entity) {
        int step = moveStep();
        switch (entity.direction) {
            case 'U' -> { entity.velocityX = 0; entity.velocityY = -step; }
            case 'D' -> { entity.velocityX = 0; entity.velocityY = step; }
            case 'L' -> { entity.velocityX = -step; entity.velocityY = 0; }
            case 'R' -> { entity.velocityX = step; entity.velocityY = 0; }
            default -> { entity.velocityX = 0; entity.velocityY = 0; }
        }
    }

    public boolean isPerpendicular(char a, char b) {
        boolean aHoriz = a == 'L' || a == 'R';
        boolean bHoriz = b == 'L' || b == 'R';
        return aHoriz != bHoriz;
    }

    public char opposite(char dir) {
        return switch (dir) {
            case 'U' -> 'D';
            case 'D' -> 'U';
            case 'L' -> 'R';
            case 'R' -> 'L';
            default -> dir;
        };
    }

    public boolean isOpposite(char a, char b) {
        return a == opposite(b);
    }

    public void wrap(Entity e) {
        int pad = actorPad();
        int minX = mapOriginX + pad;
        int maxX = mapOriginX + (columnCount - 1) * tileSize + pad;
        int minY = mapOriginY + pad;
        int maxY = mapOriginY + (rowCount - 1) * tileSize + pad;
        if (e.x < minX) {
            e.x = maxX;
        } else if (e.x > maxX) {
            e.x = minX;
        }
        if (e.y < minY) {
            e.y = maxY;
        } else if (e.y > maxY) {
            e.y = minY;
        }
    }

    public boolean collision(Entity a, Entity b) {
        return a.x < b.x + b.width && a.x + a.width > b.x
            && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    public void resolveWallHit(Entity actor) {
        for (Entity wall : walls) {
            if (collision(actor, wall)) {
                actor.x -= actor.velocityX;
                actor.y -= actor.velocityY;
                lockToLane(actor);
                break;
            }
        }
    }

    public GameMap getGameMap() { return gameMap; }
    public int getRowCount() { return rowCount; }
    public int getColumnCount() { return columnCount; }
    public int getTileSize() { return tileSize; }
    public int getMapOriginX() { return mapOriginX; }
    public int getMapOriginY() { return mapOriginY; }
    public int getGameAreaWidth() { return gameAreaWidth; }
    public int getBoardHeight() { return boardHeight; }
    public Set<Entity> getWalls() { return walls; }
    public Set<Entity> getFoods() { return foods; }
    public List<Entity> getEnemies() { return enemies; }
    public Entity getHero() { return hero; }
}
