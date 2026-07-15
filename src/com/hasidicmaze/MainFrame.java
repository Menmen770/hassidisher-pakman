package com.hasidicmaze;

import com.hasidicmaze.game.GamePanel;
import com.hasidicmaze.map.GameMap;
import com.hasidicmaze.score.HighScoreManager;
import com.hasidicmaze.ui.HighScorePanel;
import com.hasidicmaze.ui.MapSelectPanel;
import com.hasidicmaze.ui.MenuPanel;
import com.hasidicmaze.ui.NewRecordPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Root window — one fixed size for every screen (menu, maps, scores, game).
 * Game content letterboxes/scales inside; map/bg alignment stays unchanged.
 */
public class MainFrame extends JFrame {
    public static final String MENU = "MENU";
    public static final String MAP_SELECT = "MAP_SELECT";
    public static final String HIGH_SCORES = "HIGH_SCORES";
    public static final String NEW_RECORD = "NEW_RECORD";
    public static final String GAME = "GAME";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final Dimension windowSize = Theme.windowSize();
    private final HighScoreManager scoreManager = new HighScoreManager();
    private final HighScorePanel highScorePanel;
    private final NewRecordPanel newRecordPanel;
    private final GamePanel gamePanel;
    private final JPanel gameHost = new JPanel(new BorderLayout());

    public MainFrame() {
        super("Hasidic Maze");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Same frame for all cards — user can maximize for true fullscreen-ish play
        setResizable(true);

        highScorePanel = new HighScorePanel(scoreManager, this::showScreen);
        newRecordPanel = new NewRecordPanel(this::saveNewRecord, ignored -> {
            highScorePanel.refresh();
            showScreen(HIGH_SCORES);
        });

        gamePanel = new GamePanel(new GamePanel.Listener() {
            @Override
            public void onQuitToMenu() {
                gamePanel.stop();
                setTitle("Hasidic Maze");
                showScreen(MENU);
            }

            @Override
            public void onGameOver(int score, String mapId) {
                gamePanel.stop();
                setTitle("Hasidic Maze");
                if (scoreManager.isTopScore(score)) {
                    int rank = scoreManager.rankFor(score);
                    newRecordPanel.prepare(score, rank, mapId);
                    showScreen(NEW_RECORD);
                } else {
                    highScorePanel.refresh();
                    showScreen(HIGH_SCORES);
                }
            }
        });

        gameHost.setBackground(Theme.INK);
        gameHost.setOpaque(true);
        gameHost.add(gamePanel, BorderLayout.CENTER);

        MenuPanel menuPanel = new MenuPanel(this::showScreen);
        MapSelectPanel mapSelectPanel = new MapSelectPanel(this::startGame, this::showScreen);

        for (JPanel panel : new JPanel[]{
            menuPanel, mapSelectPanel, highScorePanel, newRecordPanel, gameHost
        }) {
            panel.setPreferredSize(windowSize);
            panel.setMinimumSize(new Dimension(640, 480));
        }

        root.add(menuPanel, MENU);
        root.add(mapSelectPanel, MAP_SELECT);
        root.add(highScorePanel, HIGH_SCORES);
        root.add(newRecordPanel, NEW_RECORD);
        root.add(gameHost, GAME);
        root.setPreferredSize(windowSize);

        setContentPane(root);
        setSize(windowSize);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null);
        showScreen(MENU);
    }

    private void showScreen(String name) {
        cards.show(root, name);
        root.revalidate();
        root.repaint();
        if (HIGH_SCORES.equals(name)) {
            highScorePanel.refresh();
        }
    }

    private void startGame(GameMap map) {
        if (map == null || map.isLocked()) {
            return;
        }
        showScreen(GAME);
        gamePanel.startSession(map);
        setTitle("Hasidic Maze — " + map.getTitle());
        // Do not pack/resize — game scales inside the same window
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
