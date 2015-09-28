package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import java.util.UUID;

/**
 * Created by Nick Mosher on 9/22/15.
 */
public class BluetoothDiscoveredIntent extends Intent implements TECIntent {

    /**
     * Creates an intent with a UUID of the Connection we've discovered.
     * @param context The context to launch the intent from.
     * @param cls The class that we're sending this intent to.
     * @param uuid The UUID of the Connection we've discovered.
     */
    public BluetoothDiscoveredIntent(Context context, Class<?> cls, UUID uuid) {
        this(context, cls, uuid.toString());
    }

    /**
     * Creates an intent with a String UUID of the Connection we've discovered.
     * @param context The context to launch the intent from.
     * @param cls The class that we're sending this intent to.
     * @param uuid The String UUID of the Connection we've discovered.
     */
    public BluetoothDiscoveredIntent(Context context, Class<?> cls, String uuid) {
        super(context, cls);
        Preconditions.checkNotNull(uuid);

        setAction(ACTION_BLUETOOTH_DISCOVERED);
        putExtra(CONNECTION_TYPE, CONNECTION_TYPE_BLUETOOTH);
        putExtra(BLUETOOTH_CONNECTION_UUID, uuid);
    }
}
