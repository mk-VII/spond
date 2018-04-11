package com.marcuthh.respond;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HCFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "SpondListeningService";

    @Override
    public void onMessageReceived(RemoteMessage message) {

        if (message.getNotification() != null) {
            String image = message.getNotification().getIcon();
            String title = message.getNotification().getTitle();
            String text = message.getNotification().getBody();
            String sound = message.getNotification().getSound();

            int id = 0;
            Object idObj = message.getData().get("id");
            if (idObj != null) {
                id = Integer.valueOf(idObj.toString());
            }

            if (id > 0) {
                sendNotification(new NotificationData(image, id, title, text, sound));
            }
        }
    }

    private void sendNotification(NotificationData notificationData) {
        Intent appIntent = new Intent(this, MainActivity.class);
        appIntent.putExtra(NotificationData.TEXT, notificationData.getMessageText());

        appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0 /*request code here*/,
                appIntent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = null;
        try {
            notificationBuilder = new NotificationCompat.Builder(this, "")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(URLDecoder.decode(notificationData.getMessageTitle(), "UTF-8"))
                    .setContentText(URLDecoder.decode(notificationData.getMessageText(), "UTF-8"))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }

}
