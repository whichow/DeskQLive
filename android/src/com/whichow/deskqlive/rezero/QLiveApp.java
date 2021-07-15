package com.whichow.deskqlive.rezero;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    private static final String DIALOG_TEXT = "需要先允许人家显示在最上层才可以食用哦！";

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                content.setBackground(bgImg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(this, QLiveService.class);
                intent.setAction(QLiveService.START_QLIVE);
                startService(intent);
            } else {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    //            dialog.setIcon(R.drawable.ic_launcher);
    //            dialog.setTitle(DIALOG_TITLE);
                dialog.setMessage(DIALOG_TEXT);
                dialog.setPositiveButton("好的呢",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                intent.setData(Uri.parse("package:" + getPackageName()));
    //                            Toast.makeText(this, "需要先允许显示在其他应用上层才可以使用哦！", Toast.LENGTH_LONG).show();
                                startActivity(intent);
                                finish();
                            }
                        });
                dialog.setNegativeButton("才不要", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                dialog.show();
            }
        }

        Button confirmBtn = (Button)findViewById(R.id.confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                // finish();
            }
        });
    }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent(this, QLiveService.class);
//        stopService(intent);
//        super.onBackPressed();
//    }
}
