package com.ypyglobal.xradio.firebasemsg;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ypyglobal.xradio.R;
import com.ypyglobal.xradio.XRadioSplashActivity;
import com.ypyglobal.xradio.ypylibs.utils.YPYLog;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;


public class XRadioFBMessagingService extends FirebaseMessagingService {
    private static final String TAG = "XRadioFBMessagingService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try{
            // TODO(developer): Handle FCM messages here.
            YPYLog.d(TAG, "From: " + remoteMessage.getFrom());
            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                YPYLog.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
                sendNotification(remoteMessage.getNotification().getBody());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */

    private void sendNotification(String messageBody) {
        try{
            if(TextUtils.isEmpty(messageBody)) return;

            Intent intent = new Intent(this, XRadioSplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            int flag = isAndroid12() ? (PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT) : PendingIntent.FLAG_ONE_SHOT;
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, flag);

            String channelId = getPackageName()+".N3";
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_notification_24dp)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        getPackageName()+"_CHANNEL1",
                        NotificationManager.IMPORTANCE_HIGH);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
            Notification mNotification = notificationBuilder.build();
            if (notificationManager != null) {
                notificationManager.notify(0 , mNotification);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    private boolean isAndroid12() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }
}
