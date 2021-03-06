package com.example.androidtv;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION = "notification";
    public static String FOOD_WEIGHT = "food_weight";
    public static String EATING_TIME = "eating_time";
    public static String EATING_SPEED = "eating_speed";
    public static String START_TIME = "start_time";

    int widthToast = 0, heightToast = 0;
    int percent = 20;
    int widthScreen = 0, heightScreen = 0;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    public MyBroadcastReceiver() {
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onReceive(final Context context, Intent intent) {
        if(Intent.ACTION_SHUTDOWN.equals(intent.getAction()))
        {
            Log.d("abc", "System shutting down");
            context.stopService(new Intent(context, MyBroadcastReceiver.class));
        } else {
            Log.d("abc", "type action: " + intent.getAction());
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = intent.getParcelableExtra(NOTIFICATION);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(notificationChannel);
            }
            Log.d("abc", " here ");

            int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            int eatingTime = intent.getIntExtra(EATING_TIME, 0);
            final int eatingSpeed = intent.getIntExtra(EATING_SPEED, 0);
            final int startWeight = intent.getIntExtra(FOOD_WEIGHT, 0);
            final long startTime = intent.getLongExtra(START_TIME, 0);
            final long endTime = startTime + eatingTime*60*1000;

            assert notificationManager != null;
            notificationManager.notify(id, notification);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            widthScreen = displayMetrics.widthPixels;
            heightScreen = displayMetrics.heightPixels;
            Log.d("abc", "Width screen " + widthScreen);
            Log.d("abc", "height screen " + heightScreen);

            // Build Notification with Notification Manager
            new AsyncTask<String, String, Integer>() {
                @Override
                protected Integer doInBackground(String... strings) {
                    TestMain example = new TestMain();
                    String getResponse = "", number = null;
                    try {
                    getResponse = example.doGetRequest("http://172.16.6.220/loadcell");

//                        getResponse = example.doGetRequest("https://api64.ipify.org/?format=json");
                        Log.d("abc", "getResponse: " + getResponse);
//                    number = getResponse.substring(getResponse.indexOf("[") + 1, getResponse.indexOf("]"));
                        Log.d("abc", "getResponse: " + getResponse);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                return Integer.parseInt(getResponse);
                    int curWeight = Integer.parseInt(getResponse);
                    int result = getPercent(startTime, endTime, curWeight, startWeight, eatingSpeed);
                    return result;
                }

                @Override
                protected void onPostExecute(Integer s) {
                    super.onPostExecute(s);
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View layout = inflater.inflate(R.layout.custom_toast, null);
                    TextView text = (TextView) layout.findViewById(R.id.text);
                    text.setText("This is a custom toast");
                    if (s > 100) {
                        percent = 100;
                    } else {
                        percent = s;
                    }

                    text.measure(0, 0);
                    widthToast = (Integer) (widthScreen * percent / 100);
                    Log.d("abc", "width toast " + widthToast);
                    heightToast = (Integer) (heightScreen * percent / 100);
                    Log.d("abc", "height toast " + heightToast);

                    text.setWidth(widthToast);
                    text.setHeight(heightToast);

                    builder = new AlertDialog.Builder(context);
                    builder.setCancelable(false);
                    builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.setView(layout);
                    if (alertDialog.getWindow() != null) {
                        int type;
                        type = WindowManager.LayoutParams.TYPE_TOAST;
                        alertDialog.getWindow().setType(type);
                    }

                    if(percent > 0){
                        alertDialog.show();
                    } else {
                        alertDialog.dismiss();
                    }

                    Log.d("abc", "Result is main: " + s);
                    Runnable dismissDialog = new Runnable() {
                        @Override
                        public void run() {
                            if (alertDialog != null) {
                                alertDialog.dismiss();
                            }
                        }
                    };
                    new Handler().postDelayed(dismissDialog, Constants.DELAY_TIMER);
                }
            }.execute();
        }
    }



    public static class TestMain {
        OkHttpClient client = new OkHttpClient();

        // code request code here
        String doGetRequest(String url) throws IOException {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "text/html")
                    .build();
            Response response = client.newCall(request).execute();
            String finalResult = Objects.requireNonNull(response.body()).string();

            Log.d("abc", "the first: " + finalResult);
            return finalResult;
        }
    }

    int getPercent(long startTime, long endTime, int curWeight, int startWeight, int speed) {
        int percentResult = 0;
        long curTime = System.currentTimeMillis();
        int timeInMinutes = (int) (curTime - startTime) / (1000 * 60);
        int idealWeight = startWeight - (timeInMinutes * speed);
        if (curWeight - idealWeight > 0) {
            percentResult = (int) (curWeight - idealWeight) * 100 / speed;
        }
        return percentResult;
    }
}
