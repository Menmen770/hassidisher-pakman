package com.hasidicmaze;

import com.hasidicmaze.game.GamePanel;
import com.hasidicmaze.map.GameMap;
import com.hasidicmaze.score.HighScoreManager;
import com.hasidicmaze.ui.HighScorePanel;
import com.hasidicmaze.ui.MapSelectPanel;
import com.hasidicmaze.ui.MenuPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Root window — CardLayout navigation between menu, maps, scores, and game.
 */
public class MainFrame extends JFrame {
    public static final String MENU = "MENU";
    public static final String MAP_SELECT = "MAP_SELECT";
    public static final String HIGH_SCORES = "HIGH_SCORES";
    public static final String GAME = "GAME";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final HighScoreManager scoreManager = new HighScoreManager();
    private final HighScorePanel highScorePanel;
    private final GamePanel gamePanel;
    private final JPanel gameHost = new JPanel(new BorderLayout());

    public MainFrame() {
        super("Hasidic Maze");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        highScorePanel = new HighScorePanel(scoreManager, this::showScreen);
        gamePanel = new GamePanel(new GamePanel.Listener() {
            @Override
            public void onQuitToMenu() {
                showMenuSized();
                showScreen(MENU);
            }

            @Override
            public void onGameOver(int score, String mapId) {
                maybeSaveScore(score, mapId);
                showMenuSized();
                highScorePanel.refresh();
                showScreen(HIGH_SCORES);
            }
        });

        gameHost.setBackground(Theme.INK);
        gameHost.setOpaque(true);
        gameHost.add(gamePanel, BorderLayout.CENTER);

        MenuPanel menuPanel = new MenuPanel(this::showScreen);
        MapSelectPanel mapSelectPanel = new MapSelectPanel(this::startGame, this::showScreen);

        // Fixed size for menu cards so CardLayout doesn't collapse
        Dimension menuSize = new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT);
        menuPanel.setPreferredSize(menuSize);
        mapSelectPanel.setPreferredSize(menuSize);
        highScorePanel.setPreferredSize(menuSize);
        gameHost.setPreferredSize(menuSize);

        root.add(menuPanel, MENU);
        root.add(mapSelectPanel, MAP_SELECT);
        root.add(highScorePanel, HIGH_SCORES);
        root.add(gameHost, GAME);

        setContentPane(root);
        setSize(menuSize);
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
        // Show the game card first so layout has a real size, then start the session
        showScreen(GAME);

        gamePanel.startSession(map);
        Dimension gameSize = gamePanel.getPreferredSize();

        gameHost.setPreferredSize(gameSize);
        gamePanel.setSize(gameSize);
        root.setPreferredSize(gameSize);

        // Resize frame to the board (include window insets)
        revalidate();
        pack();
        setLocationRelativeTo(null);

        // Focus after layout settles
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
            gamePanel.repaint();
        });
    }

    private void showMenuSized() {
        gamePanel.stop();
        Dimension menuSize = new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT);
        gameHost.setPreferredSize(menuSize);
        root.setPreferredSize(menuSize);
        setSize(menuSize);
        setLocationRelativeTo(null);
    }

    private void maybeSaveScore(int score, String mapId) {
        if (score <= 0) {
            return;
        }
        String name = JOptionPane.showInputDialog(
            this,
            "הניקוד שלך: " + score + "\nהכנס שם ללוח השיאים:",
            "שיא חדש",
            JOptionPane.PLAIN_MESSAGE
        );
        if (name != null && !name.trim().isEmpty()) {
            scoreManager.add(name, score, mapId);
        } else if (scoreManager.isTopScore(score)) {
            scoreManager.add("Player", score, mapId);
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
