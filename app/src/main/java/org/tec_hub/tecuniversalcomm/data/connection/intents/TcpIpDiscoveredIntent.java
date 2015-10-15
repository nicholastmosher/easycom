package org.tec_hub.tecuniversalcomm.data.connection.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import java.util.UUID;

/**
 * Created by Nick Mosher on 9/22/15.
 */
public class TcpIpDiscoveredIntent extends Intent implements ConnectionIntent { //TODO this may not be a permanent thing.

    /**
     * Creates an intent with the UUID of the Connection we've discovered.
     * @param context The context to launch the intent from.
     * @param cls The class to send the intent to.
     * @param uuid The UUID of the Connection we've discovered.
     */
    public TcpIpDiscoveredIntent(Context context, Class<?> cls, UUID uuid) {
        this(context, cls, uuid.toString());
    }

    /**
     * Creates an intent with the String UUID of the Connection we've discovered.
     * @param context The context to launch the intent from.
     * @param cls The class to send the intent to.
     * @param uuid The String UUID of the Connection we've discovered.
     */
    public TcpIpDiscoveredIntent(Context context, Class<?> cls, String uuid) {
        super(context, cls);
        Preconditions.checkNotNull(uuid);

        setAction(ACTION_TCPIP_DISCOVERED);
        putExtra(CONNECTION_TYPE, CONNECTION_TYPE_TCPIP);
        putExtra(CONNECTION_UUID, uuid);
    }
}