package org.tec_hub.tecuniversalcomm.data.connection.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;

import java.util.UUID;

/**
 * Created by Nick Mosher on 4/29/15.
 * Is a pre-built intent that targets the ConnectionService to
 * instruct the given connection to be disconnected.
 */
public class BluetoothConnectIntent extends Intent implements ConnectionIntent {

    /**
     * Creates an intent with the UUID of the desired Connection to connect.
     * @param context The context to launch the intent from.
     * @param uuid The UUID of the Connection we're connecting.
     */
    public BluetoothConnectIntent(Context context, UUID uuid) {
        this(context, uuid.toString());
    }

    /**
     * Creates an intent with the String UUID of the desired Connection to connect.
     * @param context The context to launch the intent from.
     * @param uuid The String UUID of the Connection we're connecting.
     */
    public BluetoothConnectIntent(Context context, String uuid) {
        super(context, ConnectionService.class);
        Preconditions.checkNotNull(uuid);

        setAction(ACTION_BLUETOOTH_CONNECT);
        putExtra(CONNECTION_TYPE, CONNECTION_TYPE_BLUETOOTH);
        putExtra(CONNECTION_UUID, uuid);
    }
}
