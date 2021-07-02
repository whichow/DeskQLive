package com.whichow.deskqlive;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.App;
import com.whichow.deskqlive.QLive;

public class AndroidLauncher extends App {
    View qLiveView;
    QLive qLive;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blank);

        if (Settings.canDrawOverlays(AndroidLauncher.this)) {
            AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
            config.r = config.g = config.b = config.a = 8;
            qLive = new QLive();
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
            layoutParams.width = 500;
            layoutParams.height = 500;
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
            Toast.makeText(AndroidLauncher.this, "需要先允许显示在其他应用上层才可以使用哦！", Toast.LENGTH_LONG).show();
            startActivity(intent);
            finish();
        }
	}

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
	    if(qLiveView != null) {
            getWindowManager().removeView(qLiveView);
        }
        super.onDestroy();
    }
}
