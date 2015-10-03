package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;

import java.util.UUID;

/**
 * Created by Nick Mosher on 9/15/15.
 */
public class TcpIpSendIntent extends Intent implements TECIntent {

    /**
     * Creates an intent with the UUID of the Connection to send data over.
     * @param context The context to launch the intent from.
     * @param uuid The UUID of the Connection to send data over.
     * @param data The data being sent over the Connection.
     */
    public TcpIpSendIntent(Context context, UUID uuid, byte[] data) {
        this(context, uuid.toString(), data);
    }

    /**
     * Creates an intent with the String UUID of the Connection to send data over.
     * @param context The context to launch the intent from.
     * @param uuid The String UUID of the Connection to send data over.
     * @param data The data being sent over the Connection.
     */
    public TcpIpSendIntent(Context context, String uuid, byte[] data) {
        super(context, ConnectionService.class);
        Preconditions.checkNotNull(uuid);

        setAction(ACTION_TCPIP_SEND_DATA);
        putExtra(CONNECTION_TYPE, CONNECTION_TYPE_TCPIP);
        putExtra(CONNECTION_UUID, uuid);
        putExtra(TCPIP_TO_SEND_DATA, data);
    }
}
