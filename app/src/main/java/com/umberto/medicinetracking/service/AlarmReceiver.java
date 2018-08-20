package com.umberto.medicinetracking.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && context != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                return;
            }
        }

        String title=intent.getStringExtra(NotificationScheduler.NOTIFICATION_TITLE);
        String content=intent.getStringExtra(NotificationScheduler.NOTIFICATION_CONTENT);
        int medicineId=intent.getIntExtra(NotificationScheduler.NOTIFICATION_MEDICINE_ID, -1);
        //Trigger the notification
        NotificationScheduler.showNotification(context,title,content,medicineId);

    }
}


