package com.id.drapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class patientIntentService extends IntentService {
    private static volatile int FOREGROUND_ID = 1338;

    private static final int ACTION_CANCEL_SYNCING = 1290;
    private static final int CONTENT_ACTION_PENDING_INTENT = 1291;

    public static volatile boolean shouldContinue = true;
    private executeBackgroundTask task;

    public patientIntentService() {
        super("patientIntentService");
        task = new executeBackgroundTask(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        startForeground(FOREGROUND_ID,
                buildForegroundNotification("Please Don't Close App"));

        task.queryTheDatabase();


        stopForeground(true);
    }




    private Notification buildForegroundNotification(String filename) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this);

        b.setContentTitle("Syncing..")
                .setContentText(filename)
                .setColor(getResources().getColor(R.color.actionBar))
                .setContentIntent(contentIntent(this))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .addAction(FirstAction(this, "Stop"))
                .setTicker("Syncing..");

        return (b.build());
    }

    public static NotificationCompat.Action FirstAction(Context context, String firstActionText) {

        Intent ignoreReminderIntent = new Intent(context, notificationIntentService.class);
        PendingIntent ignoreReminderPendingIntent = PendingIntent.getService(
                context,
                ACTION_CANCEL_SYNCING,
                ignoreReminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_launcher_background,
                firstActionText,
                ignoreReminderPendingIntent);
        return action;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        stopSelf();

    }

    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                CONTENT_ACTION_PENDING_INTENT,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
