package com.whichow.deskqlive.rezero;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

public class AndroidLauncher extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        TableLayout table = (TableLayout)findViewById(R.id.image_table);
        TableRow row1 = new TableRow(this);
        Button remBtn = new Button(this);
        remBtn.setText("雷姆");
        remBtn.setBackgroundColor(Color.TRANSPARENT);
        Drawable remImage = getResources().getDrawable(R.drawable.rem_icon);
        remBtn.setCompoundDrawablesWithIntrinsicBounds(null, remImage , null, null);
        remBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getSharedPreferences("path", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("atlas", "house_rem/house_rem.atlas");
                editor.putString("skel", "house_rem/house_rem.skel");
                editor.commit();
                startQLiveApp();
            }
        });
        row1.addView(remBtn);
        Button beatriceBtn = new Button(this);
        beatriceBtn.setText("贝蒂");
        beatriceBtn.setBackgroundColor(Color.TRANSPARENT);
        Drawable beatriceImage = getResources().getDrawable(R.drawable.rem_icon);
        beatriceBtn.setCompoundDrawablesWithIntrinsicBounds(null, beatriceImage , null, null);
        beatriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getSharedPreferences("path", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("atlas", "house_beatrice/house_beatrice.atlas");
                editor.putString("skel", "house_beatrice/house_beatrice.skel");
                editor.commit();
                startQLiveApp();
            }
        });
        row1.addView(beatriceBtn);
        table.addView(row1);
        TableRow row2 = new TableRow(this);
        Button emiliaBtn = new Button(this);
        emiliaBtn.setText("艾米莉亚");
        emiliaBtn.setBackgroundColor(Color.TRANSPARENT);
        Drawable emiliaImage = getResources().getDrawable(R.drawable.rem_icon);
        emiliaBtn.setCompoundDrawablesWithIntrinsicBounds(null, emiliaImage , null, null);
        emiliaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getSharedPreferences("path", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("atlas", "house_emilia/house_emilia.atlas");
                editor.putString("skel", "house_emilia/house_emilia.skel");
                editor.commit();
                startQLiveApp();
            }
        });
        row2.addView(emiliaBtn);
        Button ramBtn = new Button(this);
        ramBtn.setText("拉姆");
        ramBtn.setBackgroundColor(Color.TRANSPARENT);
        Drawable ramImage = getResources().getDrawable(R.drawable.rem_icon);
        ramBtn.setCompoundDrawablesWithIntrinsicBounds(null, ramImage , null, null);
        ramBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getSharedPreferences("path", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("atlas", "house_ram/house_ram.atlas");
                editor.putString("skel", "house_ram/house_ram.skel");
                editor.commit();
                startQLiveApp();
            }
        });
        row2.addView(ramBtn);
        table.addView(row2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, QLiveService.class);
        stopService(intent);
    }

    private void startQLiveApp() {
        Intent intent = new Intent(this, QLiveApp.class);
        startActivity(intent);
    }
}
