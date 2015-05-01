package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.connection.BluetoothConnection;
import org.tec_hub.tecuniversalcomm.connection.BluetoothConnectionService;

/**
 * Created by Nick Mosher on 4/29/15.
 * Is a pre-built intent that targets the BluetoothConnectionService with the
 * provided BluetoothConnection to instruct a disconnect.
 */
public class BluetoothDisconnectIntent extends Intent implements TECIntent {

    public BluetoothDisconnectIntent(Context context, BluetoothConnection connection) {
        super(context, BluetoothConnectionService.class);
        Preconditions.checkNotNull(connection);

        setAction(ACTION_BLUETOOTH_DISCONNECT);
        putExtra(BLUETOOTH_CONNECTION_DATA, connection);
    }
}
