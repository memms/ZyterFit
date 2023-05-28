package com.example.zyterfitlayout;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslCertificate;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class TrackingService extends Service {
    private String setActivityType, sessionName, sessionDesc;
    private Context context = this;
    private String identifier;
    private ActivityTrackActivity activityTrackActivity = new ActivityTrackActivity();
    private Intent intent1 = new Intent();
    private long elapsedTime;
    private long startTime = System.currentTimeMillis()+1;

    public static final String CHANNEL_ID = "MyNotificationChannelID";

    @Override
    public void onCreate() {
        super.onCreate();
        elapsedTime  = SystemClock.elapsedRealtime();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = ""+intent.getStringExtra("activitytype");
        sessionName= intent.getStringExtra("sessionName");
        sessionDesc= intent.getStringExtra("sessionDesc");
        setActivityType=intent.getStringExtra("setActivityType");

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "ZyterFit", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

            Intent notificationIntent = new Intent(this, ActivityTrackActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            int requestID = (int)System.currentTimeMillis();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Currently Tracking " + input)
                    .setContentText("Tap to see workout")
                    .setSmallIcon(R.drawable.ic_icon)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(2,notification);

        if(activityTrackActivity.getTracking(this) == false) {
            startTracking();
            Log.i("Service:" , "Tracking started");
        } else {
            intent1.setAction("ID");
            intent1.putExtra("SessionID", identifier);
            sendBroadcast(intent1);
        }
        intent1.setAction("time");
        intent1.putExtra("time", elapsedTime);
        intent1.putExtra("startT", startTime);
        sendBroadcast(intent1);

        return START_NOT_STICKY;
    }

    private void startTracking(){
        Calendar now =Calendar.getInstance();
        long startTime = now.getTimeInMillis();
        if(activityTrackActivity.getTracking(this) == false) {
            final Session session = new Session.Builder()
                    .setName(sessionName)
                    .setDescription(sessionDesc)
                    .setActivity(setActivityType)
                    .setStartTime(startTime, TimeUnit.MILLISECONDS)
                    .build();

            identifier = session.getIdentifier();

            final Task<Void> response = Fitness.getSessionsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .startSession(session);


            response.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    activityTrackActivity.setTracking(true, TrackingService.this);
                    intent1.setAction("ID");
                    intent1.putExtra("SessionID", identifier);
                    sendBroadcast(intent1);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
