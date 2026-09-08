package com.hasidicmaze.game;

import com.hasidicmaze.assets.AssetManager;
import com.hasidicmaze.map.GameMap;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Session rules: score, lives, frightened mode, stage bonuses, timers. No painting.
 */
public final class GameSession {
    public static final int HUNT_WARMUP_TICKS = 5 * 20;
    public static final int FRIGHTENED_TICKS = 8 * 20;
    public static final int FRIGHTENED_FLASH_TICKS = 2 * 20;
    public static final int BONUS_EATEN_DISPLAY_TICKS = 2 * 20;
    public static final int MAX_EATEN_BOOK_ICONS = 8;
    public static final int SIDE_W = 110;

    /** One stage prize spawn (book / tefillin / hat). */
    private static final int BONUS_THRESHOLD = 70;
    /** Slower than Pac-Man so chase is escapable. */
    private static final float GHOST_SPEED_START = 0.72f;
    private static final float GHOST_SPEED_PER_MAP = 0.04f;
    private static final float GHOST_SPEED_MAX = 0.95f;
    private static final int SCATTER_TICKS = 7 * 20;
    private static final int CHASE_TICKS = 18 * 20;

    private final AssetManager assets;
    private final Maze maze;
    private final GhostAi ghostAi;
    private final Random random = new Random();

    private GameMap gameMap;
    private int score;
    private int totalFoodCount;
    private int lives = 3;
    private boolean gameOver;
    private boolean isPaused;
    private boolean timedOut;
    private char nextDirection = 'R';
    private boolean awaitingName;
    private boolean campaignMode;
    private boolean stageAdvancePending;

    private int mazeTimeLimitTicks;
    private int mazeTimeLeftTicks;
    private int huntWarmupTicksLeft;
    /** After warmup: alternate scatter (corners) and chase. */
    private boolean scattering = true;
    private int modeTicksLeft;

    private float ghostSpeedRatio = GHOST_SPEED_START;
    private float ghostMoveCredit;
    private int frightenedTicksLeft;
    private int ghostEatStreak;
    private int pathRecalcTiles = 26;

    private Entity bonus;
    private int bonusPoints;
    private int bonusEdibleTicks;
    private int bonusEatenTicks;
    private int foodsEatenThisMap;
    private int bonusesSpawnedThisMap;
    private int mapsCleared;
    private final List<Image> eatenBooks = new ArrayList<>();
    private Image activeBonusSymbol;
    private final List<FloatingScore> floatingScores = new ArrayList<>();

    public GameSession(AssetManager assets, Maze maze) {
        this.assets = assets;
        this.maze = maze;
        this.ghostAi = new GhostAi(maze, random);
    }

    public void start(GameMap map, boolean campaign) {
        this.gameMap = map;
        this.campaignMode = campaign;
        this.stageAdvancePending = false;
        maze.bind(map);
        maze.applyDefaultGridLayout();
        maze.load(map);

        score = 0;
        lives = 3;
        gameOver = false;
        timedOut = false;
        isPaused = true;
        awaitingName = false;
        nextDirection = 'R';
        mapsCleared = 0;
        eatenBooks.clear();
        resetBonusProgress();
        totalFoodCount = maze.getFoods().size();
        mazeTimeLimitTicks = mazeTimeLimitForMap();
        resetPositions();
        resetMazeTimer();
        resetGhostSpeed();
        pathRecalcTiles = 26;
        scattering = true;
        modeTicksLeft = SCATTER_TICKS;
        stopEnemyMotion();
    }

    /** Keep score/lives and load the next campaign stage. */
    public void continueCampaign(GameMap next) {
        this.gameMap = next;
        this.stageAdvancePending = false;
        maze.bind(next);
        maze.applyDefaultGridLayout();
        maze.load(next);
        totalFoodCount = maze.getFoods().size();
        mazeTimeLimitTicks = mazeTimeLimitForMap();
        resetBonusProgress();
        clearFrightenedMode();
        resetPositions();
        stopEnemyMotion();
        resetMazeTimer();
        huntWarmupTicksLeft = 0;
        scattering = true;
        modeTicksLeft = SCATTER_TICKS;
        nextDirection = 'R';
        isPaused = true;
        timedOut = false;
    }

