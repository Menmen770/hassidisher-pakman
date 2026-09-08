package com.hasidicmaze.game;

import com.hasidicmaze.Theme;
import com.hasidicmaze.assets.AssetManager;
import com.hasidicmaze.map.GameMap;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Game view: timer loop, input, letterbox scale, and paint.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {
    public interface Listener {
        void onQuitToMenu();
        void onGameOver(int score, String mapId);
        void onCampaignStageCleared(String completedMapId);
    }

    private final AssetManager assets = AssetManager.get();
    private final Listener listener;
    private final Maze maze = new Maze(assets);
    private final GameSession session = new GameSession(assets, maze);
    private final GameHud hud = new GameHud(assets);
    private final Timer gameLoop = new Timer(50, this);

    private Image sessionBackground;
    private boolean showTileGrid = false;
    private boolean showWallTiles = false;

    public GamePanel(Listener listener) {
        this.listener = listener;
        setBackground(Theme.INK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (hitMuteButton(e.getX(), e.getY())) {
                    com.hasidicmaze.sound.SoundManager.get().toggleMute();
                    requestFocusInWindow();
                    repaint();
                    return;
                }
                if (hitBackButton(e.getX(), e.getY())) {
                    stop();
                    listener.onQuitToMenu();
                }
            }
        });
    }

    private int canvasWidth() {
        return maze.getGameAreaWidth() + GameSession.SIDE_W;
    }

    private int canvasHeight() {
        return maze.getBoardHeight();
    }

    private int canvasOffsetX(int panelW, int drawW) {
        return (panelW - drawW) / 2;
    }

    private boolean hitBackButton(int panelX, int panelY) {
        double[] g = toGameCoords(panelX, panelY);
        return g[0] >= hud.backButtonX && g[0] <= hud.backButtonX + hud.backButtonW
            && g[1] >= hud.backButtonY && g[1] <= hud.backButtonY + hud.backButtonH;
    }

    private boolean hitMuteButton(int panelX, int panelY) {
        double[] g = toGameCoords(panelX, panelY);
        return g[0] >= hud.muteButtonX && g[0] <= hud.muteButtonX + hud.muteButtonW
            && g[1] >= hud.muteButtonY && g[1] <= hud.muteButtonY + hud.muteButtonH;
    }

    private double[] toGameCoords(int panelX, int panelY) {
        int panelW = Math.max(getWidth(), 1);
        int panelH = Math.max(getHeight(), 1);
        double scale = Math.min(panelW / (double) canvasWidth(), panelH / (double) canvasHeight());
        int drawW = (int) Math.round(canvasWidth() * scale);
        int drawH = (int) Math.round(canvasHeight() * scale);
        int ox = canvasOffsetX(panelW, drawW);
        int oy = (panelH - drawH) / 2;
        return new double[]{(panelX - ox) / scale, (panelY - oy) / scale};
    }

    public void startSession(GameMap map, boolean campaign) {
        sessionBackground = assets.backgroundFor(map.getBackgroundFile());
        if (sessionBackground == null) {
            sessionBackground = assets.background;
        }
        fitCanvasToBackground();
        session.start(map, campaign);
        setOpaque(true);
        if (!gameLoop.isRunning()) {
            gameLoop.start();
        }
        revalidate();
        repaint();
    }

    public void advanceCampaign(GameMap next) {
        sessionBackground = assets.backgroundFor(next.getBackgroundFile());
        if (sessionBackground == null) {
            sessionBackground = assets.background;
        }
        fitCanvasToBackground();
        session.continueCampaign(next);
        if (!gameLoop.isRunning()) {
            gameLoop.start();
        }
        requestFocusInWindow();
        revalidate();
        repaint();
    }

    private void fitCanvasToBackground() {
        Image bg = sessionBackground != null ? sessionBackground : assets.background;
        if (bg == null) {
            maze.configureCanvas(640, 720);
            return;
        }
        int bw = bg.getWidth(null);
        int bh = bg.getHeight(null);
        if (bw <= 0 || bh <= 0) {
            maze.configureCanvas(640, 720);
            return;
        }
        int gameAreaWidth = Math.max(1, (int) Math.round(bw * 0.42));
        int boardHeight = Math.max(1, (int) Math.round(bh * 0.42));
        maze.configureCanvas(gameAreaWidth, boardHeight);
    }

    public void stop() {
        gameLoop.stop();
        com.hasidicmaze.sound.SoundManager.get().stopAll();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (session.getGameMap() == null || maze.getHero() == null) {
            g.setColor(Theme.INK);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int panelW = Math.max(getWidth(), 1);
        int panelH = Math.max(getHeight(), 1);
        double scale = Math.min(panelW / (double) canvasWidth(), panelH / (double) canvasHeight());
        int drawW = (int) Math.round(canvasWidth() * scale);
        int drawH = (int) Math.round(canvasHeight() * scale);
        int ox = canvasOffsetX(panelW, drawW);
        int oy = (panelH - drawH) / 2;

        g2.setColor(Theme.INK);
        g2.fillRect(0, 0, panelW, panelH);

        // Extend sidebar bark through top/bottom letterbox so no navy "hole" above hearts
        int sideScreenX = ox + (int) Math.round(maze.getGameAreaWidth() * scale);
        g2.setColor(Theme.TITLE_BAR);
        g2.fillRect(sideScreenX, 0, Math.max(0, panelW - sideScreenX), panelH);

        g2.translate(ox, oy);
        g2.scale(scale, scale);

        hud.paintWorld(g2, session, sessionBackground, showWallTiles, showTileGrid);
        g2.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        session.tick();
        if (session.consumeStageCleared()) {
            listener.onCampaignStageCleared(session.getGameMap().getId());
        }
        if (session.isGameOver()) {
            gameLoop.stop();
        }
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (session.isGameOver()) {
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
        if (e.getKeyCode() == KeyEvent.VK_M) {
            com.hasidicmaze.sound.SoundManager.get().toggleMute();
            repaint();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_P) {
            session.toggleUserPause();
            repaint();
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
            case KeyEvent.VK_UP -> session.setNextDirection('U');
            case KeyEvent.VK_DOWN -> session.setNextDirection('D');
            case KeyEvent.VK_LEFT -> session.setNextDirection('L');
            case KeyEvent.VK_RIGHT -> session.setNextDirection('R');
            default -> {}
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    private void nudgeLayout(int dTile, int dOx, int dOy) {
        if (!maze.nudgeLayout(dTile, dOx, dOy)) {
            return;
        }
        session.reloadAfterLayoutNudge();
        requestFocusInWindow();
        repaint();
    }

    private void finishGame() {
        if (session.isAwaitingName()) {
            return;
        }
        session.markAwaitingName();
        listener.onGameOver(session.getScore(), session.getGameMap().getId());
    }
}
