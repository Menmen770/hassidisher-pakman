package com.hasidicmaze.ui;

import com.hasidicmaze.Screen;
import com.hasidicmaze.Theme;
import com.hasidicmaze.assets.AssetManager;
import com.hasidicmaze.map.GameMap;
import com.hasidicmaze.map.MapCatalog;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/** Stage select — RTL vertical list, gold/navy language. */
public class MapSelectPanel extends AtmospherePanel {
    private static final int ROW_W = 480;
    private static final int ROW_H = 76;
    private static final int GAP = 14;
    private static final int PRIZE = 44;

    private final Consumer<GameMap> onPlay;
    private GameMap selected = MapCatalog.all().get(0);
    private final StyledButton playButton;
    private final List<StageCard> cards = new ArrayList<>();

    public MapSelectPanel(Consumer<GameMap> onPlay, Consumer<Screen> navigate) {
        this.onPlay = onPlay;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT));
        setBorder(new EmptyBorder(36, 40, 32, 40));
        applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setAlignmentX(CENTER_ALIGNMENT);
        JLabel title = new JLabel("בחירת שלב", SwingConstants.CENTER);
        title.setFont(Theme.display(28));
        title.setForeground(Theme.BG_GOLD);
        title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = new JLabel("בחר סדר מהרשימה", SwingConstants.CENTER);
        sub.setFont(Theme.body(13));
        sub.setForeground(Theme.BG_STEEL);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        head.add(title);
        head.add(Box.createVerticalStrut(4));
        head.add(sub);

        Board board = new Board();
        board.setLayout(new BoxLayout(board, BoxLayout.Y_AXIS));
        board.setBorder(new EmptyBorder(16, 20, 16, 20));
        board.setAlignmentX(CENTER_ALIGNMENT);
        int listH = ROW_H * 4 + GAP * 3;
        Dimension boardSize = new Dimension(ROW_W + 40, listH + 32);
        board.setPreferredSize(boardSize);
        board.setMaximumSize(boardSize);
        board.setMinimumSize(boardSize);

        int n = 0;
        for (GameMap map : MapCatalog.all()) {
            if (n > 0) {
                board.add(Box.createVerticalStrut(GAP));
            }
            StageCard card = new StageCard(map, n);
            cards.add(card);
            board.add(card);
            n++;
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        bottom.setOpaque(false);
        bottom.setAlignmentX(CENTER_ALIGNMENT);
        bottom.setMaximumSize(new Dimension(Short.MAX_VALUE, 56));
        bottom.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        playButton = new StyledButton("שחק", StyledButton.Variant.PRIMARY);
        size(playButton, 168, 50);
        playButton.addActionListener(e -> {
            if (selected != null && !selected.isLocked()) {
                onPlay.accept(selected);
            }
        });

        StyledButton back = new StyledButton("חזרה", StyledButton.Variant.GHOST);
        size(back, 132, 44);
        back.addActionListener(e -> navigate.accept(Screen.MENU));

        bottom.add(playButton);
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

    private static void size(StyledButton b, int w, int h) {
        Dimension d = new Dimension(w, h);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);
    }

    private void select(GameMap map) {
        if (map == null || map.isLocked()) {
            return;
        }
        selected = map;
        refresh();
    }

    private void refresh() {
        playButton.setEnabled(selected != null && !selected.isLocked());
        for (StageCard c : cards) {
            c.repaint();
        }
    }

    private static final class Board extends JPanel {
        Board() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = 24;
            g2.setColor(Theme.withAlpha(Theme.BG_BARK, 80));
            g2.fillRoundRect(4, 5, w - 8, h - 6, arc, arc);
            g2.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 1f},
                new Color[]{Theme.INK_SOFT, Theme.withAlpha(Theme.BG_BARK, 200)}));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setColor(Theme.withAlpha(Theme.BG_GOLD, 200));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class StageCard extends JPanel {
        private final GameMap map;
        private final int index;
        private boolean hovered;

        StageCard(GameMap map, int index) {
            this.map = map;
            this.index = index;
            setOpaque(false);
            Dimension d = new Dimension(ROW_W, ROW_H);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
            setAlignmentX(CENTER_ALIGNMENT);
            setCursor(map.isLocked()
                ? Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
                : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!map.isLocked()) {
                        hovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (map.isLocked()) {
                        return;
                    }
                    select(map);
                    if (e.getClickCount() == 2) {
                        onPlay.accept(map);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();
            boolean on = !map.isLocked() && selected.getId().equals(map.getId());
            int arc = h;

            g2.setColor(Theme.withAlpha(Theme.BG_BARK, 100));
            g2.fillRoundRect(2, 3, w - 4, h - 3, arc, arc);

            if (on) {
                g2.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 0.5f, 1f},
                    new Color[]{Theme.BG_GOLD, Theme.BG_ORANGE, Theme.BG_RUST}));
            } else if (hovered) {
                g2.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 1f},
                    new Color[]{Theme.INK_SOFT, Theme.withAlpha(Theme.BG_BARK, 200)}));
            } else {
                g2.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 1f},
                    new Color[]{Theme.withAlpha(Theme.BG_NAVY, 230), Theme.withAlpha(Theme.BG_BARK, 170)}));
            }
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.setColor(on ? Theme.BG_BARK : (hovered ? Theme.BG_GOLD : Theme.BG_TAN));
            g2.setStroke(new BasicStroke(on ? 2.2f : 1.6f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // RTL: number chip on the RIGHT
            String num = String.format("%02d", index + 1);
            g2.setFont(Theme.mono(16));
            FontMetrics nm = g2.getFontMetrics();
            int chip = 40;
            int chipX = w - chip - 14;
            int chipY = (h - chip) / 2;
            g2.setColor(on ? Theme.withAlpha(Theme.BG_BARK, 50) : Theme.withAlpha(Theme.BG_GOLD, 45));
            g2.fillRoundRect(chipX, chipY, chip, chip, 14, 14);
            g2.setColor(on ? Theme.BG_BARK : Theme.BG_GOLD);
            g2.drawString(num, chipX + (chip - nm.stringWidth(num)) / 2,
                chipY + (chip + nm.getAscent() - nm.getDescent()) / 2);

            // Title — right-aligned, next to the chip
            String name = map.getTitle();
            g2.setFont(Theme.bodyBold(19));
            FontMetrics fm = g2.getFontMetrics();
            int maxText = w - chip - PRIZE - 70;
            while (fm.stringWidth(name) > maxText && g2.getFont().getSize() > 14) {
                g2.setFont(Theme.bodyBold(g2.getFont().getSize() - 1));
                fm = g2.getFontMetrics();
            }
            int titleX = chipX - 16 - fm.stringWidth(name);
            g2.setColor(map.isLocked() ? Theme.MUTED_DARK : (on ? Theme.BG_BARK : Theme.CREAM));
            g2.drawString(name, titleX, (h + fm.getAscent() - fm.getDescent()) / 2);

            // Prize icon on the LEFT
            Image prize = AssetManager.get().bonusForStage(index);
            if (prize != null) {
                int ix = 18;
                int iy = (h - PRIZE) / 2;
                g2.setColor(Theme.withAlpha(on ? Theme.BG_BARK : Theme.BG_NAVY, on ? 40 : 150));
                g2.fillRoundRect(ix - 5, iy - 5, PRIZE + 10, PRIZE + 10, 14, 14);
                g2.drawImage(prize, ix, iy, PRIZE, PRIZE, null);
            }

            g2.dispose();
        }
    }
}
