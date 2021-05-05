package org.techtown.diary;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "alarm_id";
    public static final String CHANNEL_NAME = "alarm";

    private AlarmHelper alarmHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        alarmHelper = new AlarmHelper(context);
        //Toast.makeText(context, "알림입니다!!", Toast.LENGTH_LONG).show();
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                //notificationChannel.setVibrationPattern(new long[] {0, 500, 1000, 500});
                notificationManager.createNotificationChannel(notificationChannel);
            }

            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        Intent clickIntent = new Intent(context, MainActivity.class);
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentTitle("1일 1일기");
        builder.setContentText("오늘 하루 어떠셨나요? 1일 1일기 실천하러 가보세요!");
        builder.setSmallIcon(R.drawable.app_icon);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        //builder.setVibrate(new long[] {0, 500, 1000, 500});

        Notification notification = builder.build();
        notificationManager.notify(1, notification);

        alarmHelper.startAlarm(true);
    }
}