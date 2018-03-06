package devnik.trancefestivalticker.background;

/**
 * Created by nik on 02.03.2018.
 */

import android.app.NotificationChannel;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import devnik.trancefestivalticker.R;
import devnik.trancefestivalticker.activity.MainActivity;
import devnik.trancefestivalticker.api.FestivalApi;
import devnik.trancefestivalticker.api.IAsyncResponse;
import devnik.trancefestivalticker.model.Festival;

public class MyService extends Service{
    private ArrayList<String> whatsNewList;
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Trigger new update pipeline, if there is an Update on Remote
        new FestivalApi(getApplicationContext()).execute();


        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mNotifyBuilder;
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
        //UPDATE for API 26 to set Max priority
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        // Sets an ID for the notification, so it can be updated
        int notifyID = 9001;
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_notify_msg)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.ic_stat_notify_msg))
                .setTicker("Hearty365")
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Hey du ✌. Es gibt was neues im TFT.")
                .setContentText(intent.getStringExtra("intntdata"))
                .setContentInfo("Info")
                .setContentIntent(resultPendingIntent);
        startForeground(notifyID, notificationBuilder.build());
        // Sets an ID for the notification, so it can be updated
        /*int notifyID = 9001;

        NotificationCompat.Builder NotifyBuilder = new android.support.v7.app.NotificationCompat.Builder(getApplicationContext(), notifyID)
                .setContentTitle("Alert")
                .setContentText("You've received new messages.")
                .setSmallIcon(R.mipmap.ic_action_refresh);
        // Set pending intent
        NotifyBuilder.setContentIntent(resultPendingIntent);
        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;
        NotifyBuilder.setDefaults(defaults);
        // Set the content for Notification

        NotifyBuilder.setContentText(intent.getStringExtra("intntdata"));
        // Set autocancel
        NotifyBuilder.setAutoCancel(true);
        // Post a notification
        mNotificationManager.notify(notifyID, NotifyBuilder.build());*/
        return startId;
    }
    public NotificationCompat.Builder initNotiBuilder(String title, String text, Intent intent){

        return null;
    }
    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

    }

}