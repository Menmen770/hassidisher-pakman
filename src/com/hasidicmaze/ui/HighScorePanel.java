package com.hasidicmaze.ui;

import com.hasidicmaze.Theme;
import com.hasidicmaze.score.HighScore;
import com.hasidicmaze.score.HighScoreManager;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Top-10 leaderboard — centered board, rank on the right, no map column.
 */
public class HighScorePanel extends AtmospherePanel {
    private static final int BOARD_WIDTH = 560;

    private final HighScoreManager manager;
    private final JPanel listPanel = new JPanel(new GridLayout(10, 1, 0, 5));

    public HighScorePanel(HighScoreManager manager, Consumer<String> navigate) {
        this.manager = manager;

        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT));
        setBorder(new EmptyBorder(18, 40, 14, 40));

        JPanel top = new JPanel(new BorderLayout(0, 2));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 12, 0));
        JLabel title = new JLabel("שיאים גבוהים", SwingConstants.CENTER);
        title.setFont(Theme.display(34));
        title.setForeground(Theme.GOLD_BRIGHT);
        JLabel subtitle = new JLabel("עשרת הגדולים", SwingConstants.CENTER);
        subtitle.setFont(Theme.body(14));
        subtitle.setForeground(Theme.MUTED);
        top.add(title, BorderLayout.NORTH);
        top.add(subtitle, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        BoardPanel board = new BoardPanel();
        board.setLayout(new BorderLayout(0, 8));
        board.setPreferredSize(new Dimension(BOARD_WIDTH, 480));
        board.setMinimumSize(new Dimension(BOARD_WIDTH, 400));
        board.setMaximumSize(new Dimension(BOARD_WIDTH, 520));
        board.setBorder(new EmptyBorder(14, 18, 14, 18));
        board.add(buildColumnHeader(), BorderLayout.NORTH);

        listPanel.setOpaque(false);
        board.add(listPanel, BorderLayout.CENTER);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        center.add(board, gbc);
        add(center, BorderLayout.CENTER);

        refresh();

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        StyledButton back = new StyledButton("חזרה לתפריט");
        back.setPreferredSize(new Dimension(200, 46));
        back.addActionListener(e -> navigate.accept("MENU"));
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildColumnHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(BOARD_WIDTH - 36, 24));

        // Visual RTL: score left, name center, rank right
        JLabel score = muted("ניקוד", 90, SwingConstants.CENTER);
        JLabel name = new JLabel("שם", SwingConstants.CENTER);
        name.setFont(Theme.bodyBold(12));
        name.setForeground(Theme.MUTED);
        JLabel rank = muted("#", 48, SwingConstants.CENTER);

        header.add(score, BorderLayout.WEST);
        header.add(name, BorderLayout.CENTER);
        header.add(rank, BorderLayout.EAST);
        return header;
    }

    private static JLabel muted(String text, int width, int align) {
        JLabel label = new JLabel(text, align);
        label.setFont(Theme.bodyBold(12));
        label.setForeground(Theme.MUTED);
        label.setPreferredSize(new Dimension(width, 22));
        return label;
    }

    public void refresh() {
        listPanel.removeAll();
        listPanel.setLayout(new GridLayout(10, 1, 0, 5));
        List<HighScore> scores = manager.topScores();

        int n = Math.min(10, scores.size());
        for (int i = 0; i < n; i++) {
            listPanel.add(new ScoreRow(i + 1, scores.get(i)));
        }
        for (int i = n; i < 10; i++) {
            listPanel.add(new EmptySlot(i + 1));
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private static final class ScoreRow extends JPanel {
        ScoreRow(int rank, HighScore hs) {
            setOpaque(false);
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(0, 6, 0, 6));

            JLabel rankLabel = new JLabel(String.format("%02d", rank), SwingConstants.CENTER);
            rankLabel.setFont(Theme.mono(18));
            rankLabel.setForeground(Theme.GOLD);
            rankLabel.setPreferredSize(new Dimension(48, 40));

            JLabel name = new JLabel(hs.getName(), SwingConstants.CENTER);
            name.setFont(Theme.bodyBold(17));
            name.setForeground(Theme.CREAM);

            JLabel score = new JLabel(String.valueOf(hs.getScore()), SwingConstants.CENTER);
            score.setFont(Theme.mono(20));
            score.setForeground(Theme.GOLD_BRIGHT);
            score.setPreferredSize(new Dimension(90, 40));

            // Rank on the right (user's right), score on the left, name in the middle
            add(score, BorderLayout.WEST);
            add(name, BorderLayout.CENTER);
            add(rankLabel, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 32, 48, 185));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class EmptySlot extends JPanel {
        EmptySlot(int rank) {
            setOpaque(false);
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(0, 6, 0, 6));

            JLabel rankLabel = new JLabel(String.format("%02d", rank), SwingConstants.CENTER);
            rankLabel.setFont(Theme.mono(16));
            rankLabel.setForeground(new Color(100, 108, 124));
            rankLabel.setPreferredSize(new Dimension(48, 40));

            JLabel dash = new JLabel("—", SwingConstants.CENTER);
            dash.setFont(Theme.body(15));
            dash.setForeground(new Color(100, 108, 124));

            add(dash, BorderLayout.CENTER);
            add(rankLabel, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(24, 32, 48, 95));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class BoardPanel extends JPanel {
        BoardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(18, 24, 38, 210));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(new Color(212, 168, 75, 120));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
