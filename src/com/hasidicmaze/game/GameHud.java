package com.hasidicmaze.game;

import com.hasidicmaze.Theme;
import com.hasidicmaze.assets.AssetManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

/**
 * HUD, overlays, and board sprites. Mutates back-button bounds for hit-testing.
 */
public final class GameHud {
    public int backButtonX;
    public int backButtonY;
    public int backButtonW = 88;
    public int backButtonH = 32;

    private final AssetManager assets;
    private final int bgShiftUp = 8;

    public GameHud(AssetManager assets) {
        this.assets = assets;
    }

    public void paintWorld(
        Graphics2D g2,
        GameSession session,
        Image sessionBackground,
        boolean showWallTiles,
        boolean showTileGrid
    ) {
        Maze maze = session.maze();
        int gameAreaWidth = maze.getGameAreaWidth();
        int boardHeight = maze.getBoardHeight();

        Image bg = sessionBackground != null ? sessionBackground : assets.background;
        if (bg != null) {
            g2.drawImage(bg, 0, -bgShiftUp, gameAreaWidth, boardHeight + bgShiftUp, null);
        } else {
            g2.setColor(Theme.INK_SOFT);
            g2.fillRect(0, 0, gameAreaWidth, boardHeight);
        }

        Entity hero = maze.getHero();
        g2.drawImage(hero.image, hero.x, hero.y, hero.width, hero.height, null);
        for (Entity enemy : maze.getEnemies()) {
            if (enemy.returningHome) {
                drawGhostEyes(g2, enemy);
            } else if (enemy.image != null) {
                g2.drawImage(enemy.image, enemy.x, enemy.y, enemy.width, enemy.height, null);
            }
        }
        for (Entity wall : maze.getWalls()) {
            if (!showWallTiles) {
                continue;
            }
            if (wall.image != null) {
                g2.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
            } else {
                g2.setColor(Theme.WOOD_DEEP);
                g2.fillRect(wall.x, wall.y, wall.width, wall.height);
            }
        }
        for (Entity food : maze.getFoods()) {
            if (food.powerPellet && food.image != null) {
                g2.drawImage(food.image, food.x, food.y, food.width, food.height, null);
            } else {
                // Logo cut-blue pellets
                int px = food.x;
                int py = food.y;
                int pw = food.width;
                int ph = food.height;
                g2.setColor(Theme.withAlpha(Theme.INK, 120));
                g2.fillOval(px + 1, py + 1, pw, ph);
                g2.setColor(Theme.PELLET);
                g2.fillOval(px, py, pw, ph);
            }
        }
        if (session.getBonus() != null && session.getBonus().image != null && session.getBonusEdibleTicks() > 0) {
            drawBonusBook(g2, session);
        }

        drawFloatingScores(g2, session);

        if (showTileGrid) {
            drawTileGrid(g2, maze);
        }

        if (session.isGameOver()) {
            drawOverlay(g2, maze, "!המשחק נגמר", "ניקוד סופי: " + session.getScore(), "ENTER — שמירה וחזרה לתפריט");
        } else if (session.isPaused() && session.getLives() < 3) {
            if (session.isTimedOut()) {
                drawOverlay(g2, maze, "הזמן נגמר!", "נותרו " + session.getLives() + " חיים", "הזז עם החיצים להמשך");
            } else {
                drawOverlay(g2, maze, "זהירות!", "נותרו " + session.getLives() + " חיים", "הזז עם החיצים להמשך");
            }
        } else if (session.isPaused() && !showTileGrid) {
            drawStartHint(g2, maze);
        }

        drawSideBar(g2, session);
    }

    private void drawTileGrid(Graphics2D g, Maze maze) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(Theme.withAlpha(Theme.WOOD_EDGE, 120));
        int columnCount = maze.getColumnCount();
        int rowCount = maze.getRowCount();
        int tileSize = maze.getTileSize();
        int mapOriginX = maze.getMapOriginX();
        int mapOriginY = maze.getMapOriginY();

        for (int c = 0; c <= columnCount; c++) {
            int x = mapOriginX + c * tileSize;
            g2.drawLine(x, mapOriginY, x, mapOriginY + rowCount * tileSize);
        }
        for (int r = 0; r <= rowCount; r++) {
            int y = mapOriginY + r * tileSize;
            g2.drawLine(mapOriginX, y, mapOriginX + columnCount * tileSize, y);
        }

