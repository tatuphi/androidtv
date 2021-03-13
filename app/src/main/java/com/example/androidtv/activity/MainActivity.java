package com.example.androidtv.activity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.androidtv.MyBroadcastReceiver;
import com.example.androidtv.R;

public class MainActivity extends FragmentActivity {
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";
    int foodWeight = 0;
    int time = 0;

    EditText edtEatingTime;
    Button btnEatingTime, btnScanWifi;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnEatingTime = findViewById(R.id.btn_eating_time);
        btnScanWifi = findViewById(R.id.btn_scan_wifi);

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.custom_dialog, null);
        edtEatingTime = layout.findViewById(R.id.edt_time);

//        fact: food get from url at start;
        foodWeight = 300;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(layout);
        builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                condition = check connection between tv and object, weight > 50 gram
                if (!edtEatingTime.getText().toString().equals("")) {
                    time = Integer.parseInt(edtEatingTime.getText().toString());
                }
                if (foodWeight > 50 && time > 0) {
                    scheduleNotification(getNotification("this is test notification"));
                } else {
                    Toast.makeText(MainActivity.this, "Please put your food on", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnScanWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("abc", "list wifi scanned here");
            }
        });
        btnEatingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.show();
            }
        });
//        AlertDialog alertDialog = builder.create();
//        alertDialog.setView(layout);
//        if (alertDialog.getWindow() != null) {
//            int type;
//            type = WindowManager.LayoutParams.TYPE_TOAST;
//            alertDialog.getWindow().setType(type);
//        }
//        alertDialog.show();

//        scheduleNotification(getNotification( "this is test notification")) ;
    }

    private void scheduleNotification(Notification notification) {
        Intent notificationIntent = new Intent(this, MyBroadcastReceiver.class);
        int eatingTime = Integer.parseInt(edtEatingTime.getText().toString());
        int eatingSpeed = 0;
        if(eatingTime>0){
            eatingSpeed = (Integer) foodWeight/eatingTime;
        }
        notificationIntent.putExtra(MyBroadcastReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(MyBroadcastReceiver.NOTIFICATION, notification);
        notificationIntent.putExtra(MyBroadcastReceiver.EATING_TIME, eatingTime );
        notificationIntent.putExtra(MyBroadcastReceiver.START_TIME, System.currentTimeMillis());
        notificationIntent.putExtra(MyBroadcastReceiver.FOOD_WEIGHT, foodWeight);
        notificationIntent.putExtra(MyBroadcastReceiver.EATING_SPEED, eatingSpeed);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60000, pendingIntent);
        Toast.makeText(getApplicationContext(), "Alarm will set in 60 seconds", Toast.LENGTH_LONG).show();
    }

    private Notification getNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, default_notification_channel_id);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        return builder.build();
    }
}