    public boolean consumeStageCleared() {
        if (!stageAdvancePending) {
            return false;
        }
        stageAdvancePending = false;
        return true;
    }

    public boolean isCampaignMode() {
        return campaignMode;
    }

    public void reloadAfterLayoutNudge() {
        maze.load(gameMap);
        totalFoodCount = maze.getFoods().size();
        mazeTimeLimitTicks = mazeTimeLimitForMap();
        resetBonusProgress();
        resetPositions();
        stopEnemyMotion();
        resetMazeTimer();
        isPaused = true;
    }

    private void loadMapKeepProgress() {
        maze.load(gameMap);
        totalFoodCount = maze.getFoods().size();
        mazeTimeLimitTicks = mazeTimeLimitForMap();
        resetBonusProgress();
    }

    private void stopEnemyMotion() {
        for (Entity enemy : maze.getEnemies()) {
            enemy.velocityX = 0;
            enemy.velocityY = 0;
        }
    }

    public void tick() {
        if (!isPaused && !gameOver) {
            if (huntWarmupTicksLeft > 0) {
                huntWarmupTicksLeft--;
                if (huntWarmupTicksLeft == 0) {
                    scattering = true;
                    modeTicksLeft = SCATTER_TICKS;
                }
            } else if (frightenedTicksLeft <= 0) {
                tickScatterChaseMode();
            }
            if (frightenedTicksLeft > 0) {
                frightenedTicksLeft--;
                if (frightenedTicksLeft == 0) {
                    clearFrightenedMode();
                } else {
                    updateFrightenedFlashSprites();
                }
            }
            tickFloatingScores();
            tickBonus();
            if (mazeTimeLeftTicks > 0) {
                mazeTimeLeftTicks--;
                if (mazeTimeLeftTicks <= 0) {
                    loseLife(true);
                }
            }
        }
        move();
    }

    private void tickScatterChaseMode() {
        if (modeTicksLeft > 0) {
            modeTicksLeft--;
        }
        if (modeTicksLeft <= 0) {
            scattering = !scattering;
            modeTicksLeft = scattering ? SCATTER_TICKS : CHASE_TICKS;
        }
    }

    public void setNextDirection(char direction) {
        nextDirection = direction;
        if (isPaused) {
            isPaused = false;
            timedOut = false;
            huntWarmupTicksLeft = HUNT_WARMUP_TICKS;
            scattering = true;
            modeTicksLeft = SCATTER_TICKS;
            if (mazeTimeLeftTicks <= 0) {
                resetMazeTimer();
            }
            for (Entity enemy : maze.getEnemies()) {
                maze.updateVelocity(enemy);
            }
        }
    }

    public void markAwaitingName() {
        awaitingName = true;
    }

    private void resetBonusProgress() {
        clearBonus();
        foodsEatenThisMap = 0;
        bonusesSpawnedThisMap = 0;
    }

    private void clearBonus() {
        bonus = null;
        bonusPoints = 0;
        bonusEdibleTicks = 0;
        bonusEatenTicks = 0;
        activeBonusSymbol = null;
    }

    private int mazeTimeLimitForMap() {
        int seconds = Math.max(75, totalFoodCount + 40);
        return seconds * 20;
    }

    private void resetMazeTimer() {
        mazeTimeLimitTicks = mazeTimeLimitForMap();
        mazeTimeLeftTicks = mazeTimeLimitTicks;
    }

    public int mazeRemainingSeconds() {
        return Math.max(0, (mazeTimeLeftTicks + 19) / 20);
    }

