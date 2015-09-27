package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;

import java.util.UUID;

/**
 * Created by Nick Mosher on 4/29/15.
 * Is a pre-built intent that targets the ConnectionService with the
 * provided BluetoothConnection to instruct a disconnect.
 */
public class BluetoothDisconnectIntent extends Intent implements TECIntent {

    /**
     * Creates an intent with the UUID of the Connection we're disconnecting.
     * @param context The context to launch the intent from.
     * @param uuid The UUID of the Connection to disconnect.
     */
    public BluetoothDisconnectIntent(Context context, UUID uuid) {
        this(context, uuid.toString());
    }

    /**
     * Creates an intent with the String UUID of the Connection we're disconnecting.
     * @param context The context to launch the intent from.
     * @param uuid The String UUID of the Connection to disconnect.
     */
    public BluetoothDisconnectIntent(Context context, String uuid) {
        super(context, ConnectionService.class);
        Preconditions.checkNotNull(uuid);

        setAction(ACTION_BLUETOOTH_DISCONNECT);
        putExtra(CONNECTION_TYPE, CONNECTION_TYPE_BLUETOOTH);
        putExtra(BLUETOOTH_CONNECTION_UUID, uuid);
    }
}
