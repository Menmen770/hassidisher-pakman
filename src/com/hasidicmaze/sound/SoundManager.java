package com.hasidicmaze.sound;

import com.hasidicmaze.AppPaths;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Loads and plays files from {@code assets/sounds/}.
 * Supports WAV natively; MP3 via mp3spi on the classpath ({@code lib/}).
 */
public final class SoundManager {
    private static SoundManager instance;

    private final Map<SoundId, Clip> oneshots = new EnumMap<>(SoundId.class);
    private final Map<SoundId, Clip> loops = new EnumMap<>(SoundId.class);
    private boolean enabled = true;
    private int munchCooldown;

    private SoundManager() {
        load(SoundId.MUNCH, "munch.wav", false);
        load(SoundId.DEATH, "pacman_death.wav", false);
        load(SoundId.GAME_START, "game_start.mp3", false);
        load(SoundId.GAME_OVER, "common/game-over.mp3", false);
        load(SoundId.LEVEL_COMPLETE, "common/level-complete.mp3", false);
        load(SoundId.LEVEL_SWEEP, "common/sweep.mp3", false);
        load(SoundId.POWER, "ghost-turn-to-blue.mp3", true);
        load(SoundId.GHOST_EATEN, "eat_ghost.mp3", false);
        load(SoundId.GHOST_RETURNS, "retreating.mp3", true);
        load(SoundId.BONUS_EATEN, "eat_fruit.mp3", false);
        load(SoundId.EXTRA_LIFE, "extend.mp3", false);
        load(SoundId.CREDIT, "credit.wav", false);
        load(SoundId.MENU_SELECT, "menu-select1.wav", false);
        load(SoundId.MENU_CONFIRM, "menu-select2.wav", false);

        setVolume(SoundId.MUNCH, 0.50f);
        setVolume(SoundId.GAME_START, 0.50f);
        setVolume(SoundId.DEATH, 0.50f);
        setVolume(SoundId.GAME_OVER, 0.50f);
        setVolume(SoundId.POWER, 0.45f);
        setVolume(SoundId.GHOST_RETURNS, 0.45f);
    }

    public static synchronized SoundManager get() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            stopAll();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void play(SoundId id) {
        if (!enabled || id == null) {
            return;
        }
        Clip clip = oneshots.get(id);
        if (clip == null) {
            return;
        }
        try {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        } catch (Exception e) {
            System.err.println("Sound play failed (" + id + "): " + e.getMessage());
        }
    }

    /** Munch with short cooldown so pellets don't spam. */
    public void playMunch() {
        if (munchCooldown > 0) {
            return;
        }
        play(SoundId.MUNCH);
        munchCooldown = 3;
    }

    public void tickCooldown() {
        if (munchCooldown > 0) {
            munchCooldown--;
        }
    }

    public void loop(SoundId id) {
        if (!enabled || id == null) {
            return;
        }
        Clip clip = loops.get(id);
        if (clip == null) {
            return;
        }
        try {
            if (clip.isRunning()) {
                return;
            }
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("Sound loop failed (" + id + "): " + e.getMessage());
        }
    }

    public void stop(SoundId id) {
        Clip clip = loops.get(id);
        if (clip == null) {
            clip = oneshots.get(id);
        }
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    public void stopAll() {
        for (Clip clip : loops.values()) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
                clip.setFramePosition(0);
            }
        }
        for (Clip clip : oneshots.values()) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
                clip.setFramePosition(0);
            }
        }
    }

    /** Ambient: power while frightened, eyes while returning. */
    public void updateAmbient(boolean playing, boolean frightened, boolean ghostReturning) {
        if (!enabled || !playing) {
            stop(SoundId.POWER);
            stop(SoundId.GHOST_RETURNS);
            return;
        }
        if (frightened) {
            loop(SoundId.POWER);
        } else {
            stop(SoundId.POWER);
        }
        if (ghostReturning) {
            loop(SoundId.GHOST_RETURNS);
        } else {
            stop(SoundId.GHOST_RETURNS);
        }
    }

    private void setVolume(SoundId id, float linear) {
        Clip clip = loops.get(id);
        if (clip == null) {
            clip = oneshots.get(id);
        }
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float clamped = Math.max(0.0001f, Math.min(1f, linear));
        float db = (float) (20.0 * Math.log10(clamped));
        db = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db));
        gain.setValue(db);
    }

    private void load(SoundId id, String relative, boolean looping) {
        File file = resolve(relative);
        if (file == null || !file.isFile()) {
            System.err.println("Missing sound: assets/sounds/" + relative);
            return;
        }
        try {
            Clip clip = openClip(file);
            if (looping) {
                loops.put(id, clip);
            } else {
                oneshots.put(id, clip);
            }
        } catch (Exception e) {
            System.err.println("Could not load sound " + relative + ": " + e.getMessage());
        }
    }

    private static Clip openClip(File file)
        throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        AudioInputStream in = AudioSystem.getAudioInputStream(file);
        AudioFormat base = in.getFormat();
        AudioFormat decoded = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            base.getSampleRate() <= 0 ? 44100f : base.getSampleRate(),
            16,
            base.getChannels() <= 0 ? 2 : base.getChannels(),
            (base.getChannels() <= 0 ? 2 : base.getChannels()) * 2,
            base.getSampleRate() <= 0 ? 44100f : base.getSampleRate(),
            false
        );
        if (!base.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
            in = AudioSystem.getAudioInputStream(decoded, in);
        }
        Clip clip = AudioSystem.getClip();
        clip.open(in);
        in.close();
        return clip;
    }

    private static File resolve(String relative) {
        Path path = AppPaths.resolve("assets", "sounds").resolve(relative);
        File file = path.toFile();
        return file.isFile() ? file : null;
    }
}
