package com.hasidicmaze.ui;

import com.hasidicmaze.Theme;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

/** New high-score name entry — warm oak card. */
public class NewRecordPanel extends AtmospherePanel {
    private final JLabel headline = new JLabel("שיא חדש!", SwingConstants.CENTER);
    private final JLabel rankLine = new JLabel("", SwingConstants.CENTER);
    private final JLabel scoreLine = new JLabel("", SwingConstants.CENTER);
    private final JTextField nameField = new JTextField();
    private final SparkleOverlay sparkles = new SparkleOverlay();

    private int pendingScore;
    private final BiConsumer<String, Integer> onSaved;
    private final Consumer<Void> onDone;

    public NewRecordPanel(BiConsumer<String, Integer> onSaved, Consumer<Void> onDone) {
        this.onSaved = onSaved;
        this.onDone = onDone;

        setPreferredSize(new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT));
        setLayout(new BorderLayout());

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new OverlayLayout(stack));

        sparkles.setOpaque(false);
        sparkles.setAlignmentX(0.5f);
        sparkles.setAlignmentY(0.5f);

        JPanel content = buildContent();
        content.setOpaque(false);
        content.setAlignmentX(0.5f);
        content.setAlignmentY(0.5f);

        stack.add(content);
        stack.add(sparkles);
        stack.setComponentZOrder(content, 0);
        stack.setComponentZOrder(sparkles, 1);
        add(stack, BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(28, 40, 28, 40));

        Card card = new Card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(480, 440));
        card.setBorder(new EmptyBorder(32, 36, 32, 36));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        headline.setFont(Theme.display(36));
        headline.setForeground(Theme.BG_GOLD);
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 8, 0);
        card.add(headline, gc);

        rankLine.setFont(Theme.bodyBold(17));
        rankLine.setForeground(Theme.MUTED);
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 16, 0);
        card.add(rankLine, gc);

        scoreLine.setFont(Theme.mono(48));
        scoreLine.setForeground(Theme.CREAM);
        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 6, 0);
        card.add(scoreLine, gc);

        JLabel pts = new JLabel("נקודות", SwingConstants.CENTER);
        pts.setFont(Theme.body(15));
        pts.setForeground(Theme.MUTED);
        gc.gridy = 3;
        gc.insets = new Insets(0, 0, 22, 0);
        card.add(pts, gc);

        JLabel prompt = new JLabel("מה השם שלך?", SwingConstants.CENTER);
        prompt.setFont(Theme.bodyBold(16));
        prompt.setForeground(Theme.CREAM);
        gc.gridy = 4;
        gc.insets = new Insets(0, 0, 12, 0);
        card.add(prompt, gc);

        styleNameField();
        gc.gridy = 5;
        gc.insets = new Insets(0, 24, 20, 24);
        card.add(nameField, gc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        buttons.setPreferredSize(new Dimension(400, 52));
        StyledButton save = new StyledButton("שמור שיא", StyledButton.Variant.PRIMARY);
        save.setPreferredSize(new Dimension(180, 50));
        save.addActionListener(e -> submit());
        StyledButton skip = new StyledButton("דלג", StyledButton.Variant.GHOST);
        skip.setPreferredSize(new Dimension(120, 48));
        skip.addActionListener(e -> {
            onSaved.accept("Player", pendingScore);
            finish();
        });
        buttons.add(save);
        buttons.add(skip);
        gc.gridy = 6;
        card.add(buttons, gc);

        root.add(card, new GridBagConstraints());
        return root;
    }

    private void styleNameField() {
        nameField.setFont(Theme.bodyBold(20));
        nameField.setForeground(Theme.CREAM);
        nameField.setCaretColor(Theme.BG_GOLD);
        nameField.setBackground(Theme.BG_NAVY);
        nameField.setHorizontalAlignment(SwingConstants.CENTER);
        nameField.setPreferredSize(new Dimension(280, 50));
        nameField.setBorder(new AbstractBorder() {
            @Override
            public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = h;
                g2.setColor(Theme.BG_NAVY);
                g2.fillRoundRect(x, y, w - 1, h - 1, arc, arc);
                g2.setColor(Theme.BG_GOLD);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, arc, arc);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(java.awt.Component c) {
                return new Insets(10, 18, 10, 18);
            }
        });
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submit();
                }
            }
        });
    }

    public void prepare(int score, int rank) {
        this.pendingScore = score;
        scoreLine.setText(String.valueOf(score));
        if (rank == 1) {
            headline.setText("שיא עולמי!");
            rankLine.setText("מקום ראשון");
        } else {
            headline.setText("נכנסת לטופ!");
            rankLine.setText("מקום " + rank);
        }
        nameField.setText("");
        sparkles.start();
        SwingUtilities.invokeLater(() -> {
            nameField.requestFocusInWindow();
            nameField.selectAll();
        });
    }

    private void submit() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            name = "Player";
        }
        onSaved.accept(name, pendingScore);
        finish();
    }

    private void finish() {
        sparkles.stop();
        onDone.accept(null);
    }

    private static final class Card extends JPanel {
        Card() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = 28;
            g2.setColor(Theme.withAlpha(Theme.BG_BARK, 100));
            g2.fillRoundRect(4, 5, w - 8, h - 6, arc, arc);
            g2.setPaint(new java.awt.LinearGradientPaint(0, 0, 0, h, new float[]{0f, 1f},
                new Color[]{Theme.INK_SOFT, Theme.withAlpha(Theme.BG_BARK, 220)}));
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.setStroke(new BasicStroke(2.4f));
            g2.setColor(Theme.BG_GOLD);
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(Theme.withAlpha(Theme.BG_TAN, 110));
            g2.drawRoundRect(7, 7, w - 15, h - 15, arc - 8, arc - 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class SparkleOverlay extends JPanel {
        private final float[] x = new float[18];
        private final float[] y = new float[18];
        private final float[] vy = new float[18];
        private final float[] size = new float[18];
        private final float[] alpha = new float[18];
        private final Random rng = new Random();
        private Timer timer;

        SparkleOverlay() {
            setOpaque(false);
            for (int i = 0; i < x.length; i++) {
                reset(i, true);
            }
        }

        void start() {
            stop();
            timer = new Timer(33, e -> {
                for (int i = 0; i < x.length; i++) {
                    y[i] += vy[i];
                    alpha[i] -= 0.010f;
                    if (alpha[i] <= 0 || y[i] > getHeight() + 10) {
                        reset(i, false);
                    }
                }
                repaint();
            });
            timer.start();
        }

        void stop() {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }

        private void reset(int i, boolean scattered) {
            int w = Math.max(getWidth(), Theme.WINDOW_WIDTH);
            int h = Math.max(getHeight(), Theme.WINDOW_HEIGHT);
            x[i] = rng.nextFloat() * w;
            y[i] = scattered ? rng.nextFloat() * h : -10 - rng.nextFloat() * 40;
            vy[i] = 0.5f + rng.nextFloat() * 1.2f;
            size[i] = 1.5f + rng.nextFloat() * 2.5f;
            alpha[i] = 0.25f + rng.nextFloat() * 0.45f;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < x.length; i++) {
                int a = Math.max(0, Math.min(255, (int) (alpha[i] * 255)));
                g2.setColor(new Color(Theme.PAC_YELLOW.getRed(), Theme.PAC_YELLOW.getGreen(),
                    Theme.PAC_YELLOW.getBlue(), a));
                float s = size[i];
                g2.fillOval((int) x[i], (int) y[i], (int) s, (int) s);
            }
            g2.dispose();
        }
    }
}
