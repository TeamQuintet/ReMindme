package com.Quintet.myremindme;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationHelper<data> extends ContextWrapper {
    public static final String channelID = "channelID";
    public static final String channelName = "Channel Name";
    private NotificationManager mManager;
    public String data;
    public String task;
//    public String desc;
    public NotificationHelper(Context base, String data) {
        super(base);
        this.data = data;
//        this.desc = desc;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }
    Intent intent = new Intent(this, HomeActivity.class);
//    intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, HomeActivity.pendingIntentNo++, intent, 0);

//    Intent snoozeIntent = new Intent(this, AlarmReceiver.class);
////    snoozeIntent.setAction(ACTION_SNOOZE);
////    snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
//    PendingIntent snoozePendingIntent =
//            PendingIntent.getBroadcast(this, HomeActivity.pendingIntentNo++, snoozeIntent, 0);

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel);
    }
    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }
    public NotificationCompat.Builder getChannelNotification() {
//        CharSequence[] tasksData = data.split(" ");
//        CharSequence task = (CharSequence) tasksData[0];
//        CharSequence desc = (CharSequence) tasksData[1];
//        Log.i("task",task);
//        String date = data.split(" ")[2];
//        String time = data.split(" ")[3];
//        CharSequence text = task;
//                + " is due in " + date + " " + time;
        task = HomeActivity.list.get(data);
//

        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle("Reminder")
                .setContentText(task)
                .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
//                .setStyle(new NotificationCompat.InboxStyle()
//                        .addLine(HomeActivity.description))
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(HomeActivity.description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
//                .addAction(R.drawable.ic_baseline_snooze_24, "SNOOZE", snoozePendingIntent)
                .setAutoCancel(true);
    }
}
