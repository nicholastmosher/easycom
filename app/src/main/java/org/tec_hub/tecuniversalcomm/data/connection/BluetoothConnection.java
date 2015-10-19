package org.tec_hub.tecuniversalcomm.data.connection;

import android.bluetooth.BluetoothSocket;
import android.content.Context;

import org.tec_hub.tecuniversalcomm.R;
import org.tec_hub.tecuniversalcomm.data.connection.intents.ConnectionIntent;
import org.tec_hub.tecuniversalcomm.data.connection.intents.DataSendIntent;

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
     *
     * @param name    The name of this BluetoothConnection.
     * @param address The MAC Address of the remote device to connect to.
     */
    public BluetoothConnection(String name, String address) {
        super(name);
        if(address == null) {
            throw new NullPointerException("Bluetooth address is null!");
        }
        mAddress = address;
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
     * Tells whether this BluetoothConnection is actively connected.
     *
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
     *
     * @return The string "connection type" as defined by ConnectionIntent.
     */
    public String getConnectionType() {
        return ConnectionIntent.CONNECTION_TYPE_BLUETOOTH;
    }

    /**
     * Retrieves the Input Stream if this Connection is connected and
     * the Input Stream is not null.
     *
     * @return The InputStream from the remote bluetooth device.
     * @throws java.lang.IllegalStateException If not connected.
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
     *
     * @return The OutputStream to the remote bluetooth device.
     * @throws java.lang.IllegalStateException If not connected.
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

    @Override
    public int getImageResourceId() {
        return R.drawable.ic_bluetooth_black_48dp;
    }

    /**
     * Gets the Bluetooth MAC Address of the remote device of this BluetoothConnection.
     *
     * @return A Bluetooth MAC Address.
     */
    public String getAddress() {
        return mAddress;
    }

    /**
     * Assigns the BluetoothSocket for this BluetoothConnection.
     *
     * @param socket New BluetoothSocket.
     */
    void setBluetoothSocket(BluetoothSocket socket) {
        if(socket == null) {
            System.out.println("Socket is null!");
            return;
        }
        mBluetoothSocket = socket;
    }

    /**
     * Gets this BluetoothConnection's BluetoothSocket if it exists.
     *
     * @return This BluetoothSocket.
     * @throws java.lang.NullPointerException If BluetoothSocket is null.
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