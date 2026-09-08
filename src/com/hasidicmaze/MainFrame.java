package com.hasidicmaze;

import com.hasidicmaze.assets.AssetManager;
import com.hasidicmaze.game.GamePanel;
import com.hasidicmaze.map.GameMap;
import com.hasidicmaze.map.MapCatalog;
import com.hasidicmaze.score.HighScoreManager;
import com.hasidicmaze.ui.HighScorePanel;
import com.hasidicmaze.ui.MapSelectPanel;
import com.hasidicmaze.ui.MenuPanel;
import com.hasidicmaze.ui.NewRecordPanel;
import com.hasidicmaze.ui.TitleBar;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Taskbar;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Root window — one fixed size for every screen (menu, maps, scores, game).
 * Game content letterboxes/scales inside; map/bg alignment stays unchanged.
 */
public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final Dimension windowSize = Theme.windowSize();
    private final HighScoreManager scoreManager = new HighScoreManager();
    private final HighScorePanel highScorePanel;
    private final NewRecordPanel newRecordPanel;
    private final GamePanel gamePanel;
    private final JPanel gameHost = new JPanel(new BorderLayout());

    public MainFrame() {
        super("Hassidisher Pakman");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(true);
        applyAppIcon();

        highScorePanel = new HighScorePanel(scoreManager, this::showScreen);
        newRecordPanel = new NewRecordPanel(this::saveNewRecord, ignored -> {
            highScorePanel.refresh();
            showScreen(Screen.HIGH_SCORES);
        });

        gamePanel = new GamePanel(new GamePanel.Listener() {
            @Override
            public void onQuitToMenu() {
                gamePanel.stop();
                setTitle("Hassidisher Pakman");
                showScreen(Screen.MENU);
            }

            @Override
            public void onGameOver(int score, String mapId) {
                gamePanel.stop();
                setTitle("Hassidisher Pakman");
                if (scoreManager.isTopScore(score)) {
                    int rank = scoreManager.rankFor(score);
                    newRecordPanel.prepare(score, rank, mapId);
                    showScreen(Screen.NEW_RECORD);
                } else {
                    highScorePanel.refresh();
                    showScreen(Screen.HIGH_SCORES);
                }
            }

            @Override
            public void onCampaignStageCleared(String completedMapId) {
                GameMap next = MapCatalog.nextAfter(completedMapId);
                gamePanel.advanceCampaign(next);
            }
        });

        gameHost.setBackground(Theme.INK);
        gameHost.setOpaque(true);
        gameHost.add(gamePanel, BorderLayout.CENTER);

        MenuPanel menuPanel = new MenuPanel(this::showScreen, () -> startGame(MapCatalog.all().get(0), true));
        MapSelectPanel mapSelectPanel = new MapSelectPanel(map -> startGame(map, false), this::showScreen);

        for (JPanel panel : new JPanel[]{
            menuPanel, mapSelectPanel, highScorePanel, newRecordPanel, gameHost
        }) {
            panel.setPreferredSize(windowSize);
            panel.setMinimumSize(new Dimension(640, 480));
        }

        root.add(menuPanel, Screen.MENU.name());
        root.add(mapSelectPanel, Screen.MAP_SELECT.name());
        root.add(highScorePanel, Screen.HIGH_SCORES.name());
        root.add(newRecordPanel, Screen.NEW_RECORD.name());
        root.add(gameHost, Screen.GAME.name());
        root.setPreferredSize(windowSize);
        root.setBackground(Theme.INK);

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(Theme.TITLE_BAR);
        shell.add(new TitleBar(this), BorderLayout.NORTH);
        shell.add(root, BorderLayout.CENTER);

        setContentPane(shell);
        setSize(windowSize.width, windowSize.height + TitleBar.HEIGHT);
        setMinimumSize(new Dimension(640, 480 + TitleBar.HEIGHT));
        setLocationRelativeTo(null);
        showScreen(Screen.MENU);
    }

    private void applyAppIcon() {
        Image icon = AssetManager.get().appIcon;
        if (icon == null) {
            return;
        }
        java.util.List<Image> icons = new java.util.ArrayList<>();
        for (int size : new int[] {16, 32, 48, 64}) {
            Image scaled = icon.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            icons.add(new javax.swing.ImageIcon(scaled).getImage());
        }
        setIconImages(icons);
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar.getTaskbar().setIconImage(icons.get(icons.size() - 1));
            }
        } catch (Exception ignored) {
            // Taskbar icon not available on this platform / JDK
        }
    }

    private void showScreen(Screen screen) {
        cards.show(root, screen.name());
        root.revalidate();
        root.repaint();
        if (screen == Screen.HIGH_SCORES) {
            highScorePanel.refresh();
        }
    }

    private void startGame(GameMap map, boolean campaign) {
        if (map == null || map.isLocked()) {
            return;
        }
        showScreen(Screen.GAME);
        gamePanel.startSession(map, campaign);
        setTitle("Hassidisher Pakman");
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
            gamePanel.repaint();
        });
    }

    private void saveNewRecord(String name, int score) {
        scoreManager.add(name, score, newRecordPanel.getPendingMapId());
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
