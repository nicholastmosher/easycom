package org.tec_hub.tecuniversalcomm.data.connection.intents;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.tec_hub.tecuniversalcomm.data.connection.Connection;
import org.tec_hub.tecuniversalcomm.data.connection.ConnectionService;

import java.util.UUID;

/**
 * Created by Nick Mosher on 10/15/15.
 * This intent is used to initiate the disconnecting process for any Connection.
 * The supplied reference to the Connection is boiled down to UUID and Type
 * to pass through the intent system over to the ConnectionService, where it
 * is then fetched from the static map in Connection.
 */
public class DisconnectIntent extends Intent implements ConnectionIntent {

    private Context mContext;

    /**
     * Builds the intent from the launching context and the connection
     * we wish to disconnect from.
     * @param context The context to send the intent from.
     * @param connection The connection we want to connect.
     */
    public DisconnectIntent(Context context, Connection connection) {
        this(context, connection.getUUID(), connection.getConnectionType());
    }

    /**
     * Builds the intent from the launching context and the UUID of the
     * connection we wish to disconnect from.
     * @param context The context to send the intent from.
     * @param uuid The UUID of the connection we want to connect.
     */
    public DisconnectIntent(Context context, UUID uuid) {
        this(context, uuid.toString());
    }

    /**
     * Builds the intent from the launching context and the String UUID of
     * the connection we wish to disconnect from.  This fetches the Connection's
     * Type from the static map in Connection for reference in the next
     * constructor.
     * @param context The context to send the intent from.
     * @param uuid The UUID of the connection we want to connect.
     */
    public DisconnectIntent(Context context, String uuid) {
        this(context, uuid, Connection.getConnection(uuid).getConnectionType());
    }

    /**
     * Builds the intent from the launching context, String UUID, and String
     * Type of the connection we wish to disconnect from.
     * @param context The context to send the intent from.
     * @param uuid The String UUID of the connection we want to connect.
     * @param type The String Type of the connection we want to connect.
     */
    public DisconnectIntent(Context context, String uuid, String type) {
        super(context, ConnectionService.class);

        mContext = context;
        setAction(ACTION_DISCONNECT);
        putExtra(CONNECTION_TYPE, type);
        putExtra(CONNECTION_UUID, uuid);
    }

    /**
     * Sends this intent using Android's global broadcast system.
     */
    public void send() {
        mContext.sendBroadcast(this);
    }

    /**
     * Sends this intent using a broadcast system local to this application.
     */
    public void sendLocal() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(this);
    }
}
