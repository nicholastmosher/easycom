package org.tec_hub.tecuniversalcomm.data.connection;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.intents.BluetoothConnectIntent;
import org.tec_hub.tecuniversalcomm.intents.BluetoothDisconnectIntent;
import org.tec_hub.tecuniversalcomm.intents.TECIntent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Nick Mosher on 4/16/15.
 */
public class BluetoothConnection extends Connection implements Parcelable {

    /**
     * UUID used for connecting to Serial boards.
     */
    public static final UUID BLUETOOTH_SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * Required for Parcelable framework.
     */
    public static final Parcelable.Creator<BluetoothConnection> CREATOR = new Parcelable.Creator<BluetoothConnection>() {
        public BluetoothConnection createFromParcel(Parcel in) {
            return new BluetoothConnection(in);
        }

        public BluetoothConnection[] newArray(int size) {
            return new BluetoothConnection[size];
        }
    };

    /**
     * Static Map is used to store object references to BluetoothSockets, since
     * BluetoothSockets do not transmit through the Parcelable framework well.
     * This connection's UUID is used as the key.
     */
    private static Map<UUID, BluetoothSocket> sockets = new HashMap<>();

    /**
     * The MAC Address of the remote bluetooth device for this Connection.
     */
    private final String mBluetoothAddress;

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
        mBluetoothAddress = Preconditions.checkNotNull(address);
    }

    /**
     * Constructs a BluetoothConnection from a Parcel.  This happens when passing
     * objects across the Android framework e.g. through intents.
     * @param in The input Parcel to recreate the BluetoothConnection from.
     */
    public BluetoothConnection(Parcel in) {
        super(Preconditions.checkNotNull(in));
        mBluetoothAddress = Preconditions.checkNotNull(in.readString());
        mBluetoothSocket = sockets.get(mUUID);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(mBluetoothAddress);
        sockets.put(mUUID, mBluetoothSocket);
    }

    /**
     * Send connect request to BluetoothConnectionService to open a BluetoothConnection
     * using this object's data.
     * @param context The context to send the intent to launch the Service.
     */
    public void connect(Context context) {
        if(!isConnected()) {

            //Build intent with this connection data to send to service
            BluetoothConnectIntent connectIntent = new BluetoothConnectIntent(context, this);

            //Send intent through LocalBroadcastManager
            LocalBroadcastManager.getInstance(context).sendBroadcast(connectIntent);
        }
    }

    /**
     * Send disconnect request to BluetoothConnectionService to close a BluetoothConnection
     * using this object's data.
     * @param context The context to send the intent to launch the Service.
     */
    public void disconnect(Context context) {
        if(isConnected()) {

            //Build intent with this connection data to send to service
            BluetoothDisconnectIntent disconnectIntent = new BluetoothDisconnectIntent(context, this);

            //Send intent through LocalBroadcastManager
            LocalBroadcastManager.getInstance(context).sendBroadcast(disconnectIntent);
        }
    }

    /**
     * Tells whether this BluetoothConnection is actively connected.
     * @return True if connected, false otherwise.
     */
    public boolean isConnected() {
        if(mBluetoothSocket != null) {
            if(!mBluetoothSocket.isConnected()) {
                try {
                    mBluetoothSocket.close();
                } catch(IOException e) {
                    System.out.println("Bluetooth socket not connected; error closing socket!");
                    e.printStackTrace();
                }
            }
            return mBluetoothSocket.isConnected();
        }
        return false;
    }

    /**
     * Retrieves the Output Stream if this Connection is connected and
     * the Output Stream is not null.
     * @throws java.lang.IllegalStateException If not connected.
     * @return The OutputStream to the remote bluetooth device.
     */
    public OutputStream getOutputStream() throws IllegalStateException {
        if(isConnected()) {
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
     * Retrieves the Input Stream if this Connection is connected and
     * the Input Stream is not null.
     * @throws java.lang.IllegalStateException If not connected.
     * @return The InputStream from the remote bluetooth device.
     */
    public InputStream getInputStream() throws IllegalStateException {
        if(isConnected()) {
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
     * Gets the Bluetooth MAC Address of the remote device of this BluetoothConnection.
     * @return A Bluetooth MAC Address.
     */
    public String getAddress() {
        return mBluetoothAddress;
    }

    /**
     * Assigns the BluetoothSocket for this BluetoothConnection.
     * @param socket New BluetoothSocket.
     */
    void setBluetoothSocket(BluetoothSocket socket) {
        Preconditions.checkNotNull(socket);
        mBluetoothSocket = socket;
        sockets.put(mUUID, mBluetoothSocket);
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
}