package com.whichow.deskqlive.rezero;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.App;

import java.io.IOException;
import java.util.Random;

public class QLiveApp extends App {
    View qLiveView;
    QLive qLive;

    private static final int VIEW_WIDTH = 500;
    private static final int VIEW_HEIGHT = 500;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blank);

        LinearLayout content = (LinearLayout)findViewById(R.id.content);

        try {
            String[] fileNames = getResources().getAssets().list("bg");
            Random rand = new Random();
            int n = rand.nextInt(fileNames.length);
            Drawable bgImg = Drawable.createFromStream(getAssets().open("bg/" + fileNames[n]), null);
            Log.d("QLiveApp", "bgImg: " + bgImg);
            content.setBackground(bgImg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button confirmBtn = (Button)findViewById(R.id.confirm);
        Log.d("App", "confirmBtn: " + confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        if (Settings.canDrawOverlays(QLiveApp.this)) {
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

            final WindowManager windowManager = getWindowManager();
            final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
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

                    Log.d("QLiveApp", "onTouch: x " + x + " y " + y + ", layout x" + viewX + " y " + viewY);
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

            windowManager.addView(qLiveView, layoutParams);
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            Toast.makeText(QLiveApp.this, "需要先允许显示在其他应用上层才可以使用哦！", Toast.LENGTH_LONG).show();
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(qLiveView != null) {
            getWindowManager().removeView(qLiveView);
        }
        super.onDestroy();
    }
}
