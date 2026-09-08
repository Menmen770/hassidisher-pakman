package com.hasidicmaze.game;

import com.hasidicmaze.Theme;
import com.hasidicmaze.assets.AssetManager;
import com.hasidicmaze.sound.SoundManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;

/**
 * HUD, overlays, and board sprites. Mutates back/mute button bounds for hit-testing.
 */
public final class GameHud {
    public int backButtonX;
    public int backButtonY;
    public int backButtonW = 88;
    public int backButtonH = 32;
    public int muteButtonX;
    public int muteButtonY;
    public int muteButtonW = 88;
    public int muteButtonH = 28;

    private final AssetManager assets;
    private final int bgShiftUp = 0;

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
            if (food.extraLife && food.image != null) {
                g2.drawImage(food.image, food.x, food.y, food.width, food.height, null);
            } else if (food.powerPellet && food.image != null) {
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

        if (session.isLevelClearFlashing()) {
            drawLevelClearFlash(g2, maze, session.levelClearFlashPulse());
        }

        if (showTileGrid) {
            drawTileGrid(g2, maze);
        }

        if (session.isGameOver()) {
            drawOverlay(g2, maze, "!המשחק נגמר", "ניקוד סופי: " + session.getScore(), "ENTER — שמירה וחזרה לתפריט");
        } else if (session.isUserPaused()) {
            drawPauseOverlay(g2, maze);
        } else if (session.isPaused() && session.getLives() < 3 && !session.isLevelClearFlashing()) {
            if (session.isTimedOut()) {
                drawOverlay(g2, maze, "הזמן נגמר!", "נותרו " + session.getLives() + " חיים", "הזז עם החיצים להמשך");
            } else {
                drawOverlay(g2, maze, "זהירות!", "נותרו " + session.getLives() + " חיים", "הזז עם החיצים להמשך");
            }
        } else if (session.isPaused() && !showTileGrid && !session.isLevelClearFlashing()) {
            drawStartHint(g2, maze);
        }

        drawSideBar(g2, session);
    }

    private void drawLevelClearFlash(Graphics2D g, Maze maze, float pulse) {
        int gameAreaWidth = maze.getGameAreaWidth();
        int boardHeight = maze.getBoardHeight();
        int alpha = Math.max(0, Math.min(255, (int) (pulse * 255)));
        g.setColor(new Color(Theme.BG_GOLD.getRed(), Theme.BG_GOLD.getGreen(), Theme.BG_GOLD.getBlue(), alpha));
        g.fillRect(0, 0, gameAreaWidth, boardHeight);
        g.setColor(Theme.CREAM);
        g.setFont(Theme.display(36));
        centerText(g, maze, "!כל הכבוד", boardHeight / 2 - 8);
        g.setFont(Theme.bodyBold(16));
        g.setColor(Theme.BG_TAN);
        centerText(g, maze, "השלב הושלם", boardHeight / 2 + 28);
    }

    private void drawPauseOverlay(Graphics2D g, Maze maze) {
        int gameAreaWidth = maze.getGameAreaWidth();
        int boardHeight = maze.getBoardHeight();
        g.setColor(Theme.withAlpha(Theme.INK, 160));
        g.fillRect(0, 0, gameAreaWidth, boardHeight);
        int boxW = 300;
        int boxH = 88;
        int bx = gameAreaWidth / 2 - boxW / 2;
        int by = boardHeight / 2 - boxH / 2;
        g.setColor(Theme.withAlpha(Theme.TITLE_BAR, 245));
        g.fillRoundRect(bx, by, boxW, boxH, 28, 28);
        g.setColor(Theme.BG_GOLD);
        g.setStroke(new BasicStroke(2.2f));
        g.drawRoundRect(bx, by, boxW - 1, boxH - 1, 28, 28);
        g.setColor(Theme.BG_GOLD);
        g.setFont(Theme.display(30));
        centerText(g, maze, "השהיה", boardHeight / 2 - 4);
        g.setColor(Theme.CREAM_SOFT);
        g.setFont(Theme.body(14));
        centerText(g, maze, "P — המשך   ·   M — השתקה", boardHeight / 2 + 26);
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

        // Slight overfill so scaled letterbox never shows a 1px seam
        g.setPaint(new LinearGradientPaint(
            x0, 0, x0 + sw, 0,
            new float[]{0f, 1f},
            new Color[]{Theme.TITLE_BAR, Theme.BG_NAVY}
        ));
        g.fillRect(x0, 0, sw + 2, boardHeight);

        final int rail = 3;
        final int pad = 16;
        final int heart = 28;
        final int heartGap = 10;
        final int maxLives = 5;

        g.setColor(Theme.BG_GOLD);
        g.fillRect(x0, 0, rail, boardHeight);

        int innerL = x0 + rail;
        int innerW = sw - rail;
        int cx = innerL + innerW / 2;

        // Lives — fixed slots from the top so gaps stay even
        int livesTop = pad;
        int lives = Math.min(session.getLives(), maxLives);
        for (int i = 0; i < lives; i++) {
            int hx = cx - heart / 2;
            int hy = livesTop + i * (heart + heartGap);
            if (assets.heart != null) {
                g.drawImage(assets.heart, hx, hy, heart, heart, null);
            } else {
                drawHeart(g, hx, hy, heart);
            }
        }
        int livesBottom = livesTop + maxLives * heart + (maxLives - 1) * heartGap;

        // Bottom chrome (mute + back + timer)
        backButtonW = Math.min(90, innerW - 12);
        backButtonH = 34;
        backButtonX = cx - backButtonW / 2;
        backButtonY = boardHeight - pad - backButtonH;

        muteButtonW = backButtonW;
        muteButtonH = 28;
        muteButtonX = backButtonX;
        muteButtonY = backButtonY - muteButtonH - 8;

        int timerBaseline = muteButtonY - 12;
        drawMazeTimer(g, session, cx, timerBaseline);
        drawMuteButton(g);
        drawBackButton(g);

        // Score block — vertically centered between lives and timer
        int midTop = livesBottom + 12;
        int midBot = timerBaseline - 18;
        int midCenter = (midTop + midBot) / 2;

        g.setFont(Theme.mono(22));
        FontMetrics scoreFm = g.getFontMetrics();
        String scoreStr = String.valueOf(session.getScore());
        int scoreW = scoreFm.stringWidth(scoreStr);
        int scoreY = midCenter + (scoreFm.getAscent() - scoreFm.getDescent()) / 2 - 8;
        g.setColor(Theme.PAC_YELLOW);
        g.drawString(scoreStr, cx - scoreW / 2, scoreY);

        g.setFont(Theme.body(12));
        FontMetrics ptsFm = g.getFontMetrics();
        String pts = "נקודות";
        int pw = ptsFm.stringWidth(pts);
        g.setColor(Theme.BG_TAN);
        g.drawString(pts, cx - pw / 2, scoreY + ptsFm.getHeight());

        drawEatenBooks(g, session, x0, scoreY + ptsFm.getHeight() + 14);
    }

