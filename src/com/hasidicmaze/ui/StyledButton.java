package com.hasidicmaze.ui;

import com.hasidicmaze.Theme;
import com.hasidicmaze.sound.SoundId;
import com.hasidicmaze.sound.SoundManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Soft pill-round buttons in BG palette — polished pixel warmth.
 */
public class StyledButton extends JButton {
    public enum Variant {
        PRIMARY,
        SECONDARY,
        GHOST
    }

    private boolean hovered;
    private boolean pressed;
    private Variant variant = Variant.SECONDARY;

    public StyledButton(String text) {
        this(text, Variant.SECONDARY);
    }

    public StyledButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        setFont(Theme.bodyBold(16));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension d = new Dimension(260, 48);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        applyForeground();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                SoundManager.get().play(SoundId.MENU_SELECT);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                SoundManager.get().play(SoundId.MENU_CONFIRM);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
        applyForeground();
        repaint();
    }

    private void applyForeground() {
        setForeground(variant == Variant.PRIMARY ? Theme.PRIMARY_TEXT : Theme.CREAM);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth();
        int h = getHeight();
        int arc = h; // fully rounded capsule
        int y = pressed ? 1 : 0;
        int bh = h - 1 - y;

        if (variant != Variant.GHOST && isEnabled()) {
            g2.setColor(Theme.withAlpha(Theme.BG_BARK, 120));
            g2.fillRoundRect(3, 4 + y, w - 6, bh - 2, arc, arc);
        }

        if (variant == Variant.PRIMARY) {
            Color top = !isEnabled() ? Theme.MUTED_DARK
                : (hovered ? Theme.PAC_YELLOW_SOFT : Theme.BG_GOLD);
            Color mid = !isEnabled() ? Theme.MUTED_DARK : Theme.BG_ORANGE;
            Color bottom = !isEnabled() ? Theme.MUTED_DARK
                : (hovered ? Theme.BG_ORANGE : Theme.BG_RUST);
            g2.setPaint(new LinearGradientPaint(0, y, 0, h,
                new float[]{0f, 0.45f, 1f},
                new Color[]{top, mid, bottom}));
            g2.fillRoundRect(0, y, w - 1, bh, arc, arc);

            g2.setColor(Theme.withAlpha(Color.WHITE, hovered ? 70 : 45));
            g2.fillRoundRect(8, 5 + y, w - 17, Math.max(10, h / 3), arc / 2, arc / 2);

            g2.setColor(Theme.BG_BARK);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(1, 1 + y, w - 3, bh - 2, arc, arc);
            g2.setColor(Theme.withAlpha(Theme.BG_TAN, hovered ? 180 : 120));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(4, 4 + y, w - 9, bh - 8, arc - 4, arc - 4);
            setForeground(Theme.BG_BARK);
        } else if (variant == Variant.GHOST) {
            if (hovered) {
                g2.setColor(Theme.withAlpha(Theme.BG_NAVY, 170));
                g2.fillRoundRect(0, y, w - 1, bh, arc, arc);
            }
            g2.setColor(hovered ? Theme.BG_TAN : Theme.withAlpha(Theme.BG_STEEL, 200));
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(0, y, w - 1, bh - 1, arc, arc);
            setForeground(hovered ? Theme.BG_TAN : Theme.BG_STEEL);
        } else {
            Color top = !isEnabled() ? Theme.INK
                : (hovered ? Theme.INK_SOFT : Theme.withAlpha(Theme.BG_NAVY, 220));
            Color bottom = !isEnabled() ? Theme.INK
                : Theme.withAlpha(Theme.BG_BARK, hovered ? 200 : 175);
            g2.setPaint(new LinearGradientPaint(0, y, 0, h, new float[]{0f, 1f},
                new Color[]{top, bottom}));
            g2.fillRoundRect(0, y, w - 1, bh, arc, arc);

            g2.setColor(hovered ? Theme.BG_GOLD : Theme.BG_TAN);
            g2.setStroke(new BasicStroke(hovered ? 2.2f : 1.8f));
            g2.drawRoundRect(0, y, w - 1, bh - 1, arc, arc);
            g2.setColor(Theme.withAlpha(Theme.BG_WOOD, 140));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(3, 3 + y, w - 7, bh - 7, arc - 4, arc - 4);
            setForeground(Theme.CREAM);
        }

        g2.setFont(getFont());
        g2.setColor(isEnabled() ? getForeground() : Theme.MUTED_DARK);
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        g2.drawString(text, (w - fm.stringWidth(text)) / 2,
            y + (bh + fm.getAscent() - fm.getDescent()) / 2);
        g2.dispose();
    }
}
