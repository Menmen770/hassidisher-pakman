package com.hasidicmaze.game;

import com.hasidicmaze.Theme;
import com.hasidicmaze.assets.AssetManager;
import com.hasidicmaze.map.GameMap;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Core maze gameplay panel — one map session at a time.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {
    public interface Listener {
        void onQuitToMenu();
        void onGameOver(int score, String mapId);
    }

    private final AssetManager assets = AssetManager.get();
    private final Listener listener;
    private GameMap gameMap;
    /** Per-map backdrop (same pixel size across stages so grid layout stays shared). */
    private Image sessionBackground;

    /**
     * Canvas matches bg.png aspect ratio (scaled to fit screen).
     * Set in startSession from the real image — never invented sizes.
     */
    private int gameAreaWidth = 640;
    private int boardHeight = 720;

    /** Gameplay grid cell size (adjust with [ ] — does not resize the background). */
    private int tileSize = 30;
    private int mapOriginX = 8;
    private int mapOriginY = 10;
    /** Shrink wall tiles relative to the cell (0 = full cell, matches dots + grid). */
    private final int wallShrinkPx = 0;
    /** Raise wall tiles upward (0 = same cell origin as dots + grid). */
    private final int wallRaisePx = 0;
    /** How much smaller hero/enemies are than a tile — needed to turn in corridors. */
    private final int actorInset = 4;
    private int rowCount;
    private int columnCount;

    private Set<Entity> walls;
    private Set<Entity> foods;
    private Set<Entity> enemies;
    private Entity hero;

    private final Timer gameLoop = new Timer(50, this);
    private final char[] directions = {'U', 'D', 'L', 'R'};
    private final Random random = new Random();

    private int score;
    private int totalFoodCount;
    private int lives = 3;
    private boolean gameOver;
    private boolean isPaused;
    private char nextDirection = 'R';
    private boolean awaitingName;

    private int closeButtonX;
    private int closeButtonY;
    private final int closeButtonSize = 14;
    /** Debug tile grid — toggle with G. Use to align mapOrigin with floor tiles in bg.png. */
    private boolean showTileGrid = true;
    /** Preview without blue wall tiles — toggle with H (collision stays). */
    private boolean showWallTiles = true;
    /** Shift background up (px) so the grid sits a bit lower on the floor art. */
    private final int bgShiftUp = 8;

    public GamePanel(Listener listener) {
        this.listener = listener;
        setBackground(Theme.INK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getX() >= closeButtonX && e.getX() <= closeButtonX + closeButtonSize
                    && e.getY() >= closeButtonY && e.getY() <= closeButtonY + closeButtonSize) {
                    stop();
                    listener.onQuitToMenu();
                }
            }
        });
    }

    public void startSession(GameMap map) {
        this.gameMap = map;
        this.rowCount = map.getRows();
        this.columnCount = map.getCols();
        this.sessionBackground = assets.backgroundFor(map.getBackgroundFile());
        if (this.sessionBackground == null) {
            this.sessionBackground = assets.background;
        }

        // Size the board from stage background; grid origin/size shared across same-size bgs
        fitCanvasToBackground();
        int maxTile = Math.min(
            Math.max(12, gameAreaWidth / Math.max(1, columnCount)),
            Math.max(12, boardHeight / Math.max(1, rowCount))
        );
        // Image size locked preference; grid fills canvas, sit as low as slack allows
        tileSize = maxTile;
        mapOriginX = Math.max(0, (gameAreaWidth - columnCount * tileSize) / 2);
        int slackY = Math.max(0, boardHeight - rowCount * tileSize);
        mapOriginY = slackY;

        Dimension size = new Dimension(gameAreaWidth, boardHeight);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setOpaque(true);

        score = 0;
        lives = 3;
        gameOver = false;
        // Wait for first arrow key so the board is visible and enemies don't rush the player
        isPaused = true;
        awaitingName = false;
        nextDirection = 'R';

        loadMap();
        resetPositions();
        for (Entity enemy : enemies) {
            enemy.direction = directions[random.nextInt(4)];
            enemy.velocityX = 0;
            enemy.velocityY = 0;
        }
        if (!gameLoop.isRunning()) {
            gameLoop.start();
        }
        revalidate();
        repaint();
    }

    /** Display stage background at fixed scale. Grid is independent and can be scaled separately. */
    private void fitCanvasToBackground() {
        Image bg = sessionBackground != null ? sessionBackground : assets.background;
        if (bg == null) {
            gameAreaWidth = 640;
            boardHeight = 720;
            return;
        }
        int bw = bg.getWidth(null);
        int bh = bg.getHeight(null);
        if (bw <= 0 || bh <= 0) {
            gameAreaWidth = 640;
            boardHeight = 720;
            return;
        }
        gameAreaWidth = Math.max(1, (int) Math.round(bw * 0.42));
        boardHeight = Math.max(1, (int) Math.round(bh * 0.42));
    }

    public void stop() {
        gameLoop.stop();
    }

    private void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        enemies = new HashSet<>();
        String[] tiles = gameMap.getTiles();

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
                    case 'b' -> enemies.add(createActor(assets.blueEnemy, x, y));
                    case 'o' -> enemies.add(createActor(assets.orangeEnemy, x, y));
                    case 'p' -> enemies.add(createActor(assets.pinkEnemy, x, y));
                    case 'r' -> enemies.add(createActor(assets.redEnemy, x, y));
                    case 'P' -> hero = createActor(assets.heroRight, x, y);
                    case ' ' -> {
                        // No pellets on the outer frame / tunnel mouths (those sit on the cabinets)
                        if (!isCabinetFrameCell(r, c)) {
                            foods.add(new Entity(null, x + tileSize / 2 - 2, y + tileSize / 2 - 2, 4, 4));
                        }
                    }
                    default -> { /* empty path O or unknown */ }
                }
            }
        }
        totalFoodCount = foods.size();
    }

    /** Outer map rim sits on the bookshelf art — no collectibles there. */
    private boolean isCabinetFrameCell(int r, int c) {
        return r == 0 || r == rowCount - 1 || c == 0 || c == columnCount - 1;
    }

    private int actorSize() {
        return Math.max(12, tileSize - actorInset * 2);
    }

    private int actorPad() {
        return (tileSize - actorSize()) / 2;
    }

    /** Movement step that always lands on tile centers (no mid-cell drift). */
    private int moveStep() {
        int step = Math.max(1, tileSize / 4);
        while (step > 1 && tileSize % step != 0) {
            step--;
        }
        return step;
    }

    private Entity createActor(java.awt.Image image, int cellX, int cellY) {
        int size = actorSize();
        int pad = actorPad();
        return new Entity(image, cellX + pad, cellY + pad, size, size);
    }

    private boolean hitsWallAt(int x, int y, int w, int h) {
        for (Entity wall : walls) {
            if (x < wall.x + wall.width && x + w > wall.x
                && y < wall.y + wall.height && y + h > wall.y) {
                return true;
            }
        }
        return false;
    }

    private int laneX(int col) {
        return mapOriginX + col * tileSize + actorPad();
    }

    private int laneY(int row) {
        return mapOriginY + row * tileSize + actorPad();
    }

    private int colAtCenter(Entity actor) {
        float centerX = actor.x + actor.width / 2f;
        return Math.max(0, Math.min(columnCount - 1,
            (int) Math.floor((centerX - mapOriginX) / tileSize)));
    }

    private int rowAtCenter(Entity actor) {
        float centerY = actor.y + actor.height / 2f;
        return Math.max(0, Math.min(rowCount - 1,
            (int) Math.floor((centerY - mapOriginY) / tileSize)));
    }

    /** Keep the axis perpendicular to travel locked on the tile lane. */
    private void lockToLane(Entity actor) {
        if (actor.velocityX != 0 && actor.velocityY == 0) {
            actor.y = laneY(rowAtCenter(actor));
        } else if (actor.velocityY != 0 && actor.velocityX == 0) {
            actor.x = laneX(colAtCenter(actor));
        }
    }

    /** True when actor is centered enough to take a perpendicular turn. */
    private boolean canTurn(Entity actor, char turn) {
        int threshold = Math.max(2, moveStep() / 2);
        if (turn == 'U' || turn == 'D') {
            return Math.abs(actor.x - laneX(colAtCenter(actor))) <= threshold;
        }
        if (turn == 'L' || turn == 'R') {
            return Math.abs(actor.y - laneY(rowAtCenter(actor))) <= threshold;
        }
        return false;
    }

    /** Snap onto the lane for a turn; only call when canTurn is true. */
    private void snapToLaneForTurn(Entity actor, char turn) {
        if (turn == 'U' || turn == 'D') {
            actor.x = laneX(colAtCenter(actor));
        } else if (turn == 'L' || turn == 'R') {
            actor.y = laneY(rowAtCenter(actor));
        }
    }

    private void updateVelocity(Entity entity) {
        int step = moveStep();
        switch (entity.direction) {
            case 'U' -> { entity.velocityX = 0; entity.velocityY = -step; }
            case 'D' -> { entity.velocityX = 0; entity.velocityY = step; }
            case 'L' -> { entity.velocityX = -step; entity.velocityY = 0; }
            case 'R' -> { entity.velocityX = step; entity.velocityY = 0; }
            default -> { entity.velocityX = 0; entity.velocityY = 0; }
        }
    }

    private void tryUpdateDirection(Entity entity, char direction) {
        if (!canTurn(entity, direction) && isPerpendicular(entity.direction, direction)) {
            return;
        }
        char prev = entity.direction;
        int saveX = entity.x;
        int saveY = entity.y;
        snapToLaneForTurn(entity, direction);
        entity.direction = direction;
        updateVelocity(entity);
        entity.x += entity.velocityX;
        entity.y += entity.velocityY;
        for (Entity wall : walls) {
            if (collision(entity, wall)) {
                entity.x = saveX;
                entity.y = saveY;
                entity.direction = prev;
                updateVelocity(entity);
                return;
            }
        }
        entity.x = saveX;
        entity.y = saveY;
        snapToLaneForTurn(entity, direction);
        updateVelocity(entity);
    }

    private boolean isPerpendicular(char a, char b) {
        boolean aHoriz = a == 'L' || a == 'R';
        boolean bHoriz = b == 'L' || b == 'R';
        return aHoriz != bHoriz;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameMap == null || walls == null || hero == null) {
            g.setColor(Theme.INK);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Scale board to fill the panel (CardLayout may stretch the host)
        int panelW = Math.max(getWidth(), 1);
        int panelH = Math.max(getHeight(), 1);
        double scale = Math.min(panelW / (double) gameAreaWidth, panelH / (double) boardHeight);
        int drawW = (int) Math.round(gameAreaWidth * scale);
        int drawH = (int) Math.round(boardHeight * scale);
        int ox = (panelW - drawW) / 2;
        int oy = (panelH - drawH) / 2;

        g2.setColor(Theme.INK);
        g2.fillRect(0, 0, panelW, panelH);
        g2.translate(ox, oy);
        g2.scale(scale, scale);

        Image bg = sessionBackground != null ? sessionBackground : assets.background;
        if (bg != null) {
            // Shift art up slightly so the maze sits lower on the floor (size unchanged)
            g2.drawImage(bg, 0, -bgShiftUp, gameAreaWidth, boardHeight + bgShiftUp, null);
        } else {
            g2.setColor(Theme.INK_SOFT);
            g2.fillRect(0, 0, gameAreaWidth, boardHeight);
        }

        drawHud(g2);

        g2.drawImage(hero.image, hero.x, hero.y, hero.width, hero.height, null);
        for (Entity enemy : enemies) {
            if (enemy.image != null) {
                g2.drawImage(enemy.image, enemy.x, enemy.y, enemy.width, enemy.height, null);
            }
        }
        for (Entity wall : walls) {
            if (!showWallTiles) {
                continue;
            }
            if (wall.image != null) {
                g2.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
            } else {
                g2.setColor(Theme.NAVY);
                g2.fillRect(wall.x, wall.y, wall.width, wall.height);
            }
        }
        g2.setColor(new Color(20, 20, 20));
        for (Entity food : foods) {
            g2.fillOval(food.x, food.y, food.width, food.height);
        }

        if (showTileGrid) {
            drawTileGrid(g2);
        }

        if (gameOver) {
            drawOverlay(g2, "!המשחק נגמר", "ניקוד סופי: " + score, "ENTER — שמירה וחזרה לתפריט");
        } else if (isPaused && lives < 3) {
            drawOverlay(g2, "זהירות!", "נותרו " + lives + " חיים", "הזז עם החיצים להמשך");
        } else if (isPaused && !showTileGrid) {
            drawStartHint(g2);
        }
        g2.dispose();
    }

    private void drawTileGrid(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Simple net locked to the SAME cells as blue walls + food dots
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(40, 40, 40, 180));

        for (int c = 0; c <= columnCount; c++) {
            int x = mapOriginX + c * tileSize;
            g2.drawLine(x, mapOriginY, x, mapOriginY + rowCount * tileSize);
        }
        for (int r = 0; r <= rowCount; r++) {
            int y = mapOriginY + r * tileSize;
            g2.drawLine(mapOriginX, y, mapOriginX + columnCount * tileSize, y);
        }

        g2.setColor(new Color(0, 0, 0, 160));
        g2.setFont(Theme.bodyBold(11));
        g2.drawString("[ ] רשת | IJKL הזזה | G רשת | H הסתר הכל | tile=" + tileSize
            + " origin=" + mapOriginX + "," + mapOriginY, 8, 16);
        g2.dispose();
    }

    private void nudgeLayout(int dTile, int dOx, int dOy) {
        int maxTileW = Math.max(12, gameAreaWidth / Math.max(1, columnCount));
        int maxTileH = Math.max(12, boardHeight / Math.max(1, rowCount));
        int maxTile = Math.min(maxTileW, maxTileH);
        int newTile = Math.max(12, Math.min(maxTile, tileSize + dTile));
        int newOx = Math.max(0, Math.min(Math.max(0, gameAreaWidth - columnCount * newTile), mapOriginX + dOx));
        int newOy = Math.max(0, Math.min(Math.max(0, boardHeight - rowCount * newTile), mapOriginY + dOy));
        if (newTile == tileSize && newOx == mapOriginX && newOy == mapOriginY) {
            return;
        }
        tileSize = newTile;
        mapOriginX = newOx;
        mapOriginY = newOy;
        applyLayoutAndReload();
    }

    private void applyLayoutAndReload() {
        // Only rebuild entities — background / window size stay put
        loadMap();
        resetPositions();
        for (Entity enemy : enemies) {
            enemy.velocityX = 0;
            enemy.velocityY = 0;
        }
        isPaused = true;
        requestFocusInWindow();
        repaint();
    }

    private void drawStartHint(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(gameAreaWidth / 2 - 160, boardHeight / 2 - 28, 320, 56, 12, 12);
        g.setColor(Theme.GOLD_BRIGHT);
        g.setFont(Theme.bodyBold(20));
        centerText(g, "לחץ חץ כדי להתחיל", boardHeight / 2 + 8);
    }

    private void drawHud(Graphics2D g) {
        closeButtonX = 35;
        closeButtonY = 3;
        g.setColor(Theme.ACCENT);
        g.fillRoundRect(closeButtonX, closeButtonY, closeButtonSize, closeButtonSize, 4, 4);
        g.setColor(Color.WHITE);
        g.setFont(Theme.bodyBold(11));
        g.drawString("X", closeButtonX + 4, closeButtonY + 11);

        int startX = 75;
        g.setColor(Theme.INK);
        g.setFont(Theme.bodyBold(13));
        String scoreStr = score + " / " + totalFoodCount;
        g.drawString(scoreStr, startX, 18);
        int scoreWidth = g.getFontMetrics().stringWidth(scoreStr);

        int heartSize = 16;
        int heartStartX = startX + scoreWidth + 15;
        for (int i = 0; i < lives; i++) {
            int hx = heartStartX + i * 20;
            if (assets.heart != null) {
                g.drawImage(assets.heart, hx, 5, heartSize, heartSize, null);
            } else {
                drawHeart(g, hx, 5, heartSize);
            }
        }

        g.setColor(Theme.GOLD);
        g.setFont(Theme.bodyBold(12));
        g.drawString(gameMap.getTitle(), gameAreaWidth - 12 - g.getFontMetrics().stringWidth(gameMap.getTitle()), 18);
    }

    private void drawOverlay(Graphics2D g, String title, String line2, String line3) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Theme.GOLD_BRIGHT);
        g.setFont(Theme.display(40));
        centerText(g, title, boardHeight / 2 - 36);
        g.setColor(Color.WHITE);
        g.setFont(Theme.bodyBold(24));
        centerText(g, line2, boardHeight / 2 + 10);
        g.setColor(Theme.MUTED);
        g.setFont(Theme.body(16));
        centerText(g, line3, boardHeight / 2 + 48);
    }

    private void centerText(Graphics g, String text, int y) {
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (gameAreaWidth - w) / 2, y);
    }

    private void drawHeart(Graphics2D g, int x, int y, int size) {
        g.setColor(Theme.ACCENT);
        int[] xPoints = {x + size / 2, x + (int) (size * 0.2), x, x, x + size / 2, x + size, x + size, x + (int) (size * 0.8), x + size / 2};
        int[] yPoints = {y + (int) (size * 0.3), y + (int) (size * 0.1), y + (int) (size * 0.1), y + (int) (size * 0.4), y + size, y + (int) (size * 0.4), y + (int) (size * 0.1), y + (int) (size * 0.1), y + (int) (size * 0.3)};
        g.fillPolygon(xPoints, yPoints, 9);
    }

    private void move() {
        if (isPaused || gameOver || hero == null) {
            return;
        }

        if (nextDirection != hero.direction) {
            boolean reverse = isOpposite(hero.direction, nextDirection);
            boolean turnOk = reverse || canTurn(hero, nextDirection);
            if (turnOk) {
                char old = hero.direction;
                int saveX = hero.x;
                int saveY = hero.y;
                snapToLaneForTurn(hero, nextDirection);
                hero.direction = nextDirection;
                updateVelocity(hero);
                int testX = hero.x + hero.velocityX;
                int testY = hero.y + hero.velocityY;
                if (hitsWallAt(testX, testY, hero.width, hero.height)) {
                    hero.x = saveX;
                    hero.y = saveY;
                    hero.direction = old;
                    updateVelocity(hero);
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
        wrap(hero);
        lockToLane(hero);

        for (Entity wall : walls) {
            if (collision(hero, wall)) {
                hero.x -= hero.velocityX;
                hero.y -= hero.velocityY;
                lockToLane(hero);
                break;
            }
        }

        for (Entity enemy : enemies) {
            if (collision(enemy, hero)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    gameLoop.stop();
                    return;
                }
                resetPositions();
                isPaused = true;
                break;
            }

            enemy.x += enemy.velocityX;
            enemy.y += enemy.velocityY;
            wrap(enemy);
            lockToLane(enemy);

            for (Entity wall : walls) {
                if (collision(enemy, wall)) {
                    enemy.x -= enemy.velocityX;
                    enemy.y -= enemy.velocityY;
                    lockToLane(enemy);
                    tryUpdateDirection(enemy, directions[random.nextInt(4)]);
                    break;
                }
            }
        }

        Entity eaten = null;
        for (Entity food : foods) {
            if (collision(hero, food)) {
                eaten = food;
                score += 1;
            }
        }
        if (eaten != null) {
            foods.remove(eaten);
        }
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    private boolean isOpposite(char a, char b) {
        return (a == 'U' && b == 'D') || (a == 'D' && b == 'U')
            || (a == 'L' && b == 'R') || (a == 'R' && b == 'L');
    }

    private void wrap(Entity e) {
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

    private boolean collision(Entity a, Entity b) {
        return a.x < b.x + b.width && a.x + a.width > b.x
            && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    private void resetPositions() {
        hero.reset();
        hero.direction = 'R';
        nextDirection = 'R';
        hero.image = assets.heroRight;
        updateVelocity(hero);
        hero.velocityX = 0;
        hero.velocityY = 0;
        for (Entity enemy : enemies) {
            enemy.reset();
            enemy.direction = directions[random.nextInt(4)];
            updateVelocity(enemy);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                finishGame();
            }
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            stop();
            listener.onQuitToMenu();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_G) {
            showTileGrid = !showTileGrid;
            repaint();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_H) {
            showWallTiles = !showWallTiles;
            showTileGrid = showWallTiles;
            repaint();
            return;
        }
        // Live alignment controls (while grid is visible)
        if (showTileGrid) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_OPEN_BRACKET -> { nudgeLayout(-1, 0, 0); return; }
                case KeyEvent.VK_CLOSE_BRACKET -> { nudgeLayout(1, 0, 0); return; }
                case KeyEvent.VK_I -> { nudgeLayout(0, 0, -1); return; }
                case KeyEvent.VK_K -> { nudgeLayout(0, 0, 1); return; }
                case KeyEvent.VK_J -> { nudgeLayout(0, -1, 0); return; }
                case KeyEvent.VK_L -> { nudgeLayout(0, 1, 0); return; }
                default -> {}
            }
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> resumeWithDirection('U');
            case KeyEvent.VK_DOWN -> resumeWithDirection('D');
            case KeyEvent.VK_LEFT -> resumeWithDirection('L');
            case KeyEvent.VK_RIGHT -> resumeWithDirection('R');
            default -> {}
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    private void resumeWithDirection(char direction) {
        nextDirection = direction;
        if (isPaused) {
            isPaused = false;
            for (Entity enemy : enemies) {
                updateVelocity(enemy);
            }
        }
    }

    private void finishGame() {
        if (awaitingName) {
            return;
        }
        awaitingName = true;
        listener.onGameOver(score, gameMap.getId());
    }
}
