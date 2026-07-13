package com.hasidicmaze.ui;

import com.hasidicmaze.Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class MenuPanel extends AtmospherePanel {
    public MenuPanel(Consumer<String> navigate) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT));

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 8, 0);

        JLabel brand = new JLabel("HASIDIC MAZE", SwingConstants.CENTER);
        brand.setFont(Theme.display(54));
        brand.setForeground(Theme.GOLD_BRIGHT);
        gc.gridy = 0;
        center.add(brand, gc);

        JLabel tagline = new JLabel("אסוף נקודות · הימנע מהרודפים · כבוש את המבוך", SwingConstants.CENTER);
        tagline.setFont(Theme.body(18));
        tagline.setForeground(Theme.CREAM);
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 36, 0);
        center.add(tagline, gc);

        gc.insets = new Insets(8, 0, 8, 0);
        StyledButton play = new StyledButton("התחל משחק");
        play.addActionListener(e -> navigate.accept("MAP_SELECT"));
        gc.gridy = 2;
        center.add(play, gc);

        StyledButton scores = new StyledButton("שיאים גבוהים");
        scores.addActionListener(e -> navigate.accept("HIGH_SCORES"));
        gc.gridy = 3;
        center.add(scores, gc);

        StyledButton quit = new StyledButton("יציאה");
        quit.addActionListener(e -> System.exit(0));
        gc.gridy = 4;
        center.add(quit, gc);

        gc.gridy = 5;
        gc.insets = new Insets(28, 0, 0, 0);
        JLabel hint = new JLabel("חיצים לתנועה · ESC לחזרה לתפריט", SwingConstants.CENTER);
        hint.setFont(Theme.body(14));
        hint.setForeground(Theme.MUTED);
        center.add(hint, gc);

        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 0, 18, 0));
        JLabel version = new JLabel("v2.0  ·  Java Edition");
        version.setFont(Theme.body(12));
        version.setForeground(Theme.MUTED);
        footer.add(version);
        add(footer, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Decorative gold line under brand area
        g2.setColor(Theme.GOLD);
        int lineW = 180;
        g2.fillRoundRect((getWidth() - lineW) / 2, 168, lineW, 3, 3, 3);
        g2.dispose();
    }
}
