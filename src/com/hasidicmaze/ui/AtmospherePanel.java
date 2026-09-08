package com.hasidicmaze.ui;

import com.hasidicmaze.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import javax.swing.JPanel;

/** Shared stage look — navy glow + gold/steel frame (matches main menu). */
public class AtmospherePanel extends JPanel {
    protected static final int FRAME = 16;

    public AtmospherePanel() {
        setOpaque(true);
        setBackground(Theme.INK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        g2.setPaint(new LinearGradientPaint(
            0, 0, 0, h,
            new float[]{0f, 1f},
            new Color[]{Theme.INK_SOFT, Theme.BG_NAVY}
        ));
        g2.fillRect(0, 0, w, h);

        g2.setPaint(new RadialGradientPaint(
            new Point2D.Float(w * 0.5f, h * 0.18f),
            Math.max(w, h) * 0.5f,
            new float[]{0f, 1f},
            new Color[]{
                Theme.withAlpha(Theme.BG_GOLD, 28),
                Theme.withAlpha(Theme.BG_GOLD, 0)
            }
        ));
        g2.fillRect(0, 0, w, h);

        drawProfessionalFrame(g2, w, h);
        g2.dispose();
    }

    protected static void drawProfessionalFrame(Graphics2D g2, int w, int h) {
        int m = FRAME;
        g2.setColor(Theme.TITLE_BAR);
        g2.fillRect(0, 0, w, m);
        g2.fillRect(0, h - m, w, m);
        g2.fillRect(0, 0, m, h);
        g2.fillRect(w - m, 0, m, h);

        g2.setColor(Theme.withAlpha(Theme.BG_STEEL, 180));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(m - 1, m - 1, w - (m - 1) * 2 - 1, h - (m - 1) * 2 - 1);

        g2.setColor(Theme.withAlpha(Theme.BG_STONE, 160));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRect(m + 3, m + 3, w - (m + 3) * 2 - 1, h - (m + 3) * 2 - 1);

        int c = 18;
        g2.setStroke(new BasicStroke(1.6f));
        g2.setColor(Theme.withAlpha(Theme.BG_GOLD, 210));
        int x0 = m + 3;
        int y0 = m + 3;
        int x1 = w - m - 4;
        int y1 = h - m - 4;
        g2.drawLine(x0, y0, x0 + c, y0);
        g2.drawLine(x0, y0, x0, y0 + c);
        g2.drawLine(x1, y0, x1 - c, y0);
        g2.drawLine(x1, y0, x1, y0 + c);
        g2.drawLine(x0, y1, x0 + c, y1);
        g2.drawLine(x0, y1, x0, y1 - c);
        g2.drawLine(x1, y1, x1 - c, y1);
        g2.drawLine(x1, y1, x1, y1 - c);
    }
}
