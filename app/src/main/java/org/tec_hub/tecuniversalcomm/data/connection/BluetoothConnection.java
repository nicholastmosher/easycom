package org.tec_hub.tecuniversalcomm.data.connection;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.data.connection.intents.BluetoothConnectIntent;
import org.tec_hub.tecuniversalcomm.data.connection.intents.BluetoothDisconnectIntent;
import org.tec_hub.tecuniversalcomm.data.connection.intents.BluetoothSendIntent;
import org.tec_hub.tecuniversalcomm.data.connection.intents.ConnectionIntent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Nick Mosher on 4/16/15.
 */
public class BluetoothConnection extends Connection {

    /**
     * UUID used for connecting to Serial boards.
     */
    public static final UUID BLUETOOTH_SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * The MAC Address of the remote bluetooth device for this Connection.
     */
    private String mAddress;

    /**
     * The Bluetooth Socket used to communicate to the remote device.
     */
    private BluetoothSocket mBluetoothSocket;

    /**
     * Constructs a BluetoothConnection from a name and bluetooth MAC address.
     * @param name The name of this BluetoothConnection.
     * @param address The MAC Address of the remote device to connect to.
     */
    public BluetoothConnection(String name, String address) {
        super(Preconditions.checkNotNull(name));
        mAddress = Preconditions.checkNotNull(address);
    }

    /**
     * No-argument constructor made private so that Gson can correctly
     * build this object and then populate the members with Json data.
     */
    protected BluetoothConnection() {
        super();
        mAddress = null;
    }

    /**
     * Send connect request to ConnectionService to open a BluetoothConnection
     * using this object's data.
     * @param context The context to send the intent to launch the Service.
     */
    public void connect(Context context) {
        if(!(getStatus().equals(Status.Connected))) {

            //Build intent with this connection data to send to service
            BluetoothConnectIntent connectIntent = new BluetoothConnectIntent(context, mUUID);

            //Send intent through LocalBroadcastManager
            LocalBroadcastManager.getInstance(context).sendBroadcast(connectIntent);

            //Indicate that this connection's status is now "connecting".
            mStatus = Status.Connecting;
        }
    }

    /**
     * Send disconnect request to ConnectionService to close a BluetoothConnection
     * using this object's data.
     * @param context The context to send the intent to launch the Service.
     */
    public void disconnect(Context context) {
        if(getStatus().equals(Status.Connected)) {

            //Build intent with this connection data to send to service
            BluetoothDisconnectIntent disconnectIntent = new BluetoothDisconnectIntent(context, mUUID);

            //Send intent through LocalBroadcastManager
            LocalBroadcastManager.getInstance(context).sendBroadcast(disconnectIntent);
        }
    }

    /**
     * Tells whether this BluetoothConnection is actively connected.
     * @return True if connected, false otherwise.
     */
    public Status getStatus() {

        //If we know we're trying to connect to something.
        if(mStatus.equals(Status.Connecting)) return Status.Connecting;

        //If not in the process of connecting, verify active connections.
        if(mBluetoothSocket != null) {
            if(!mBluetoothSocket.isConnected()) {
                try {
                    //Closing a socket really "should" never throw an error unless it's FUBAR.
                    mBluetoothSocket.close();
                } catch(IOException e) {
                    System.out.println("Bluetooth socket not connected; error closing socket!");
                    e.printStackTrace();
                }
            }
            return mBluetoothSocket.isConnected() ? Status.Connected : Status.Disconnected;
        }
        return Status.Disconnected;
    }

    /**
     * Convenience method for use with intent extra "CONNECTION_TYPE".
     * @return The string "connection type" as defined by ConnectionIntent.
     */
    public String getConnectionType() {
        return ConnectionIntent.CONNECTION_TYPE_BLUETOOTH;
    }

    /**
     * Retrieves the Input Stream if this Connection is connected and
     * the Input Stream is not null.
     * @throws java.lang.IllegalStateException If not connected.
     * @return The InputStream from the remote bluetooth device.
     */
    public InputStream getInputStream() throws IllegalStateException {
        if(getStatus().equals(Status.Connected)) {
            try {
                return mBluetoothSocket.getInputStream();
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Connection is not active!");
        }
        return null;
    }

    /**
     * Retrieves the Output Stream if this Connection is connected and
     * the Output Stream is not null.
     * @throws java.lang.IllegalStateException If not connected.
     * @return The OutputStream to the remote bluetooth device.
     */
    public OutputStream getOutputStream() throws IllegalStateException {
        if(getStatus().equals(Status.Connected)) {
            try {
                return mBluetoothSocket.getOutputStream();
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Connection is not active!");
        }
        return null;
    }

    /**
     * Sends the given data over this connection.
     * @param context The context to send the intent from.
     * @param data The data to send.
     */
    public void sendData(Context context, byte[] data) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new BluetoothSendIntent(context, getUUID(), data));
    }

    /**
     * Gets the Bluetooth MAC Address of the remote device of this BluetoothConnection.
     * @return A Bluetooth MAC Address.
     */
    public String getAddress() {
        return mAddress;
    }

    /**
     * Assigns the BluetoothSocket for this BluetoothConnection.
     * @param socket New BluetoothSocket.
     */
    void setBluetoothSocket(BluetoothSocket socket) {
        Preconditions.checkNotNull(socket);
        mBluetoothSocket = socket;
    }

    /**
     * Gets this BluetoothConnection's BluetoothSocket if it exists.
     * @throws java.lang.NullPointerException If BluetoothSocket is null.
     * @return This BluetoothSocket.
     */
    public BluetoothSocket getBluetoothSocket() throws NullPointerException {
        if(mBluetoothSocket != null) {
            return mBluetoothSocket;
        } else {
            throw new NullPointerException("Bluetooth Socket is null!");
        }
    }

    @Override
    public String toString() {
        return mName + ", " + mAddress;
    }
}