        g2.setColor(Theme.withAlpha(Theme.INK, 180));
        g2.setFont(Theme.bodyBold(11));
        g2.drawString("[ ] רשת | IJKL הזזה | G רשת | H הצג/הסתר | tile=" + tileSize
            + " origin=" + mapOriginX + "," + mapOriginY, 8, 16);
        g2.dispose();
    }

    private void drawStartHint(Graphics2D g, Maze maze) {
        int gameAreaWidth = maze.getGameAreaWidth();
        int boardHeight = maze.getBoardHeight();
        int boxW = 340;
        int boxH = 64;
        int bx = gameAreaWidth / 2 - boxW / 2;
        int by = boardHeight / 2 - boxH / 2;
        g.setColor(Theme.withAlpha(Theme.PANEL_SOLID, 245));
        int arc = boxH;
        g.fillRoundRect(bx, by, boxW, boxH, arc, arc);
        g.setColor(Theme.BG_GOLD);
        g.setStroke(new BasicStroke(2.2f));
        g.drawRoundRect(bx, by, boxW - 1, boxH - 1, arc, arc);
        g.setColor(Theme.CREAM);
        g.setFont(Theme.bodyBold(19));
        centerText(g, maze, "לחץ חץ כדי להתחיל", boardHeight / 2 + 7);
    }

    private void drawSideBar(Graphics2D g, GameSession session) {
        Maze maze = session.maze();
        int gameAreaWidth = maze.getGameAreaWidth();
        int boardHeight = maze.getBoardHeight();
        int x0 = gameAreaWidth;
        int sw = GameSession.SIDE_W;

        g.setColor(Theme.INK_SOFT);
        g.fillRect(x0, 0, sw, boardHeight);

        g.setColor(Theme.BG_GOLD);
        g.fillRect(x0, 0, 3, boardHeight);

        int cx = x0 + sw / 2;

        int heartSize = 26;
        int gap = 10;
        int heartStartY = 28;
        for (int i = 0; i < session.getLives(); i++) {
            int hx = cx - heartSize / 2;
            int hy = heartStartY + i * (heartSize + gap);
            if (assets.heart != null) {
                g.drawImage(assets.heart, hx, hy, heartSize, heartSize, null);
            } else {
                drawHeart(g, hx, hy, heartSize);
            }
        }

        g.setColor(Theme.PAC_YELLOW);
        g.setFont(Theme.mono(24));
        String scoreStr = String.valueOf(session.getScore());
        int scoreW = g.getFontMetrics().stringWidth(scoreStr);
        int scoreY = boardHeight / 2 + 8;
        g.drawString(scoreStr, cx - scoreW / 2, scoreY);

        g.setColor(Theme.MUTED);
        g.setFont(Theme.body(12));
        String pts = "נקודות";
        int pw = g.getFontMetrics().stringWidth(pts);
        g.drawString(pts, cx - pw / 2, scoreY + 20);

        drawEatenBooks(g, session, x0, scoreY + 48);

        backButtonW = 88;
        backButtonH = 36;
        backButtonX = x0 + (sw - backButtonW) / 2;
        backButtonY = boardHeight - backButtonH - 22;
        drawMazeTimer(g, session, cx, backButtonY - 12);

        int arc = backButtonH;
        g.setColor(Theme.withAlpha(Theme.BG_BARK, 120));
        g.fillRoundRect(backButtonX + 2, backButtonY + 2, backButtonW - 2, backButtonH - 2, arc, arc);
        g.setColor(Theme.withAlpha(Theme.BG_NAVY, 230));
        g.fillRoundRect(backButtonX, backButtonY, backButtonW - 1, backButtonH - 1, arc, arc);
        g.setColor(Theme.BG_TAN);
        g.setStroke(new BasicStroke(1.8f));
        g.drawRoundRect(backButtonX, backButtonY, backButtonW - 2, backButtonH - 2, arc, arc);
        g.setColor(Theme.CREAM);
        g.setFont(Theme.bodyBold(14));
        String back = "חזרה";
        int bw = g.getFontMetrics().stringWidth(back);
        g.drawString(back, backButtonX + (backButtonW - bw) / 2 - 1, backButtonY + 24);
    }

    private void drawEatenBooks(Graphics2D g, GameSession session, int sideLeft, int startY) {
        if (session.getEatenBooks().isEmpty()) {
            return;
        }
        int icon = 22;
        int gap = 4;
        int cols = 3;
        int padRight = 8;
        int right = sideLeft + GameSession.SIDE_W - padRight;
        for (int i = 0; i < session.getEatenBooks().size(); i++) {
            Image img = session.getEatenBooks().get(i);
            if (img == null) {
                continue;
            }
            int col = i % cols;
            int row = i / cols;
            int x = right - (col + 1) * icon - col * gap;
            int y = startY + row * (icon + gap);
            g.drawImage(img, x, y, icon, icon, null);
        }
    }

    private void drawMazeTimer(Graphics2D g, GameSession session, int cx, int baselineY) {
        int secs = session.mazeRemainingSeconds();
        int mm = secs / 60;
        int ss = secs % 60;
        String time = String.format("%d:%02d", mm, ss);
        Color color = secs <= 10 ? Theme.ACCENT : (secs <= 25 ? Theme.PAC_ORANGE : Theme.CREAM_SOFT);
        g.setColor(color);
        g.setFont(Theme.mono(13));
        int tw = g.getFontMetrics().stringWidth(time);
        g.drawString(time, cx - tw / 2, baselineY);
    }

    private void drawOverlay(Graphics2D g, Maze maze, String title, String line2, String line3) {
        int gameAreaWidth = maze.getGameAreaWidth();
        int boardHeight = maze.getBoardHeight();
        g.setColor(Theme.withAlpha(Theme.INK, 175));
        g.fillRect(0, 0, gameAreaWidth, boardHeight);
        int cardW = Math.min(420, gameAreaWidth - 40);
        int cardH = 180;
        int cx = (gameAreaWidth - cardW) / 2;
        int cy = boardHeight / 2 - cardH / 2;
        g.setColor(Theme.withAlpha(Theme.INK_SOFT, 245));
        int arc = 24;
        g.fillRoundRect(cx, cy, cardW, cardH, arc, arc);
        g.setColor(Theme.BG_GOLD);
        g.setStroke(new BasicStroke(2.4f));
        g.drawRoundRect(cx + 1, cy + 1, cardW - 3, cardH - 3, arc, arc);
        g.setColor(Theme.BG_GOLD);
        g.setFont(Theme.display(32));
        centerText(g, maze, title, cy + 58);
        g.setColor(Theme.CREAM);
        g.setFont(Theme.bodyBold(21));
        centerText(g, maze, line2, cy + 100);
        g.setColor(Theme.BG_STEEL);
        g.setFont(Theme.body(14));
        centerText(g, maze, line3, cy + 138);
    }

    private void centerText(Graphics g, Maze maze, String text, int y) {
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (maze.getGameAreaWidth() - w) / 2, y);
    }

    private void drawHeart(Graphics2D g, int x, int y, int size) {
        g.setColor(Theme.ACCENT);
        int[] xPoints = {x + size / 2, x + (int) (size * 0.2), x, x, x + size / 2, x + size, x + size, x + (int) (size * 0.8), x + size / 2};
        int[] yPoints = {y + (int) (size * 0.3), y + (int) (size * 0.1), y + (int) (size * 0.1), y + (int) (size * 0.4), y + size, y + (int) (size * 0.4), y + (int) (size * 0.1), y + (int) (size * 0.1), y + (int) (size * 0.3)};
        g.fillPolygon(xPoints, yPoints, 9);
    }

    private void drawBonusBook(Graphics2D g2, GameSession session) {
        Entity bonus = session.getBonus();
        double t = session.getBonusEdibleTicks() * 0.40;
        double scale = 0.90 + 0.10 * (0.5 + 0.5 * Math.sin(t));
        int drawW = Math.max(12, (int) Math.round(bonus.width * scale));
        int drawH = Math.max(12, (int) Math.round(bonus.height * scale));
        int cx = bonus.x + bonus.width / 2;
        int cy = bonus.y + bonus.height / 2;
        int base = Math.max(drawW, drawH);

        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int layers = 8;
        for (int i = layers; i >= 1; i--) {
            float f = i / (float) layers;
            int r = (int) (base * (0.95 + 0.75 * f));
            int alpha = (int) (14 + 48 * (1f - f) * (1f - f));
            int red = 212;
            int green = (int) (168 + 30 * (1f - f));
            int blue = (int) (72 + 40 * (1f - f));
            g.setColor(new Color(red, green, blue, alpha));
            g.fillOval(cx - r / 2, cy - r / 2, r, r);
        }

        g.drawImage(bonus.image, cx - drawW / 2, cy - drawH / 2, drawW, drawH, null);
        g.dispose();
    }

    private void drawGhostEyes(Graphics2D g, Entity enemy) {
        if (assets.ghostEyes != null) {
            g.drawImage(assets.ghostEyes, enemy.x, enemy.y, enemy.width, enemy.height, null);
            return;
        }
        int cx = enemy.x + enemy.width / 2;
        int cy = enemy.y + enemy.height / 2;
        int eyeW = Math.max(4, enemy.width / 4);
        int eyeH = Math.max(5, enemy.height / 3);
        int gap = Math.max(2, eyeW / 2);
        g.setColor(Theme.CREAM);
        g.fillOval(cx - gap - eyeW, cy - eyeH / 2, eyeW, eyeH);
        g.fillOval(cx + gap, cy - eyeH / 2, eyeW, eyeH);
    }

    private void drawFloatingScores(Graphics2D g, GameSession session) {
        Maze maze = session.maze();
        g.setFont(Theme.bodyBold(Math.max(14, maze.getTileSize() / 2)));
        for (FloatingScore fs : session.getFloatingScores()) {
            String text = String.valueOf(fs.getPoints());
            int tw = g.getFontMetrics().stringWidth(text);
            int alpha = Math.min(255, 80 + fs.getTicksLeft() * 8);
            g.setColor(new Color(Theme.CREAM.getRed(), Theme.CREAM.getGreen(), Theme.CREAM.getBlue(), alpha));
            g.drawString(text, fs.getX() - tw / 2, fs.getY() - (28 - fs.getTicksLeft()));
        }
    }
}
