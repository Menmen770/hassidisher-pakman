package com.hasidicmaze.ui;

import com.hasidicmaze.Theme;
import com.hasidicmaze.assets.AssetManager;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Ebony title bar with a thin oak accent. */
public class TitleBar extends JPanel {
    public static final int HEIGHT = 32;

    private static final int BTN_W = 44;
    private static final int BTN_COUNT = 3;
    private static final int ICON = 14;

    private final JFrame frame;
    private final Image appIcon;
    private Point dragOffset;
    private Rectangle restoreBounds;
    private int hoverIndex = -1;

    public TitleBar(JFrame frame) {
        this.frame = frame;
        this.appIcon = AssetManager.get().appIcon;
        setOpaque(true);
        setBackground(Theme.TITLE_BAR);
        setLayout(null);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || buttonAt(e.getX()) >= 0) {
                    return;
                }
                dragOffset = e.getLocationOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset == null || frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    return;
                }
                Point now = e.getLocationOnScreen();
                Point loc = frame.getLocation();
                frame.setLocation(loc.x + now.x - dragOffset.x, loc.y + now.y - dragOffset.y);
                dragOffset = now;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragOffset = null;
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                int btn = buttonAt(e.getX());
                if (btn == 0) {
                    frame.setState(Frame.ICONIFIED);
                } else if (btn == 1) {
                    toggleMaximize();
                } else if (btn == 2) {
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && buttonAt(e.getX()) < 0) {
                    toggleMaximize();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int next = buttonAt(e.getX());
                if (next != hoverIndex) {
                    hoverIndex = next;
                    setCursor(next >= 0
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoverIndex != -1) {
                    hoverIndex = -1;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, HEIGHT);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, HEIGHT);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, HEIGHT);
    }

    private int buttonAt(int x) {
        int start = getWidth() - BTN_W * BTN_COUNT;
        if (x < start) {
            return -1;
        }
        return Math.min(BTN_COUNT - 1, (x - start) / BTN_W);
    }

    private Rectangle buttonBounds(int index) {
        return new Rectangle(getWidth() - BTN_W * (BTN_COUNT - index), 0, BTN_W, HEIGHT);
    }

    private void toggleMaximize() {
        if (frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
            frame.setExtendedState(Frame.NORMAL);
            if (restoreBounds != null) {
                frame.setBounds(restoreBounds);
            }
        } else {
            restoreBounds = frame.getBounds();
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(Theme.TITLE_BAR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        int midY = HEIGHT / 2;
        int x = 10;
        if (appIcon != null) {
            g2.drawImage(appIcon, x, midY - ICON / 2, ICON, ICON, null);
            x += ICON + 8;
        }
        g2.setFont(Theme.bodyBold(12));
        g2.setColor(Theme.CREAM_SOFT);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("PAC-MANDY", x, midY + fm.getAscent() / 2 - 1);

        // Logo-blue accent rail
        g2.setColor(Theme.LOGO_BLUE_DEEP);
        g2.fillRect(0, HEIGHT - 2, getWidth(), 2);
        g2.setColor(Theme.withAlpha(Theme.LOGO_BLUE, 180));
        g2.fillRect(0, HEIGHT - 1, getWidth(), 1);

        for (int i = 0; i < BTN_COUNT; i++) {
            Rectangle r = buttonBounds(i);
            if (hoverIndex == i) {
                g2.setColor(i == 2 ? Theme.ACCENT : Theme.INK_LIFT);
                g2.fillRect(r.x, r.y, r.width, r.height - 2);
            }
            g2.setColor(Theme.CREAM);
            g2.setStroke(new BasicStroke(1.1f));
            float cx = r.x + r.width / 2f;
            float cy = r.y + r.height / 2f;
            if (i == 0) {
                g2.draw(new Line2D.Float(cx - 5f, cy, cx + 5f, cy));
            } else if (i == 1) {
                g2.draw(new Rectangle2D.Float(cx - 4f, cy - 4f, 8, 8));
            } else {
                g2.draw(new Line2D.Float(cx - 4f, cy - 4f, cx + 4f, cy + 4f));
                g2.draw(new Line2D.Float(cx + 4f, cy - 4f, cx - 4f, cy + 4f));
            }
        }
        g2.dispose();
    }
}
