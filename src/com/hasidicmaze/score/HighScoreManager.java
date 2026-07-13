package com.hasidicmaze.score;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Persists top scores to scores.txt in the working directory.
 */
public final class HighScoreManager {
    private static final int MAX_ENTRIES = 10;
    private static final Path FILE = Paths.get("scores.txt");

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
        if (scores.size() < MAX_ENTRIES) {
            return score > 0;
        }
        return score > scores.get(scores.size() - 1).getScore();
    }

    public void add(String name, int score, String mapId) {
        String clean = name == null ? "Player" : name.trim();
        if (clean.isEmpty()) {
            clean = "Player";
        }
        if (clean.length() > 16) {
            clean = clean.substring(0, 16);
        }
        scores.add(new HighScore(clean, score, mapId, System.currentTimeMillis()));
        Collections.sort(scores);
        while (scores.size() > MAX_ENTRIES) {
            scores.remove(scores.size() - 1);
        }
        save();
    }

    private void seedDefaults() {
        scores.add(new HighScore("NOT.N.T", 481, "courtyard", 0));
        scores.add(new HighScore("MenMen", 411, "courtyard", 0));
        scores.add(new HighScore("Igor", 399, "twin-halls", 0));
        scores.add(new HighScore("Shmuel", 350, "crossroads", 0));
        scores.add(new HighScore("Oded", 294, "spiral-gate", 0));
        scores.add(new HighScore("Eitan", 288, "courtyard", 0));
        scores.add(new HighScore("Nattai", 277, "twin-halls", 0));
        scores.add(new HighScore("Dvir", 255, "crossroads", 0));
        scores.add(new HighScore("Shneor", 241, "courtyard", 0));
        scores.add(new HighScore("LeviYitzchak", 228, "spiral-gate", 0));
        Collections.sort(scores);
    }

    private void load() {
        if (!Files.isRegularFile(FILE)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    scores.add(new HighScore(
                        parts[0],
                        Integer.parseInt(parts[1]),
                        parts[2],
                        parts.length >= 4 ? Long.parseLong(parts[3]) : 0L
                    ));
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

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
            for (HighScore score : scores) {
                writer.write(score.getName() + "|" + score.getScore() + "|"
                    + score.getMapId() + "|" + score.getTimestamp());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not save scores: " + e.getMessage());
        }
    }
}
