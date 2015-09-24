package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;

/**
 * Created by Nick Mosher on 9/22/15.
 */
public class BluetoothDiscoveredIntent extends Intent implements TECIntent {

    public BluetoothDiscoveredIntent(Context context, Class<?> cls, BluetoothConnection connection) {
        super(context, cls);
        Preconditions.checkNotNull(connection);

        setAction(ACTION_BLUETOOTH_DISCOVREED);
        putExtra(CONNECTION_TYPE, CONNECTION_TYPE_BLUETOOTH);
        putExtra(BLUETOOTH_CONNECTION_DATA, connection);
    }
}
