package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;

import java.util.UUID;

/**
 * Created by Nick Mosher on 4/29/15.
 * Is a pre-built intent that targets the ConnectionService that will
 * transfer String data to be sent over the given BluetoothConnection.
 */
public class BluetoothSendIntent extends Intent implements TECIntent {

    /**
     * Creates an intent with a UUID of the desired Connection.
     * @param context The context to launch the intent from.
     * @param uuid The UUID of the Connection.
     * @param data The data we're sending over the connection.
     */
    public BluetoothSendIntent(Context context, UUID uuid, byte[] data) {
        this(context, uuid.toString(), data);
    }

    /**
     * Creates an intent with a String-UUID of the desired Connection.
     * @param context The context to launch the intent from.
     * @param uuid The String UUID of the Connection.
     * @param data The data we're sending over the connection.
     */
    public BluetoothSendIntent(Context context, String uuid, byte[] data) {
        super(context, ConnectionService.class);
        Preconditions.checkNotNull(uuid);
        Preconditions.checkNotNull(data);

        setAction(ACTION_BLUETOOTH_SEND_DATA);
        putExtra(CONNECTION_TYPE, CONNECTION_TYPE_BLUETOOTH);
        putExtra(CONNECTION_UUID, uuid);
        putExtra(BLUETOOTH_TO_SEND_DATA, data);
    }
}
