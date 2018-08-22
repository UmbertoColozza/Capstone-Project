package com.umberto.medicinetracking.service;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.umberto.medicinetracking.R;
import com.umberto.medicinetracking.ui.MainActivity;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

public class NotificationScheduler
{
    public static final int MEDICINE_REQUEST_CODE=100;
    public static final String CHANNEL_ID="1234";
    public static final String CHANNEL_NAME="medicine";
    public static final String NOTIFICATION_TITLE="notification_title";
    public static final String NOTIFICATION_CONTENT="notification_content";
    public static final String NOTIFICATION_MEDICINE_ID="notification_medicine_id";

    public static void setReminder(Context context, Date dateAlarm, String title,String content, int medicineId)
    {
        // Enable a receiver
        ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);


        Intent intent1 = new Intent(context, AlarmReceiver.class);
        intent1.putExtra(NOTIFICATION_TITLE, title);
        intent1.putExtra(NOTIFICATION_CONTENT, content);
        intent1.putExtra(NOTIFICATION_MEDICINE_ID, medicineId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MEDICINE_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC, dateAlarm.getTime(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }

    public static void cancelReminder(Context context)
    {
        // Disable a receiver
        ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Intent intent1 = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MEDICINE_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public static void showNotification(Context context, String title, String content,int medicineId)
    {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra(NOTIFICATION_TITLE, title);
        notificationIntent.putExtra(NOTIFICATION_CONTENT, content);
        notificationIntent.putExtra(NOTIFICATION_MEDICINE_ID, medicineId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(MEDICINE_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);

        //Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setChannelId("4565")
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert notificationManager != null;
            builder.setChannelId("1234");
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(MEDICINE_REQUEST_CODE, builder.build());

    }

}
