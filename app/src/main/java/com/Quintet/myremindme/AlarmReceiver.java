package com.Quintet.myremindme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    MediaPlayer mediaPlayer;
    @Override
    public void onReceive(Context context, Intent intent) {
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
        mediaPlayer.start();
        Toast.makeText(context, "Time's up", Toast.LENGTH_SHORT).show();


    }


}
