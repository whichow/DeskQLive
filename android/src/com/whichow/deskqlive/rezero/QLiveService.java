package com.whichow.deskqlive.rezero;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AppService;

import java.io.IOException;
import java.lang.reflect.Method;

public class QLiveService extends AppService {
    private static final String TAG = "QLiveService";

    View qLiveView;
    QLive qLive;

    WindowManager windowManager;
    WindowManager.LayoutParams layoutParams;

//    NotificationManager notificationManager;
    Notification notification;
    RemoteViews remoteViews;

    public static final String START_QLIVE = "com.whichow.deskqlive.rezero.START_QLIVE";
    public static final String STOP_QLIVE = "com.whichow.deskqlive.rezero.STOP_QLIVE";

    private static final int VIEW_WIDTH = 500;
    private static final int VIEW_HEIGHT = 500;

    private static final String NOTIFICATION_TITLE = "从零开始的桌面萌宠";
    private static final String NOTIFICATION_TEXT = "要保护好人家不要被清理掉哦！";
    private static final String NOTIFICATION_CHANNEL_ID = "QLiveService";
    public static final int MANAGER_NOTIFICATION_ID = 0x1001;

    public static final String BUTTON_OFF_TEXT = "快走开啦";
    public static final String BUTTON_ON_TEXT = "给我回来";

    public static boolean isQLiveShowing = false;

    private void addQLiveView() {
        if(!isQLiveShowing && qLiveView != null) {
            windowManager.addView(qLiveView, layoutParams);
            isQLiveShowing = true;
        }
    }

    private void removeQLiveView() {
        if(isQLiveShowing && qLiveView != null) {
            getWindowManager().removeView(qLiveView);
            isQLiveShowing = false;
        }
    }

    private void collapseStatusBar(Context context) {
        try {
            Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;
            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action  = intent.getAction();
        Log.d(TAG, "onStartCommand: " + action);

        SharedPreferences sharedPref = getSharedPreferences("path", Context.MODE_PRIVATE);
        String iconPath = sharedPref.getString("icon", "");

        Drawable icon = null;
        try {
            icon = Drawable.createFromStream(getAssets().open(iconPath), null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        remoteViews.setImageViewBitmap(R.id.icon_img, ((BitmapDrawable)icon).getBitmap());

        if(intent.getAction().equals(START_QLIVE)) {
            addQLiveView();
            remoteViews.setTextViewText(R.id.switch_button, QLiveService.BUTTON_OFF_TEXT);
        } else if(intent.getAction().equals(STOP_QLIVE)) {
            removeQLiveView();
            remoteViews.setTextViewText(R.id.switch_button, QLiveService.BUTTON_ON_TEXT);
        }
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MANAGER_NOTIFICATION_ID, notification);

//        startForeground(MANAGER_NOTIFICATION_ID, notification);
        collapseStatusBar(this);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate () {
        super.onCreate();

        addForegroundNotification();
        
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.r = config.g = config.b = config.a = 8;
        SharedPreferences sharedPref = getSharedPreferences("path", Context.MODE_PRIVATE);
        String atlasPath = sharedPref.getString("atlas", "");
        String skelPath = sharedPref.getString("skel", "");
        qLive = new QLive(atlasPath, skelPath);
        qLiveView = initializeForView(qLive, config);
        if (qLiveView instanceof SurfaceView) {
            SurfaceView glView = (SurfaceView) qLiveView;
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setZOrderOnTop(true);
        }
//		initialize(new QLive(), config);

        windowManager = getWindowManager();
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.width = VIEW_WIDTH;
        layoutParams.height = VIEW_HEIGHT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        layoutParams.format = PixelFormat.TRANSPARENT;

        qLiveView.setOnTouchListener(new View.OnTouchListener() {
            float lastX, lastY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int x = (int) motionEvent.getRawX();
                int y = (int) motionEvent.getRawY();

                int[] location = new int[2];
                qLiveView.getLocationOnScreen(location);
                int viewX = location[0];
                int viewY = location[1];
                Point viewCenter = new Point(viewX + VIEW_WIDTH / 2, viewY + VIEW_HEIGHT / 2);

//                Log.d("QLiveApp", "onTouch: x " + x + " y " + y + ", layout x" + viewX + " y " + viewY);
                if(x < viewCenter.x - 150 || x > viewCenter.x + 150 || y < viewCenter.y - 200 || y > viewCenter.y + 200) {
                    return false;
                }
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = x;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        layoutParams.x += (int) (x - lastX);
                        layoutParams.y += (int) (y - lastY);
                        windowManager.updateViewLayout(qLiveView, layoutParams);
                        lastX = x;
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        qLive.changeAnimation();
                        break;
                }
                return true;
            }
        });
        addQLiveView();
    }

    @Override
    public void onDestroy() {
        removeQLiveView();
        Log.d("QLiveService", "onDestroy: ");
        super.onDestroy();
    }

    private void addForegroundNotification() {
        createNotificationChannel();
//        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        Intent switchIntent = new Intent(this, SwitchButtonListener.class);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 0, switchIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.switch_button, pendingSwitchIntent);
        remoteViews.setTextViewText(R.id.switch_button, BUTTON_OFF_TEXT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap())
//                .setContentTitle(NOTIFICATION_TITLE)
//                .setContentText(NOTIFICATION_TEXT)
                .setContent(remoteViews)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent msgIntent = getStartAppIntent(getApplicationContext());
        PendingIntent mainPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                msgIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification = notificationBuilder.setContentIntent(mainPendingIntent)
                .setAutoCancel(false).build();

//        notificationManager.notify(1, notification);
        startForeground(MANAGER_NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "DeskQLive";
//            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
//            channel.setDescription(description);
            channel.setShowBadge(false);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Intent getStartAppIntent(Context context) {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(getApplicationContext().getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }

        return intent;
    }
}
