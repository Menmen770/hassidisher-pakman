package com.hasidicmaze.ui;

import com.hasidicmaze.Screen;
import com.hasidicmaze.Theme;
import com.hasidicmaze.assets.AssetManager;
import com.hasidicmaze.map.GameMap;
import com.hasidicmaze.map.MapCatalog;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
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

/** Stage select — clean vertical list matching the main-menu language. */
public class MapSelectPanel extends AtmospherePanel {
    private static final int ROW_W = 420;
    private static final int ROW_H = 64;
    private static final int GAP = 12;

    private final Consumer<GameMap> onPlay;
    private GameMap selected = MapCatalog.all().get(0);
    private final StyledButton playButton;
    private final List<StageCard> cards = new ArrayList<>();

    public MapSelectPanel(Consumer<GameMap> onPlay, Consumer<Screen> navigate) {
        this.onPlay = onPlay;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT));
        setBorder(new EmptyBorder(32, 48, 22, 48));

        JPanel head = new JPanel(new BorderLayout(0, 4));
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(2, 0, 6, 0));
        JLabel title = new JLabel("בחירת שלב", SwingConstants.CENTER);
        title.setFont(Theme.display(28));
        title.setForeground(Theme.BG_GOLD);
        JLabel sub = new JLabel("בחר סדר מהרשימה", SwingConstants.CENTER);
        sub.setFont(Theme.body(13));
        sub.setForeground(Theme.BG_STEEL);
        head.add(title, BorderLayout.NORTH);
        head.add(sub, BorderLayout.SOUTH);
        add(head, BorderLayout.NORTH);

        Board board = new Board();
        board.setLayout(new BorderLayout());
        board.setBorder(new EmptyBorder(18, 22, 18, 22));
        int listH = ROW_H * 4 + GAP * 3;
        board.setPreferredSize(new Dimension(ROW_W + 44, listH + 36));

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        int n = 0;
        for (GameMap map : MapCatalog.all()) {
            if (n > 0) {
                list.add(Box.createVerticalStrut(GAP));
            }
            StageCard card = new StageCard(map, n);
            cards.add(card);
            list.add(card);
            n++;
        }
        board.add(list, BorderLayout.CENTER);

        JPanel mid = new JPanel(new GridBagLayout());
        mid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        mid.add(board, gbc);
        add(mid, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(4, 0, 2, 0));

        StyledButton back = new StyledButton("חזרה", StyledButton.Variant.GHOST);
        size(back, 132, 44);
        back.addActionListener(e -> navigate.accept(Screen.MENU));

        playButton = new StyledButton("שחק", StyledButton.Variant.PRIMARY);
        size(playButton, 168, 50);
        playButton.addActionListener(e -> {
            if (selected != null && !selected.isLocked()) {
                onPlay.accept(selected);
            }
        });

        bottom.add(back);
        bottom.add(playButton);
        add(bottom, BorderLayout.SOUTH);
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

            // Number
            String num = String.format("%02d", index + 1);
            g2.setFont(Theme.mono(15));
            FontMetrics nm = g2.getFontMetrics();
            int chip = 34;
            int cx = 14;
            int cy = (h - chip) / 2;
            g2.setColor(on ? Theme.withAlpha(Theme.BG_BARK, 50) : Theme.withAlpha(Theme.BG_GOLD, 45));
            g2.fillRoundRect(cx, cy, chip, chip, 12, 12);
            g2.setColor(on ? Theme.BG_BARK : Theme.BG_GOLD);
            g2.drawString(num, cx + (chip - nm.stringWidth(num)) / 2, cy + 23);

            // Title + difficulty in one clear row stack
            String name = map.getTitle();
            g2.setFont(Theme.bodyBold(16));
            FontMetrics fm = g2.getFontMetrics();
            int textX = 60;
            int maxText = w - 120;
            while (fm.stringWidth(name) > maxText && g2.getFont().getSize() > 13) {
                g2.setFont(Theme.bodyBold(g2.getFont().getSize() - 1));
                fm = g2.getFontMetrics();
            }
            g2.setColor(map.isLocked() ? Theme.MUTED_DARK : (on ? Theme.BG_BARK : Theme.CREAM));
            g2.drawString(name, textX, h / 2 - 2);

            g2.setFont(Theme.body(12));
            g2.setColor(on ? Theme.withAlpha(Theme.BG_BARK, 180) : Theme.BG_STEEL);
            g2.drawString(map.getDifficulty(), textX, h / 2 + 16);

            // Prize
            Image prize = AssetManager.get().bonusForStage(index);
            if (prize != null) {
                int icon = 30;
                int ix = w - icon - 18;
                int iy = (h - icon) / 2;
                g2.drawImage(prize, ix, iy, icon, icon, null);
            }

            g2.dispose();
        }
    }
}
