package com.example.mygrocery;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.mygrocery.activities.OrderDetailsSellerActivity;
import com.example.mygrocery.activities.OrderDetailsUserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static  final String NOTIFICATION_CHANNEL_IO = "MY_NOTIFICATION_CHANNEL_IO";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        //all messages will be received here

        String notificationType = message.getData().get("notificationType");
        if (notificationType.equals("NewOrder")){
            String buyerUid = message.getData().get("buyerUid");
            String sellerUid = message.getData().get("sellerUid");
            String orderId = message.getData().get("orderId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationMessage = message.getData().get("notificationMessage");

            if (firebaseUser !=null && firebaseAuth.getUid().equals(sellerUid)){
                showNotification(orderId, sellerUid, buyerUid, notificationTitle,notificationMessage,notificationType);
            }

        }
        if (notificationType.equals("OrderStatusChanged")){
            String buyerUid = message.getData().get("buyerUid");
            String sellerUid = message.getData().get("sellerUid");
            String orderId = message.getData().get("orderId");
            String notificationTitle = message.getData().get("notificationTitle");
            String notificationMessage = message.getData().get("notificationMessage");

            if (firebaseUser !=null && firebaseAuth.getUid().equals(buyerUid)){
                showNotification(orderId, sellerUid, buyerUid, notificationTitle,notificationMessage,notificationType);
            }

        }
    }
    private void showNotification(String orderId, String sellerUid, String buyerUid, String notificationTitle, String notificationMessage, String notificationType){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setUpNotificationChannel(notificationManager);
        }
        Intent intent= null;
        if (notificationType.equals("NewOrder")){
            intent = new Intent(this, OrderDetailsSellerActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderBy", buyerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        else if (notificationType.equals("OrderStatusChanged")) {
            intent = new Intent(this, OrderDetailsUserActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderTo", sellerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.icon);
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_IO);
        notificationBuilder.setSmallIcon(R.drawable.icon)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setSound(notificationSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        notificationManager.notify(notificationID,notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Some text here";
        String channelDescription = "Channel Description here";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_IO, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        if (notificationManager !=null){
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
