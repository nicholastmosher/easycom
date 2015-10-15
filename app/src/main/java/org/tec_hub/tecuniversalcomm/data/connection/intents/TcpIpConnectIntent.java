package org.tec_hub.tecuniversalcomm.data.connection.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;

import java.util.UUID;

/**
 * Created by Nick Mosher on 9/15/15.
 */
public class TcpIpConnectIntent extends Intent implements ConnectionIntent {

    /**
     * Creates an intent with a UUID of the Connection we want to connect.
     * @param context The context to launch the intent from.
     * @param uuid The UUID of the Connection we want to connect.
     */
    public TcpIpConnectIntent(Context context, UUID uuid) {
        this(context, uuid.toString());
    }

    /**
     * Creates an intent with a String UUID of the Connection we want to connect.
     * @param context The context to launch the intent from.
     * @param uuid The String UUID of the Connection we want to connect.
     */
    public TcpIpConnectIntent(Context context, String uuid) {
        super(context, ConnectionService.class);
        Preconditions.checkNotNull(uuid);

        setAction(ACTION_TCPIP_CONNECT);
        putExtra(CONNECTION_TYPE, CONNECTION_TYPE_TCPIP);
        putExtra(CONNECTION_UUID, uuid);
    }
}
