package org.tec_hub.tecuniversalcomm.intents;

import android.content.Context;
import android.content.Intent;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.TcpIpConnection;

/**
 * Created by Nick Mosher on 9/22/15.
 */
public class TcpIpDiscoveredIntent extends Intent implements TECIntent { //TODO this may not be a permanent thing.

    public TcpIpDiscoveredIntent(Context context, Class<?> cls, TcpIpConnection connection) {
        super(context, cls);
        Preconditions.checkNotNull(connection);

        setAction(ACTION_TCPIP_DISOVERED);
        putExtra(CONNECTION_TYPE, CONNECTION_TYPE_TCPIP);
        putExtra(TCPIP_CONNECTION_DATA, connection);
    }
}