    private void move() {
        Entity hero = maze.getHero();
        if (isPaused || gameOver || hero == null) {
            return;
        }

        if (nextDirection != hero.direction) {
            boolean reverse = maze.isOpposite(hero.direction, nextDirection);
            boolean turnOk = reverse || maze.canTurn(hero, nextDirection);
            if (turnOk) {
                char old = hero.direction;
                int saveX = hero.x;
                int saveY = hero.y;
                maze.snapToLaneForTurn(hero, nextDirection);
                hero.direction = nextDirection;
                maze.updateVelocity(hero);
                int testX = hero.x + hero.velocityX;
                int testY = hero.y + hero.velocityY;
                if (maze.hitsWallAt(testX, testY, hero.width, hero.height)) {
                    hero.x = saveX;
                    hero.y = saveY;
                    hero.direction = old;
                    maze.updateVelocity(hero);
                } else {
                    hero.image = switch (hero.direction) {
                        case 'U' -> assets.heroUp;
                        case 'D' -> assets.heroDown;
                        case 'L' -> assets.heroLeft;
                        default -> assets.heroRight;
                    };
                }
            }
        }

        hero.x += hero.velocityX;
        hero.y += hero.velocityY;
        maze.wrap(hero);
        maze.lockToLane(hero);
        maze.resolveWallHit(hero);

        for (Entity enemy : maze.getEnemies()) {
            if (maze.collision(enemy, hero)) {
                handleHeroEnemyCollision(enemy);
                break;
            }
        }
        if (gameOver || isPaused) {
            return;
        }

        tickGhostMoveCredit();
        if (ghostMoveCredit >= 1f) {
            ghostMoveCredit -= 1f;
            moveEnemiesOneStep();
        }

        Entity eaten = null;
        for (Entity food : maze.getFoods()) {
            if (maze.collision(hero, food)) {
                eaten = food;
                if (food.powerPellet) {
                    score += 10;
                    activateFrightenedMode();
                } else {
                    score += 1;
                }
            }
        }
        if (eaten != null) {
            maze.getFoods().remove(eaten);
            foodsEatenThisMap++;
            maybeSpawnBonus();
        }
        tryEatBonus();
        if (maze.getFoods().isEmpty() && !stageAdvancePending) {
            bumpGhostSpeedAfterMapClear();
            bumpPathRecalcAfterMapClear();
            clearFrightenedMode();
            mapsCleared++;
            if (campaignMode) {
                stageAdvancePending = true;
                isPaused = true;
                stopEnemyMotion();
                return;
            }
            loadMapKeepProgress();
            resetPositions();
            huntWarmupTicksLeft = HUNT_WARMUP_TICKS;
            scattering = true;
            modeTicksLeft = SCATTER_TICKS;
            resetMazeTimer();
        }
    }

    private void maybeSpawnBonus() {
        if (bonusesSpawnedThisMap >= 1) {
            return;
        }
        if (foodsEatenThisMap != BONUS_THRESHOLD) {
            return;
        }
        activateBonus();
    }

    private void activateBonus() {
        clearBonus();
        bonusesSpawnedThisMap++;
        int stage = stageIndex();
        Image img = assets.bonusForStage(stage);
        bonusPoints = assets.bonusPointsForStage(stage);
        activeBonusSymbol = img;
        int size = Math.max(18, maze.getTileSize() - 2);
        int[] cell = bonusSpawnCell();
        int x = maze.getMapOriginX() + cell[1] * maze.getTileSize() + (maze.getTileSize() - size) / 2;
        int y = maze.getMapOriginY() + cell[0] * maze.getTileSize() + (maze.getTileSize() - size) / 2;
        bonus = new Entity(img, x, y, size, size);
        bonus.bonusItem = true;
        bonusEdibleTicks = (9 + random.nextInt(2)) * 20;
        bonusEatenTicks = 0;
    }

    /** 0=book1, 1=book2, 2=tefillin, 3=hat — one prize per stage. */
    private int stageIndex() {
        if (gameMap == null) {
            return 0;
        }
        List<GameMap> maps = com.hasidicmaze.map.MapCatalog.all();
        for (int i = 0; i < maps.size(); i++) {
            if (maps.get(i).getId().equals(gameMap.getId())) {
                return i;
            }
        }
        return 0;
    }

