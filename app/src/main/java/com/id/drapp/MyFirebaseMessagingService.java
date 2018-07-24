package com.id.drapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "FirebaseMessagingServce";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String notificationTitle = null,notificationTime = null ,notificationBody = null;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        }


        if(notificationBody.equals(doctorPreference.getUserPushId(getApplicationContext()))){
            sendNotification(notificationTitle);
        }
    }


    private void sendNotification(String notificationTitle) {

        NotificationManager notificationManager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentTitle("New Appointment")
                .setContentText("You Have got new Appointment from: " + notificationTitle)
                .setWhen(Long.valueOf("1532163083138"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        "You Have got new Appointment from: " + notificationTitle))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(this))
                .setAutoCancel(true);
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationManager.notify(1566, notificationBuilder.build());
    }

    public static PendingIntent contentIntent(Context context) {

        Intent intent = new Intent(context, patientsListActivity.class);
        return PendingIntent.getActivity(context,
                1234,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


    }
}
