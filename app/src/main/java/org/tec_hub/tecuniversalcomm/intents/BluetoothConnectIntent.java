package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.data.connection.BluetoothConnectionService;

/**
 * Created by Nick Mosher on 4/29/15.
 * Is a pre-built intent that targets the BluetoothConnectionService to
 * instruct the given connection to be disconnected.
 */
public class BluetoothConnectIntent extends Intent implements TECIntent {

    public BluetoothConnectIntent(Context context, BluetoothConnection connection) {
        super(context, BluetoothConnectionService.class);
        Preconditions.checkNotNull(connection);

        setAction(ACTION_BLUETOOTH_CONNECT);
        putExtra(BLUETOOTH_CONNECTION_DATA, connection);
    }
}