    private void drawMuteButton(Graphics2D g) {
        boolean muted = SoundManager.get().isMuted();
        int arc = muteButtonH;
        g.setColor(Theme.withAlpha(Theme.TITLE_BAR, 160));
        g.fillRoundRect(muteButtonX + 1, muteButtonY + 2, muteButtonW, muteButtonH, arc, arc);
        g.setColor(Theme.withAlpha(muted ? Theme.BG_RUST : Theme.BG_NAVY, 230));
        g.fillRoundRect(muteButtonX, muteButtonY, muteButtonW, muteButtonH, arc, arc);
        g.setColor(muted ? Theme.BG_ORANGE : Theme.BG_TAN);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(muteButtonX, muteButtonY, muteButtonW - 1, muteButtonH - 1, arc, arc);
        g.setColor(Theme.CREAM);
        g.setFont(Theme.bodyBold(12));
        FontMetrics fm = g.getFontMetrics();
        String label = muted ? "השתק" : "קול";
        int tx = muteButtonX + (muteButtonW - fm.stringWidth(label)) / 2;
        int ty = muteButtonY + (muteButtonH + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(label, tx, ty);
    }

    private void drawBackButton(Graphics2D g) {
        int arc = backButtonH;
        g.setColor(Theme.withAlpha(Theme.TITLE_BAR, 160));
        g.fillRoundRect(backButtonX + 1, backButtonY + 2, backButtonW, backButtonH, arc, arc);
        g.setColor(Theme.withAlpha(Theme.BG_STEEL, 220));
        g.fillRoundRect(backButtonX, backButtonY, backButtonW, backButtonH, arc, arc);
        g.setColor(Theme.BG_GOLD);
        g.setStroke(new BasicStroke(1.6f));
        g.drawRoundRect(backButtonX, backButtonY, backButtonW - 1, backButtonH - 1, arc, arc);
        g.setColor(Theme.CREAM);
        g.setFont(Theme.bodyBold(14));
        FontMetrics fm = g.getFontMetrics();
        String back = "חזרה";
        int tx = backButtonX + (backButtonW - fm.stringWidth(back)) / 2;
        int ty = backButtonY + (backButtonH + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(back, tx, ty);
    }

    private void drawEatenBooks(Graphics2D g, GameSession session, int sideLeft, int startY) {
        if (session.getEatenBooks().isEmpty()) {
            return;
        }
        int icon = 22;
        int gap = 4;
        int cols = 3;
        int contentLeft = sideLeft + 3;
        int contentW = GameSession.SIDE_W - 3;
        int cx = contentLeft + contentW / 2;
        for (int i = 0; i < session.getEatenBooks().size(); i++) {
            Image img = session.getEatenBooks().get(i);
            if (img == null) {
                continue;
            }
            int col = i % cols;
            int row = i / cols;
            int totalW = cols * icon + (cols - 1) * gap;
            int gridLeft = cx - totalW / 2;
            int x = gridLeft + col * (icon + gap);
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