    private int[] bonusSpawnCell() {
        int rowCount = maze.getRowCount();
        int columnCount = maze.getColumnCount();
        int preferR = Math.min(rowCount - 2, Math.max(1, rowCount / 2 + 1));
        int preferC = columnCount / 2;
        if (!maze.isWallTile(preferR, preferC)) {
            return new int[]{preferR, preferC};
        }
        for (int rad = 1; rad < Math.max(rowCount, columnCount); rad++) {
            for (int dr = -rad; dr <= rad; dr++) {
                for (int dc = -rad; dc <= rad; dc++) {
                    if (Math.abs(dr) != rad && Math.abs(dc) != rad) {
                        continue;
                    }
                    int r = preferR + dr;
                    int c = preferC + dc;
                    if (r >= 0 && r < rowCount && c >= 0 && c < columnCount && !maze.isWallTile(r, c)) {
                        return new int[]{r, c};
                    }
                }
            }
        }
        return new int[]{preferR, preferC};
    }

    private void tryEatBonus() {
        Entity hero = maze.getHero();
        if (bonus == null || bonusEdibleTicks <= 0 || hero == null) {
            return;
        }
        if (!maze.collision(hero, bonus)) {
            return;
        }
        score += bonusPoints;
        floatingScores.add(new FloatingScore(
            bonus.x + bonus.width / 2,
            bonus.y + bonus.height / 2,
            bonusPoints,
            BONUS_EATEN_DISPLAY_TICKS
        ));
        if (activeBonusSymbol != null) {
            eatenBooks.add(activeBonusSymbol);
            while (eatenBooks.size() > MAX_EATEN_BOOK_ICONS) {
                eatenBooks.remove(0);
            }
        }
        clearBonus();
    }

    private void tickBonus() {
        if (bonusEatenTicks > 0) {
            bonusEatenTicks--;
            if (bonusEatenTicks <= 0) {
                clearBonus();
            }
            return;
        }
        if (bonus != null && bonusEdibleTicks > 0) {
            bonusEdibleTicks--;
            if (bonusEdibleTicks <= 0) {
                clearBonus();
            }
        }
    }

    private void activateFrightenedMode() {
        frightenedTicksLeft = FRIGHTENED_TICKS;
        ghostEatStreak = 0;
        for (Entity enemy : maze.getEnemies()) {
            enemy.eatenThisFright = false;
            if (enemy.returningHome || enemy.eatenScoreTicks > 0) {
                continue;
            }
            enemy.frightened = true;
            enemy.image = scaredSprite(false);
            if (enemy.image == null) {
                enemy.image = enemy.normalImage;
            }
            enemy.direction = maze.opposite(enemy.direction);
            enemy.tileRow = -1;
            enemy.tileCol = -1;
            maze.updateVelocity(enemy);
        }
    }

    private void clearFrightenedMode() {
        frightenedTicksLeft = 0;
        ghostEatStreak = 0;
        for (Entity enemy : maze.getEnemies()) {
            enemy.eatenThisFright = false;
            if (enemy.frightened && !enemy.returningHome) {
                enemy.frightened = false;
                if (enemy.normalImage != null) {
                    enemy.image = enemy.normalImage;
                }
                enemy.tileRow = -1;
                enemy.tileCol = -1;
            }
        }
    }

    private void updateFrightenedFlashSprites() {
        boolean flashing = frightenedTicksLeft <= FRIGHTENED_FLASH_TICKS;
        boolean whiteFrame = flashing && ((frightenedTicksLeft / 4) % 2 == 0);
        for (Entity enemy : maze.getEnemies()) {
            if (!enemy.frightened || enemy.returningHome) {
                continue;
            }
            Image sprite = scaredSprite(whiteFrame);
            if (sprite != null) {
                enemy.image = sprite;
            }
        }
    }

    private Image scaredSprite(boolean whiteFlash) {
        if (whiteFlash && assets.scaredEnemyFlash != null) {
            return assets.scaredEnemyFlash;
        }
        if (assets.scaredEnemy != null) {
            return assets.scaredEnemy;
        }
        return null;
    }

