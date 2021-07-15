package com.whichow.deskqlive.rezero;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SwitchButtonListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context, QLiveService.class);
        Log.d("SwitchButtonListener", "onReceive: " + QLiveService.isQLiveShowing);
        if(QLiveService.isQLiveShowing) {
            newIntent.setAction(QLiveService.STOP_QLIVE);
        } else {
            newIntent.setAction(QLiveService.START_QLIVE);
        }
        context.startService(newIntent);
    }
}