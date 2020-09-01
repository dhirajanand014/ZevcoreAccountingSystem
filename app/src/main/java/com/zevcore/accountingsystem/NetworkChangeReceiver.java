package com.zevcore.accountingsystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isOnline(context, intent)) {
            sendInternalBroadcast(context, true);
        } else {
            sendInternalBroadcast(context, false);
        }
    }

    /**
     * This method is responsible to send status by internal broadcast
     *
     * @param context
     * @param status
     */
    private void sendInternalBroadcast(Context context, boolean status) {
        try {
            Intent intent = new Intent();
            intent.putExtra("status", status);
            ObservableObject.getInstance().updateValue(intent);
        } catch (Exception ex) {
            Toast.makeText(context, "Cannot connect to internet", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if network available or not
     *
     * @param context
     * @param intent
     */
    public boolean isOnline(Context context, Intent intent) {
        boolean isOnline = false;
        try {
            //should check null because in airplane mode it will be null
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                isOnline = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            }

        } catch (Exception ex) {
            Toast.makeText(context, "Cannot connect to internet", Toast.LENGTH_SHORT).show();
        }
        return isOnline;
    }
}
