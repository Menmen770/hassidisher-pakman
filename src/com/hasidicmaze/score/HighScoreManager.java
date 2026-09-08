package com.hasidicmaze.score;

import com.hasidicmaze.AppPaths;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Persists top scores to {@code data/scores.txt} as: rank|name|points
 */
public final class HighScoreManager {
    private static final int MAX_ENTRIES = 10;

    private final Path file = AppPaths.resolve("data", "scores.txt");
    private final List<HighScore> scores = new ArrayList<>();

    public HighScoreManager() {
        load();
        if (scores.isEmpty()) {
            seedDefaults();
            save();
        }
    }

    public List<HighScore> topScores() {
        return Collections.unmodifiableList(scores);
    }

    public boolean isTopScore(int score) {
        if (score <= 0) {
            return false;
        }
        if (scores.size() < MAX_ENTRIES) {
            return true;
        }
        return score > scores.get(scores.size() - 1).getScore();
    }

    /** 1-based rank this score would take if saved now, or -1 if not in top. */
    public int rankFor(int score) {
        if (!isTopScore(score)) {
            return -1;
        }
        int rank = 1;
        for (HighScore hs : scores) {
            if (score > hs.getScore()) {
                return rank;
            }
            rank++;
        }
        return Math.min(rank, MAX_ENTRIES);
    }

    public void add(String name, int score) {
        String clean = name == null ? "Player" : name.trim();
        if (clean.isEmpty()) {
            clean = "Player";
        }
        if (clean.length() > 16) {
            clean = clean.substring(0, 16);
        }
        scores.add(new HighScore(clean, score));
        Collections.sort(scores);
        while (scores.size() > MAX_ENTRIES) {
            scores.remove(scores.size() - 1);
        }
        save();
    }

    private void seedDefaults() {
        scores.add(new HighScore("NOT.N.T", 481));
        scores.add(new HighScore("MenMen", 411));
        scores.add(new HighScore("Igor", 399));
        scores.add(new HighScore("Shmuel", 350));
        scores.add(new HighScore("Oded", 294));
        scores.add(new HighScore("Eitan", 288));
        scores.add(new HighScore("Nattai", 277));
        scores.add(new HighScore("Dvir", 255));
        scores.add(new HighScore("Shneor", 241));
        scores.add(new HighScore("LeviYitzchak", 228));
        Collections.sort(scores);
    }

    private void load() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                HighScore parsed = parseLine(parts);
                if (parsed != null) {
                    scores.add(parsed);
                }
            }
            Collections.sort(scores);
            while (scores.size() > MAX_ENTRIES) {
                scores.remove(scores.size() - 1);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Could not load scores: " + e.getMessage());
            scores.clear();
        }
    }

    /** Accepts {@code rank|name|points} or legacy {@code name|points|...}. */
    private static HighScore parseLine(String[] parts) {
        if (parts.length >= 3 && isInt(parts[0])) {
            return new HighScore(parts[1].trim(), Integer.parseInt(parts[2].trim()));
        }
        if (parts.length >= 2) {
            return new HighScore(parts[0].trim(), Integer.parseInt(parts[1].trim()));
        }
        return null;
    }

    private static boolean isInt(String s) {
        try {
            Integer.parseInt(s.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void save() {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (int i = 0; i < scores.size(); i++) {
                    HighScore score = scores.get(i);
                    writer.write((i + 1) + "|" + score.getName() + "|" + score.getScore());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not save scores: " + e.getMessage());
        }
    }
}
