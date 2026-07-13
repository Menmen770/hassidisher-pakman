package com.hasidicmaze.ui;

import com.hasidicmaze.Theme;
import com.hasidicmaze.map.MapCatalog;
import com.hasidicmaze.score.HighScore;
import com.hasidicmaze.score.HighScoreManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class HighScorePanel extends AtmospherePanel {
    private final HighScoreManager manager;
    private final JPanel listPanel = new JPanel(new GridBagLayout());
    private final Map<String, String> mapTitles = new HashMap<>();

    public HighScorePanel(HighScoreManager manager, Consumer<String> navigate) {
        this.manager = manager;
        for (var map : MapCatalog.all()) {
            mapTitles.put(map.getId(), map.getTitle());
        }

        setLayout(new BorderLayout(0, 12));
        setPreferredSize(new Dimension(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT));
        setBorder(new EmptyBorder(28, 48, 28, 48));

        JLabel title = new JLabel("שיאים גבוהים", SwingConstants.CENTER);
        title.setFont(Theme.display(36));
        title.setForeground(Theme.GOLD_BRIGHT);
        add(title, BorderLayout.NORTH);

        listPanel.setOpaque(false);
        add(listPanel, BorderLayout.CENTER);
        refresh();

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        StyledButton back = new StyledButton("חזרה לתפריט");
        back.addActionListener(e -> navigate.accept("MENU"));
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        listPanel.removeAll();
        List<HighScore> scores = manager.topScores();
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 8, 6, 8);
        gc.weightx = 1;

        if (scores.isEmpty()) {
            JLabel empty = new JLabel("אין עדיין שיאים — תהיה הראשון!", SwingConstants.CENTER);
            empty.setFont(Theme.body(18));
            empty.setForeground(Theme.MUTED);
            gc.gridy = 0;
            listPanel.add(empty, gc);
        } else {
            for (int i = 0; i < scores.size(); i++) {
                HighScore hs = scores.get(i);
                JPanel row = buildRow(i + 1, hs);
                gc.gridy = i;
                listPanel.add(row, gc);
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildRow(int rank, HighScore hs) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(true);
        row.setBackground(rank <= 3 ? new java.awt.Color(40, 56, 86, 200) : new java.awt.Color(24, 32, 48, 160));
        row.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel rankLabel = new JLabel(String.format("%02d", rank));
        rankLabel.setFont(Theme.mono(18));
        rankLabel.setForeground(rank <= 3 ? Theme.GOLD_BRIGHT : Theme.GOLD);

        JLabel name = new JLabel(hs.getName());
        name.setFont(Theme.bodyBold(18));
        name.setForeground(Theme.CREAM);

        String mapName = mapTitles.getOrDefault(hs.getMapId(), hs.getMapId());
        JLabel map = new JLabel(mapName);
        map.setFont(Theme.body(14));
        map.setForeground(Theme.MUTED);

        JPanel left = new JPanel(new BorderLayout(14, 0));
        left.setOpaque(false);
        left.add(rankLabel, BorderLayout.WEST);
        JPanel mid = new JPanel(new BorderLayout());
        mid.setOpaque(false);
        mid.add(name, BorderLayout.NORTH);
        mid.add(map, BorderLayout.SOUTH);
        left.add(mid, BorderLayout.CENTER);

        JLabel score = new JLabel(String.valueOf(hs.getScore()));
        score.setFont(Theme.mono(22));
        score.setForeground(Theme.GOLD_BRIGHT);

        row.add(left, BorderLayout.CENTER);
        row.add(score, BorderLayout.EAST);
        return row;
    }
}
