package org.tec_hub.tecuniversalcomm.data.connection.intents;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.tec_hub.tecuniversalcomm.data.connection.Connection;

import java.util.UUID;

/**
 * Created by Nick Mosher on 4/30/15.
 * Intent that is broadcast by the ConnectionService whenever data is received
 * over some Connection.  The details of the Connection and the data received
 * is bundled into this intent to be received by some part of the program.
 *
 * @author Nick Mosher, nicholastmosher@gmail.com, https://github.com/nicholastmosher
 */
public class DataReceiveIntent extends Intent implements ConnectionIntent {

    private Context mContext;

    /**
     * Builds the intent from the launching context, the class we wish
     * to send data to, the details of the connection received from, and
     * the data received.
     * @param context The context to send the intent from.
     * @param target The class to send this intent to.
     * @param connection The Connection we received data from.
     * @param data The data received.
     */
    public DataReceiveIntent(Context context, Class target, Connection connection, byte[] data) {
        this(context, target, connection.getUUID(), connection.getConnectionType(), data);
    }

    /**
     * Builds the intent from the launching context, the class we wish
     * to send data to, the details of the connection received from, and
     * the data received.
     * @param context The context to send the intent from.
     * @param target The class to send this intent to.
     * @param uuid The UUID of the Connection received from.
     * @param data The data received.
     */
    public DataReceiveIntent(Context context, Class target, UUID uuid, byte[] data) {
        this(context, target, uuid.toString(), data);
    }

    /**
     * Builds the intent from the launching context, the class we wish
     * to send data to, the details of the connection received from, and
     * the data received.
     * @param context The context to send the intent from.
     * @param target The class to send this intent to.
     * @param uuid The String UUID of the Connection received from.
     * @param data The data received.
     */
    public DataReceiveIntent(Context context, Class target, String uuid, byte[] data) {
        this(context, target, uuid, Connection.getConnection(uuid).getConnectionType(), data);
    }

    /**
     * Builds the intent from the launching context, the class we wish
     * to send data to, the details of the connection received from, and
     * the data received.
     * @param context The context to send the intent from.
     * @param target The class to send this intent to.
     * @param uuid The String UUID of the Connection received from.
     * @param type The type of Connection received from.
     * @param data The data received.
     */
    public DataReceiveIntent(Context context, Class target, String uuid, String type, byte[] data) {
        super(context, target);

        mContext = context;
        setAction(ACTION_RECEIVED_DATA);
        putExtra(CONNECTION_TYPE, type);
        putExtra(CONNECTION_UUID, uuid);
        putExtra(RECEIVED_DATA, data);
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
