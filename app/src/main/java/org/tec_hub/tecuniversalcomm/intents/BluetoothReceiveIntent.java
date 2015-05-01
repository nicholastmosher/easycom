package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.connection.BluetoothConnection;

/**
 * Created by Nick Mosher on 4/30/15.
 */
public class BluetoothReceiveIntent extends Intent implements TECIntent {

    public BluetoothReceiveIntent(Context context, Class target, String data) {
        super(context, target);
        Preconditions.checkNotNull(data);

        setAction(ACTION_BLUETOOTH_UPDATE_INPUT);
        putExtra(BLUETOOTH_RECEIVED_DATA, data);
    }
}