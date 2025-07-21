package com.ly2xxx.sos.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EmergencyReceiver extends BroadcastReceiver {
    
    private static final String TAG = "EmergencyReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Emergency receiver triggered");
        
        if (intent != null && "com.ly2xxx.sos.EMERGENCY_ACTION".equals(intent.getAction())) {
            // Handle emergency action (could be from widget, notification, etc.)
            Intent mainActivityIntent = new Intent(context, com.ly2xxx.sos.MainActivity.class);
            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(mainActivityIntent);
        }
    }
}