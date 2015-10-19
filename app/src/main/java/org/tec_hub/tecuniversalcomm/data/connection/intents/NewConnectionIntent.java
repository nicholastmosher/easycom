package org.tec_hub.tecuniversalcomm.data.connection.intents;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.tec_hub.tecuniversalcomm.data.connection.Connection;

import java.util.UUID;

/**
 * Created by Nick Mosher on 9/22/15.
 * Intent used to notify when a new Connection is created.
 *
 * @author Nick Mosher, nicholastmosher@gmail.com, https://github.com/nicholastmosher
 */
public class NewConnectionIntent extends Intent implements ConnectionIntent {

    private Context mContext;

    /**
     * Builds the intent from a launching intent, the class we wish to
     * notify about the new connection, and the details of the connection
     * itself.
     * @param context The context to send the intent from.
     * @param target The class to send this intent to.
     * @param connection The details of the new connection.
     */
    public NewConnectionIntent(Context context, Class target, Connection connection) {
        this(context, target, connection.getUUID(), connection.getConnectionType());
    }

    /**
     * Builds the intent from a launching intent, the class we wish to
     * notify about the new connection, and the details of the connection
     * itself.
     * @param context The context to send the intent from.
     * @param target The class to send this intent to.
     * @param uuid The UUID of the new connection.
     */
    public NewConnectionIntent(Context context, Class target, UUID uuid) {
        this(context, target, uuid.toString());
    }

    /**
     * Builds the intent from a launching intent, the class we wish to
     * notify about the new connection, and the details of the connection
     * itself.
     * @param context The context to send the intent from.
     * @param target The class to send this intent to.
     * @param uuid The String UUID of the new connection.
     */
    public NewConnectionIntent(Context context, Class target, String uuid) {
        this(context, target, uuid, Connection.getConnection(uuid).getConnectionType());
    }

    /**
     * Builds the intent from a launching intent, the class we wish to
     * notify about the new connection, and the details of the connection
     * itself.
     * @param context The context to send the intent from.
     * @param target The class to send this intent to.
     * @param uuid The String UUID of the new connection.
     * @param type The type of the new connection.
     */
    public NewConnectionIntent(Context context, Class target, String uuid, String type) {
        super(context, target);

        mContext = context;
        setAction(ACTION_NEW_CONNECTION);
        putExtra(CONNECTION_TYPE, type);
        putExtra(CONNECTION_UUID, uuid);
    }

    /**
     * Sends this intent using Android's global broadcast system.
     */
    @Override
    public void send() {
        mContext.sendBroadcast(this);
    }

    /**
     * Sends this intent using a broadcast system local to this application.
     */
    @Override
    public void sendLocal() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(this);
    }
}
