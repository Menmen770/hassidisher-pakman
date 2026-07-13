package com.hasidicmaze.ui;

import com.hasidicmaze.Theme;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/** Gold-bordered navy button used across menu screens. */
public class StyledButton extends JButton {
    private boolean hovered;

    public StyledButton(String text) {
        super(text);
        setFont(Theme.bodyBold(18));
        setForeground(Theme.CREAM);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(280, 52));
        setMaximumSize(new Dimension(320, 52));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovered ? Theme.BUTTON_HOVER : Theme.BUTTON);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g2.setColor(Theme.BUTTON_BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g2.setColor(getForeground());
        g2.setFont(getFont());
        int textWidth = g2.getFontMetrics().stringWidth(getText());
        int textHeight = g2.getFontMetrics().getAscent();
        g2.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 3);
        g2.dispose();
    }
}
