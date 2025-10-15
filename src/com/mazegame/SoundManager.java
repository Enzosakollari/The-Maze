package com.mazegame;

import javax.sound.sampled.*;
import java.io.InputStream;

public class SoundManager {
    private Clip backgroundMusic;
    private float volume = 0.7f;
    private boolean isPlaying = false;

    public SoundManager() {
        loadBackgroundMusic();
    }

    private void loadBackgroundMusic() {
        try {
            InputStream is = getClass().getResourceAsStream("/sounds/main.wav");
            if (is != null) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(is);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioInput);

                // Set volume
                setClipVolume(volume); // Renamed to avoid conflict

                System.out.println("Background music loaded successfully!");
            } else {
                System.out.println("Background music not found: /sounds/background_music.wav");
                System.out.println("Please add background_music.wav to src/resources/sounds/");
            }
        } catch (Exception e) {
            System.out.println("Error loading background music: " + e.getMessage());
        }
    }

    private void setClipVolume(float volume) { // Renamed method
        if (backgroundMusic != null) {
            try {
                FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            } catch (Exception e) {
                System.out.println("Error setting volume: " + e.getMessage());
            }
        }
    }

    public void startGameMusic() {
        if (backgroundMusic != null && !isPlaying) {
            backgroundMusic.setFramePosition(0); // Start from beginning
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Loop forever
            backgroundMusic.start();
            isPlaying = true;
            System.out.println("Game music started (looping)");
        }
    }

    public void stopGameMusic() {
        if (backgroundMusic != null && isPlaying) {
            backgroundMusic.stop();
            isPlaying = false;
            System.out.println("Game music stopped");
        }
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        setClipVolume(this.volume); // Use renamed method
    }

    public boolean isMusicLoaded() {
        return backgroundMusic != null;
    }

    public float getVolume() {
        return volume;
    }
}