    private void handleHeroEnemyCollision(Entity enemy) {
        if (enemy.returningHome || enemy.eatenScoreTicks > 0) {
            return;
        }
        if (enemy.frightened) {
            int points = Math.min(80, 20 * (ghostEatStreak + 1));
            ghostEatStreak++;
            score += points;
            floatingScores.add(new FloatingScore(
                enemy.x + enemy.width / 2,
                enemy.y + enemy.height / 2,
                points,
                28
            ));
            enemy.frightened = false;
            enemy.eatenThisFright = true;
            enemy.returningHome = true;
            enemy.eatenScoreTicks = 0;
            enemy.image = null;
            enemy.tileRow = -1;
            enemy.tileCol = -1;
            int row = maze.rowAtCenter(enemy);
            int col = maze.colAtCenter(enemy);
            enemy.direction = ghostAi.shortestPathStep(
                enemy, row, col, maze.spawnRow(enemy), maze.spawnCol(enemy));
            maze.updateVelocity(enemy);
            return;
        }
        loseLife(false);
    }

    private void finishReturningHome(Entity enemy) {
        enemy.returningHome = false;
        enemy.eatenScoreTicks = 0;
        enemy.x = enemy.startX;
        enemy.y = enemy.startY;
        enemy.tileRow = -1;
        enemy.tileCol = -1;
        enemy.frightened = frightenedTicksLeft > 0 && !enemy.eatenThisFright;
        if (enemy.frightened) {
            boolean flashing = frightenedTicksLeft <= FRIGHTENED_FLASH_TICKS;
            boolean whiteFrame = flashing && ((frightenedTicksLeft / 4) % 2 == 0);
            Image sprite = scaredSprite(whiteFrame);
            enemy.image = sprite != null ? sprite : enemy.normalImage;
        } else if (enemy.normalImage != null) {
            enemy.image = enemy.normalImage;
        }
        enemy.direction = switch (enemy.personality) {
            case RED, BLUE -> 'L';
            case PINK, ORANGE -> 'R';
        };
        maze.updateVelocity(enemy);
    }

    private void tickFloatingScores() {
        floatingScores.removeIf(fs -> {
            fs.ticksLeft--;
            fs.y -= 1;
            return fs.ticksLeft <= 0;
        });
    }

    private void resetGhostSpeed() {
        ghostSpeedRatio = GHOST_SPEED_START;
        ghostMoveCredit = 0f;
    }

    private void tickGhostMoveCredit() {
        ghostMoveCredit += ghostSpeedRatio;
    }

    private void bumpGhostSpeedAfterMapClear() {
        ghostSpeedRatio = Math.min(GHOST_SPEED_MAX, ghostSpeedRatio + GHOST_SPEED_PER_MAP);
    }

    private void bumpPathRecalcAfterMapClear() {
        if (pathRecalcTiles > 20) {
            pathRecalcTiles = 20;
        } else if (pathRecalcTiles > 14) {
            pathRecalcTiles = 14;
        }
    }

