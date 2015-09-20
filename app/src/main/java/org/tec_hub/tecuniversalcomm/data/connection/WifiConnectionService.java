package org.tec_hub.tecuniversalcomm.data.connection;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import org.tec_hub.tecuniversalcomm.intents.TECIntent;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Nick Mosher on 9/15/15.
 */
public class WifiConnectionService extends Service implements Observer {

    private static boolean launched = false;

    public void onCreate() {
        launched = true;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TECIntent.ACTION_WIFI_CONNECT);
        intentFilter.addAction(TECIntent.ACTION_WIFI_DISCONNECT);
        intentFilter.addAction(TECIntent.ACTION_WIFI_SEND_DATA);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(intent.getAction())
                {
                    //Received an action to establish wifi communication.
                    case TECIntent.ACTION_WIFI_CONNECT:

                        break;
                    //Received an action to disconnect wifi communication.
                    case TECIntent.ACTION_WIFI_DISCONNECT:

                        break;
                    //Received an action to send data over a wifi connection.
                    case TECIntent.ACTION_WIFI_SEND_DATA:

                        break;
                    default:
                }
            }
        }, intentFilter);

        return Service.START_STICKY;
    }

    public void onDestroy() {
        launched = false;
    }

    public static void launch(Context context) {
        if(!launched) {
            context.startService(new Intent(context, WifiConnectionService.class));
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (!(observable instanceof WifiConnection)) {
            throw new IllegalStateException("Update did not originate at a WifiConnection");
        }

        if (data instanceof Connection.ObserverCues) {
            WifiConnection wifiConnection = (WifiConnection) observable;
            Connection.ObserverCues cue = (Connection.ObserverCues) data;
            switch (cue) {
                case Connected:

                    break;
                case Disconnected:

                    break;
                case ConnectFailed:

                    break;
                default:
            }
        }
    }
}
