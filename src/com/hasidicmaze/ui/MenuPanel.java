package com.hasidicmaze.ui;

import com.hasidicmaze.Screen;
import com.hasidicmaze.Theme;
import com.hasidicmaze.assets.AssetManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Main menu — at default size the art fills the width (top-anchored).
 * When the window grows wider, side navy mats appear instead of cropping more.
 */
public class MenuPanel extends AtmospherePanel {
    private static final int FRAME = 16;

    public MenuPanel(Consumer<Screen> navigate, Runnable onPlayCampaign) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setBorder(new EmptyBorder(0, 90, 78, 90));

        stack.add(Box.createVerticalGlue());
        stack.add(fixed(btn("שחק", StyledButton.Variant.PRIMARY, 18), 248, 52, e -> onPlayCampaign.run()));
        stack.add(Box.createVerticalStrut(14));
        stack.add(fixed(btn("בחירת שלב", StyledButton.Variant.SECONDARY, 15), 248, 46,
            e -> navigate.accept(Screen.MAP_SELECT)));
        stack.add(Box.createVerticalStrut(12));
        stack.add(fixed(btn("שיאים גבוהים", StyledButton.Variant.SECONDARY, 15), 248, 46,
            e -> navigate.accept(Screen.HIGH_SCORES)));
        stack.add(Box.createVerticalStrut(14));
        stack.add(fixed(btn("יציאה", StyledButton.Variant.GHOST, 13), 200, 38,
            e -> System.exit(0)));

        add(stack, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();
        g2.setColor(Theme.INK);
        g2.fillRect(0, 0, w, h);

        Image bg = AssetManager.get().menuBackground;
        int innerX = FRAME;
        int innerY = FRAME;
        int innerW = Math.max(1, w - FRAME * 2);
        int innerH = Math.max(1, h - FRAME * 2);

        g2.setColor(Theme.BG_NAVY);
        g2.fillRect(innerX, innerY, innerW, innerH);

        int imgX = innerX;
        int imgY = innerY;
        int imgW = innerW;
        int imgH = innerH;

        if (bg != null) {
            int iw = bg.getWidth(null);
            int ih = bg.getHeight(null);
            if (iw > 0 && ih > 0) {
                double defaultInnerW = Theme.WINDOW_WIDTH - FRAME * 2.0;
                double defaultInnerH = Theme.WINDOW_HEIGHT - FRAME * 2.0;
                double defaultAspect = defaultInnerW / defaultInnerH;
                double panelAspect = innerW / (double) innerH;

                if (panelAspect <= defaultAspect + 0.02) {
                    // Default / taller: fill width, pin top (the look you liked)
                    double scale = innerW / (double) iw;
                    int visibleSrcH = Math.min(ih, (int) Math.ceil(innerH / scale));
                    imgW = innerW;
                    imgH = Math.min(innerH, (int) Math.round(visibleSrcH * scale));
                    imgX = innerX;
                    imgY = innerY;
                    g2.drawImage(
                        bg,
                        imgX, imgY, imgX + imgW, imgY + imgH,
                        0, 0, iw, visibleSrcH,
                        null
                    );
                } else {
                    // Wider than default: keep full height visible, navy on the sides
                    double scale = innerH / (double) ih;
                    imgH = innerH;
                    imgW = Math.max(1, (int) Math.round(iw * scale));
                    imgX = innerX + (innerW - imgW) / 2;
                    imgY = innerY;
                    g2.drawImage(bg, imgX, imgY, imgW, imgH, null);
                }
            }
        } else {
            super.paintComponent(g);
        }

        int scrimTop = Math.max(imgY, (int) (h * 0.58f));
        int scrimBottom = imgY + imgH;
        if (scrimBottom > scrimTop) {
            g2.setPaint(new java.awt.GradientPaint(
                0, scrimTop, Theme.withAlpha(Theme.BG_NAVY, 0),
                0, scrimBottom, Theme.withAlpha(Theme.BG_NAVY, 110)
            ));
            g2.fillRect(imgX, scrimTop, imgW, scrimBottom - scrimTop);
        }

        drawProfessionalFrame(g2, w, h);
        g2.dispose();
    }

    private static StyledButton btn(String text, StyledButton.Variant variant, int fontSize) {
        StyledButton b = new StyledButton(text, variant);
        b.setFont(Theme.bodyBold(fontSize));
        return b;
    }

    private static Component fixed(StyledButton button, int w, int h,
                                   java.awt.event.ActionListener action) {
        Dimension d = new Dimension(w, h);
        button.setPreferredSize(d);
        button.setMinimumSize(d);
        button.setMaximumSize(d);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(action);
        return button;
    }
}