    private void moveEnemiesOneStep() {
        Entity hero = maze.getHero();
        for (Entity enemy : maze.getEnemies()) {
            if (maze.collision(enemy, hero) && !enemy.returningHome) {
                handleHeroEnemyCollision(enemy);
                if (gameOver || isPaused) {
                    return;
                }
                continue;
            }

            if (enemy.returningHome) {
                moveReturningEnemy(enemy);
                continue;
            }

            if (enemy.frightened && random.nextBoolean()) {
                continue;
            }

            ghostAi.chooseEnemyDirection(
                enemy, hero, maze.getEnemies(), huntWarmupTicksLeft, pathRecalcTiles, scattering);
            enemy.x += enemy.velocityX;
            enemy.y += enemy.velocityY;
            maze.wrap(enemy);
            maze.lockToLane(enemy);

            boolean hitWall = false;
            for (Entity wall : maze.getWalls()) {
                if (maze.collision(enemy, wall)) {
                    enemy.x -= enemy.velocityX;
                    enemy.y -= enemy.velocityY;
                    maze.lockToLane(enemy);
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                int r = maze.rowAtCenter(enemy);
                int c = maze.colAtCenter(enemy);
                enemy.x = maze.laneX(c);
                enemy.y = maze.laneY(r);
                enemy.tileRow = -1;
                enemy.tileCol = -1;
                ghostAi.chooseEnemyDirection(
                    enemy, hero, maze.getEnemies(), huntWarmupTicksLeft, pathRecalcTiles, scattering, true);
            }

            if (maze.collision(enemy, hero) && !enemy.returningHome) {
                handleHeroEnemyCollision(enemy);
                if (gameOver || isPaused) {
                    return;
                }
            }
        }
    }

    private void moveReturningEnemy(Entity enemy) {
        int row = maze.rowAtCenter(enemy);
        int col = maze.colAtCenter(enemy);
        int goalR = maze.spawnRow(enemy);
        int goalC = maze.spawnCol(enemy);

        if (Math.abs(enemy.x - enemy.startX) <= maze.moveStep()
            && Math.abs(enemy.y - enemy.startY) <= maze.moveStep()) {
            finishReturningHome(enemy);
            return;
        }

        boolean newTile = row != enemy.tileRow || col != enemy.tileCol;
        if (newTile) {
            enemy.tileRow = row;
            enemy.tileCol = col;
            char best = ghostAi.shortestPathStep(enemy, row, col, goalR, goalC);
            if (best != enemy.direction && maze.isPerpendicular(enemy.direction, best)) {
                enemy.x = maze.laneX(col);
                enemy.y = maze.laneY(row);
            } else {
                maze.lockToLane(enemy);
            }
            enemy.direction = best;
            maze.updateVelocity(enemy);
        }

        enemy.x += enemy.velocityX;
        enemy.y += enemy.velocityY;
        maze.wrap(enemy);
        maze.lockToLane(enemy);

        boolean hit = false;
        for (Entity wall : maze.getWalls()) {
            if (maze.collision(enemy, wall)) {
                enemy.x -= enemy.velocityX;
                enemy.y -= enemy.velocityY;
                maze.lockToLane(enemy);
                hit = true;
                break;
            }
        }
        if (hit) {
            enemy.tileRow = -1;
            enemy.tileCol = -1;
        }

        if (Math.abs(enemy.x - enemy.startX) <= maze.moveStep()
            && Math.abs(enemy.y - enemy.startY) <= maze.moveStep()) {
            finishReturningHome(enemy);
        }
    }

    private void loseLife(boolean fromTimeout) {
        lives -= 1;
        timedOut = fromTimeout;
        clearFrightenedMode();
        clearBonus();
        if (lives == 0) {
            gameOver = true;
            return;
        }
        resetPositions();
        resetMazeTimer();
        isPaused = true;
    }

    private void resetPositions() {
        Entity hero = maze.getHero();
        hero.reset();
        hero.direction = 'R';
        nextDirection = 'R';
        hero.image = assets.heroRight;
        maze.updateVelocity(hero);
        hero.velocityX = 0;
        hero.velocityY = 0;
        for (Entity enemy : maze.getEnemies()) {
            enemy.reset();
            enemy.frightened = false;
            enemy.eatenThisFright = false;
            enemy.returningHome = false;
            enemy.eatenScoreTicks = 0;
            if (enemy.normalImage != null) {
                enemy.image = enemy.normalImage;
            }
            enemy.direction = switch (enemy.personality) {
                case RED, BLUE -> 'L';
                case PINK, ORANGE -> 'R';
            };
            maze.updateVelocity(enemy);
        }
        frightenedTicksLeft = 0;
        ghostEatStreak = 0;
        floatingScores.clear();
    }

    public Maze maze() { return maze; }
    public GameMap getGameMap() { return gameMap; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public boolean isGameOver() { return gameOver; }
    public boolean isPaused() { return isPaused; }
    public boolean isTimedOut() { return timedOut; }
    public boolean isAwaitingName() { return awaitingName; }
    public Entity getBonus() { return bonus; }
    public int getBonusEdibleTicks() { return bonusEdibleTicks; }
    public List<Image> getEatenBooks() { return eatenBooks; }
    public List<FloatingScore> getFloatingScores() { return floatingScores; }
}
