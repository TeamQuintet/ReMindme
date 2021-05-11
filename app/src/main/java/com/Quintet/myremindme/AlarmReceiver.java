package com.Quintet.myremindme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
    MediaPlayer mediaPlayer;
    public static final String channelID = "channelID";
    public static final String channelName = "Channel Name";
//    Context context;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
//        this.context = context;
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context,"remindme")
                .setContentTitle("Alarm!")
                .setContentText("Your AlarmManager is working.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(139, nb.build());

        mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
        mediaPlayer.start();
        Toast.makeText(context, "Time's up", Toast.LENGTH_SHORT).show();


    }


}
