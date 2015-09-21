package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.WifiConnection;
import org.tec_hub.tecuniversalcomm.data.connection.WifiConnectionService;

/**
 * Created by Nick Mosher on 9/15/15.
 */
public class WifiReceiveIntent extends Intent implements TECIntent {

    public WifiReceiveIntent(Context context, WifiConnection connection) {
        super(context, WifiConnectionService.class);
        Preconditions.checkNotNull(connection);

        setAction(ACTION_WIFI_UPDATE_INPUT);
        putExtra(WIFI_CONNECTION_DATA, connection);
    }
}
