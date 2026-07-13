package com.hasidicmaze.ui;

import com.hasidicmaze.Theme;
import com.hasidicmaze.assets.AssetManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import javax.swing.JPanel;

/** Shared atmospheric background for menu screens. */
public class AtmospherePanel extends JPanel {
    private final Image backdrop = AssetManager.get().background;

    public AtmospherePanel() {
        setOpaque(true);
        setBackground(Theme.INK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (backdrop != null) {
            g2.drawImage(backdrop, 0, 0, getWidth(), getHeight(), null);
        }

        Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(0, getHeight());
        LinearGradientPaint wash = new LinearGradientPaint(
            start, end,
            new float[]{0f, 0.45f, 1f},
            new Color[]{
                new Color(18, 24, 38, 210),
                new Color(18, 24, 38, 170),
                new Color(12, 16, 28, 230)
            }
        );
        g2.setPaint(wash);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Soft gold vignette glow at top
        g2.setColor(new Color(212, 168, 75, 28));
        g2.fillOval(getWidth() / 2 - 220, -80, 440, 220);

        g2.dispose();
    }
}
