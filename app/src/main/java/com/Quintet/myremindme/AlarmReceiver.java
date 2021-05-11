package com.Quintet.myremindme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    MediaPlayer mediaPlayer;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        notificationHelper.getManager().notify(1, nb.build());

        mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
        mediaPlayer.start();
        Toast.makeText(context, "Time's up", Toast.LENGTH_SHORT).show();


    }


}
