package com.pagechange.notification;

import com.pagechange.config.AppConstants;
import com.pagechange.config.MonitoringConfig;
import com.pagechange.util.Sleeper;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final List<Notifier> notifiers;

    public NotificationService(List<Notifier> notifiers) {
        this.notifiers = notifiers;
    }

    public void notifyAll(NotificationContext context) {
        for (Notifier notifier : notifiers) {
            notifier.notify(context);
        }
    }

    public static NotificationService create(MonitoringConfig config, Sleeper sleeper) {
        List<Notifier> notifiers = new ArrayList<>();

        SoundNotifier soundNotifierForEmail = null;
        if (config.isSound()) {
            soundNotifierForEmail = new SoundNotifier(sleeper, AppConstants.SOUND_REPEATS_ON_EMAIL);
        }

        if (!config.getEmails().isEmpty()) {
            notifiers.add(new EmailNotifier(config.getEmails(), sleeper, soundNotifierForEmail));
        }

        if (config.isSound()) {
            notifiers.add(new SoundNotifier(sleeper, AppConstants.SOUND_REPEATS_ON_SUCCESS));
        }

        return new NotificationService(notifiers);
    }
}

