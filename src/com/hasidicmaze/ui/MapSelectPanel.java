package com.hasidicmaze.ui;

import com.hasidicmaze.Theme;
import com.hasidicmaze.map.GameMap;
import com.hasidicmaze.map.MapCatalog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class MapSelectPanel extends AtmospherePanel {
    private final Consumer<GameMap> onPlay;
    private final Consumer<String> navigate;
    private GameMap selected = MapCatalog.all().stream()
        .filter(m -> !m.isLocked())
        .findFirst()
        .orElse(MapCatalog.all().get(0));
    private final StyledButton playButton;

    public MapSelectPanel(Consumer<GameMap> onPlay, Consumer<String> navigate) {
        this.onPlay = onPlay;
        this.navigate = navigate;
        setLayout(new BorderLayout(0, 16));
        setPreferredSize(new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("בחירת סדר", SwingConstants.CENTER);
        title.setFont(Theme.display(36));
        title.setForeground(Theme.GOLD_BRIGHT);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);
        for (GameMap map : MapCatalog.all()) {
            grid.add(new MapCard(map));
        }
        add(grid, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        bottom.setOpaque(false);
        StyledButton back = new StyledButton("חזרה");
        back.setPreferredSize(new Dimension(180, 48));
        back.addActionListener(e -> navigate.accept("MENU"));
        playButton = new StyledButton("שחק בסדר זה");
        playButton.setPreferredSize(new Dimension(220, 48));
        playButton.addActionListener(e -> {
            if (selected != null && !selected.isLocked()) {
                onPlay.accept(selected);
            }
        });
        updatePlayEnabled();
        bottom.add(back);
        bottom.add(playButton);
        add(bottom, BorderLayout.SOUTH);
    }

    private void updatePlayEnabled() {
        boolean ok = selected != null && !selected.isLocked();
        playButton.setEnabled(ok);
        playButton.setText(ok ? "שחק בסדר זה" : "סגור — בקרוב");
    }

    private class MapCard extends JPanel {
        private final GameMap map;
        private boolean hovered;

        MapCard(GameMap map) {
            this.map = map;
            setOpaque(false);
            setCursor(map.isLocked()
                ? Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
                : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setLayout(new BorderLayout(8, 8));
            setBorder(new EmptyBorder(12, 14, 12, 14));

            String titleText = map.isLocked() ? map.getTitle() : map.getTitle();
            JLabel name = new JLabel(titleText, SwingConstants.RIGHT);
            name.setFont(Theme.bodyBold(18));
            name.setForeground(map.isLocked() ? Theme.MUTED : Theme.CREAM);
            name.setOpaque(false);

            JLabel diff = new JLabel(
                map.isLocked() ? map.getDifficulty() + " · נעול" : map.getDifficulty(),
                SwingConstants.RIGHT
            );
            diff.setFont(Theme.bodyBold(13));
            diff.setForeground(map.isLocked() ? Theme.MUTED : Theme.GOLD);
            diff.setOpaque(false);

            String subtitle = map.isLocked() ? "בקרוב" : map.getSubtitle();
            JLabel desc = new JLabel("<html><div style='text-align:right'>" + subtitle + "</div></html>");
            desc.setFont(Theme.body(13));
            desc.setForeground(Theme.MUTED);
            desc.setOpaque(false);

            JPanel mini = map.isLocked() ? lockedPreview() : new MiniMapPreview(map);
            mini.setPreferredSize(new Dimension(120, 96));

            JPanel text = new JPanel(new BorderLayout(4, 6));
            text.setOpaque(false);
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(name, BorderLayout.CENTER);
            top.add(diff, BorderLayout.EAST);
            text.add(top, BorderLayout.NORTH);
            text.add(desc, BorderLayout.CENTER);

            add(mini, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (map.isLocked()) {
                        return;
                    }
                    hovered = true;
                    repaint();
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
                    selected = map;
                    updatePlayEnabled();
                    MapSelectPanel.this.repaint();
                    if (e.getClickCount() == 2) {
                        onPlay.accept(map);
                    }
                }
            });
        }

        private JPanel lockedPreview() {
            return new JPanel() {
                {
                    setOpaque(false);
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(30, 36, 48, 220));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.setColor(Theme.MUTED);
                    g2.setFont(Theme.bodyBold(16));
                    String lock = "נעול";
                    int tw = g2.getFontMetrics().stringWidth(lock);
                    g2.drawString(lock, (getWidth() - tw) / 2, getHeight() / 2 + 6);
                    g2.setColor(new Color(212, 168, 75, 70));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.dispose();
                }
            };
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean isSelected = !map.isLocked() && selected.getId().equals(map.getId());
            if (map.isLocked()) {
                g2.setColor(new Color(18, 22, 32, 210));
            } else {
                g2.setColor(isSelected ? new Color(40, 56, 86, 230) : new Color(24, 32, 48, 200));
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(map.isLocked()
                ? new Color(90, 96, 110, 120)
                : (isSelected || hovered ? Theme.GOLD : new Color(212, 168, 75, 90)));
            g2.setStroke(new java.awt.BasicStroke(isSelected ? 2.4f : 1.2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Tiny wall/path preview of the maze. */
    private static class MiniMapPreview extends JPanel {
        private final GameMap map;

        MiniMapPreview(GameMap map) {
            this.map = map;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int rows = map.getRows();
            int cols = map.getCols();
            float cellW = (getWidth() - 4f) / cols;
            float cellH = (getHeight() - 4f) / rows;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    char ch = map.charAt(r, c);
                    if (ch == 'X') {
                        g2.setColor(Theme.NAVY);
                    } else if (ch == 'P') {
                        g2.setColor(Theme.GOLD_BRIGHT);
                    } else if (ch == 'b' || ch == 'o' || ch == 'p' || ch == 'r') {
                        g2.setColor(Theme.ACCENT);
                    } else {
                        g2.setColor(new Color(60, 72, 96));
                    }
                    g2.fillRect(2 + Math.round(c * cellW), 2 + Math.round(r * cellH),
                        Math.max(1, Math.round(cellW)), Math.max(1, Math.round(cellH)));
                }
            }
            g2.setColor(Theme.GOLD);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
        }
    }
}
