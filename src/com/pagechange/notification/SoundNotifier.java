package com.pagechange.notification;

import com.pagechange.config.AppConstants;
import com.pagechange.util.Sleeper;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class SoundNotifier implements Notifier {
    private final Sleeper sleeper;
    private final int repeats;

    public SoundNotifier(Sleeper sleeper, int repeats) {
        this.sleeper = sleeper;
        this.repeats = repeats;
    }

    @Override
    public void notify(NotificationContext context) {
        playSound(repeats);
    }

    private void playSound(int repeats) {
        for (int j = 0; j < repeats; j++) {
            try {
                InputStream inputStream = getClass().getResourceAsStream(AppConstants.SOUND_RESOURCE_PATH);
                if (inputStream != null) {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.start();
                    sleeper.sleep(clip.getMicrosecondLength() / 950);
                    clip.close();
                    audioInputStream.close();
                    bufferedInputStream.close();
                }
            } catch (Exception e) {
                System.out.println("Blad podczas odtwarzania dzwieku: " + e.getMessage());
            }
        }
    }
}

