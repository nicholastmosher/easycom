package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;

import java.util.UUID;

/**
 * Created by Nick Mosher on 9/15/15.
 */
public class TcpIpDisconnectIntent extends Intent implements TECIntent {

    /**
     * Creates an intent with the UUID of the Connection we want to disconnect.
     * @param context The context to launch the intent from.
     * @param uuid The UUID of the Connection to disconnect.
     */
    public TcpIpDisconnectIntent(Context context, UUID uuid) {
        this(context, uuid.toString());
    }

    /**
     * Creates an intent with the String UUID of the Connection we want to disconnect.
     * @param context The context to launch the intent from.
     * @param uuid The String UUID of the Connection to disconnect.
     */
    public TcpIpDisconnectIntent(Context context, String uuid) {
        super(context, ConnectionService.class);
        Preconditions.checkNotNull(uuid);

        setAction(ACTION_TCPIP_DISCONNECT);
        putExtra(CONNECTION_TYPE, CONNECTION_TYPE_TCPIP);
        putExtra(TCPIP_CONNECTION_UUID, uuid);
    }
}
