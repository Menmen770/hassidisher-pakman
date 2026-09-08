package com.hasidicmaze.ui;

import com.hasidicmaze.Screen;
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
import java.awt.GridLayout;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/** Top-10 leaderboard — same capsule / gold-navy language as main menu. */
public class HighScorePanel extends AtmospherePanel {
    private static final int BOARD_WIDTH = 560;
    private static final int BOARD_HEIGHT = 470;

    private final HighScoreManager manager;
    private final JPanel listPanel = new JPanel(new GridLayout(10, 1, 0, 6));

    public HighScorePanel(HighScoreManager manager, Consumer<Screen> navigate) {
        this.manager = manager;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT));
        setBorder(new EmptyBorder(36, 40, 32, 40));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setAlignmentX(CENTER_ALIGNMENT);
        JLabel title = new JLabel("שיאים", SwingConstants.CENTER);
        title.setFont(Theme.display(28));
        title.setForeground(Theme.BG_GOLD);
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("עשרת הגדולים", SwingConstants.CENTER);
        subtitle.setFont(Theme.body(13));
        subtitle.setForeground(Theme.BG_STEEL);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        head.add(title);
        head.add(Box.createVerticalStrut(4));
        head.add(subtitle);

        BoardPanel board = new BoardPanel();
        board.setLayout(new BorderLayout(0, 8));
        board.setAlignmentX(CENTER_ALIGNMENT);
        Dimension boardSize = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
        board.setPreferredSize(boardSize);
        board.setMinimumSize(boardSize);
        board.setMaximumSize(boardSize);
        board.setBorder(new EmptyBorder(16, 18, 16, 18));
        board.add(buildColumnHeader(), BorderLayout.NORTH);

        listPanel.setOpaque(false);
        board.add(listPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        bottom.setOpaque(false);
        bottom.setAlignmentX(CENTER_ALIGNMENT);
        bottom.setMaximumSize(new Dimension(Short.MAX_VALUE, 56));
        StyledButton back = new StyledButton("חזרה", StyledButton.Variant.GHOST);
        Dimension backSize = new Dimension(132, 44);
        back.setPreferredSize(backSize);
        back.setMinimumSize(backSize);
        back.setMaximumSize(backSize);
        back.addActionListener(e -> navigate.accept(Screen.MENU));
        bottom.add(back);

        stack.add(Box.createVerticalGlue());
        stack.add(head);
        stack.add(Box.createVerticalStrut(16));
        stack.add(board);
        stack.add(Box.createVerticalStrut(18));
        stack.add(bottom);
        stack.add(Box.createVerticalGlue());

        add(stack, BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildColumnHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(BOARD_WIDTH - 36, 24));
        JLabel score = muted("ניקוד", 90, SwingConstants.CENTER);
        JLabel name = new JLabel("שם", SwingConstants.CENTER);
        name.setFont(Theme.bodyBold(12));
        name.setForeground(Theme.BG_STEEL);
        JLabel rank = muted("#", 48, SwingConstants.CENTER);
        header.add(score, BorderLayout.WEST);
        header.add(name, BorderLayout.CENTER);
        header.add(rank, BorderLayout.EAST);
        return header;
    }

    private static JLabel muted(String text, int width, int align) {
        JLabel label = new JLabel(text, align);
        label.setFont(Theme.bodyBold(12));
        label.setForeground(Theme.BG_STEEL);
        label.setPreferredSize(new Dimension(width, 22));
        return label;
    }

    public void refresh() {
        listPanel.removeAll();
        listPanel.setLayout(new GridLayout(10, 1, 0, 6));
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
            setBorder(new EmptyBorder(0, 10, 0, 10));

            JLabel rankLabel = new JLabel(String.format("%02d", rank), SwingConstants.CENTER);
            rankLabel.setFont(Theme.mono(18));
            rankLabel.setForeground(Theme.BG_GOLD);
            rankLabel.setPreferredSize(new Dimension(48, 40));

            JLabel name = new JLabel(hs.getName(), SwingConstants.CENTER);
            name.setFont(Theme.bodyBold(17));
            name.setForeground(Theme.CREAM);

            JLabel score = new JLabel(String.valueOf(hs.getScore()), SwingConstants.CENTER);
            score.setFont(Theme.mono(20));
            score.setForeground(Theme.BG_TAN);
            score.setPreferredSize(new Dimension(90, 40));

            add(score, BorderLayout.WEST);
            add(name, BorderLayout.CENTER);
            add(rankLabel, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            int arc = Math.min(22, h);
            g2.setColor(Theme.withAlpha(Theme.BG_NAVY, 200));
            g2.fillRoundRect(0, 0, getWidth() - 1, h - 1, arc, arc);
            g2.setColor(Theme.withAlpha(Theme.BG_TAN, 90));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 2, h - 2, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class EmptySlot extends JPanel {
        EmptySlot(int rank) {
            setOpaque(false);
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(0, 10, 0, 10));
            JLabel rankLabel = new JLabel(String.format("%02d", rank), SwingConstants.CENTER);
            rankLabel.setFont(Theme.mono(16));
            rankLabel.setForeground(Theme.MUTED_DARK);
            rankLabel.setPreferredSize(new Dimension(48, 40));
            JLabel dash = new JLabel("—", SwingConstants.CENTER);
            dash.setFont(Theme.body(15));
            dash.setForeground(Theme.MUTED_DARK);
            add(dash, BorderLayout.CENTER);
            add(rankLabel, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            int arc = Math.min(22, h);
            g2.setColor(Theme.withAlpha(Theme.BG_NAVY, 110));
            g2.fillRoundRect(0, 0, getWidth() - 1, h - 1, arc, arc);
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
            int w = getWidth();
            int h = getHeight();
            int arc = 28;
            g2.setColor(Theme.withAlpha(Theme.BG_BARK, 90));
            g2.fillRoundRect(4, 5, w - 8, h - 6, arc, arc);
            g2.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 1f},
                new Color[]{Theme.INK_SOFT, Theme.withAlpha(Theme.BG_BARK, 210)}));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(Theme.BG_GOLD);
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            g2.setColor(Theme.withAlpha(Theme.BG_TAN, 100));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(6, 6, w - 13, h - 13, arc - 8, arc - 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
