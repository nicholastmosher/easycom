package org.tec_hub.tecuniversalcomm.connection;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.base.Preconditions;

import org.tec_hub.tecuniversalcomm.TECIntent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Nick Mosher on 4/16/15.
 */
public class BluetoothConnection extends Connection implements Parcelable {

    public static final UUID BLUETOOTH_SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final Parcelable.Creator<BluetoothConnection> CREATOR = new Parcelable.Creator<BluetoothConnection>() {
        public BluetoothConnection createFromParcel(Parcel in) {
            return new BluetoothConnection(in);
        }

        public BluetoothConnection[] newArray(int size) {
            return new BluetoothConnection[size];
        }
    };

    private final String mBluetoothAddress;
    private BluetoothSocket mBluetoothSocket;

    private Intent mConnectIntent;
    private Intent mDisconnectIntent;

    public BluetoothConnection(String name, String address) {
        super(Preconditions.checkNotNull(name));
        mBluetoothAddress = Preconditions.checkNotNull(address);
    }

    public BluetoothConnection(Parcel in) {
        super(Preconditions.checkNotNull(in));
        mBluetoothAddress = Preconditions.checkNotNull(in.readString());
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(mBluetoothAddress);
    }

    public String getAddress() {
        return mBluetoothAddress;
    }

    public boolean isConnected() {
        if(mBluetoothSocket != null) {
            return mBluetoothSocket.isConnected();
        } else {
            return false;
        }
    }

    /**
     * Send connect request to BluetoothConnectionService to open a BluetoothConnection
     * using this object's data.
     * @param context The context to send the intent to launch the Service.
     */
    public void connect(Context context) {
        if(!isConnected()) {

            //Build intent with this connection data to send to service
            mConnectIntent = new Intent(context, BluetoothConnectionService.class);
            mConnectIntent.setAction(TECIntent.ACTION_BLUETOOTH_CONNECT);
            mConnectIntent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, this);

            //Send intent through LocalBroadcastManager
            LocalBroadcastManager.getInstance(context).sendBroadcast(mConnectIntent);
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
            mDisconnectIntent = new Intent(context, BluetoothConnectionService.class);
            mDisconnectIntent.setAction(TECIntent.ACTION_BLUETOOTH_DISCONNECT);
            mDisconnectIntent.putExtra(TECIntent.BLUETOOTH_CONNECTION_DATA, this);

            //Send intent through LocalBroadcastManager
            LocalBroadcastManager.getInstance(context).sendBroadcast(mDisconnectIntent);
        }
    }

    /**
     * Retrieves the Output Stream if this Connection is connected and
     * the Output Stream is not null.
     * @return The OutputStream to the remote bluetooth device.
     */
    public OutputStream getOutputStream() {
        if(isConnected()) {
            try {
                return mBluetoothSocket.getOutputStream();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Retrieves the Input Stream if this Connection is connected and
     * the Input Stream is not null.
     * @return The InputStream from the remote bluetooth device.
     */
    public InputStream getInputStream() {
        if(isConnected()) {
            try {
                return mBluetoothSocket.getInputStream();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Assigns the BluetoothSocket for this BluetoothConnection.
     * @param bluetoothSocket
     */
    void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        Preconditions.checkNotNull(bluetoothSocket);
        mBluetoothSocket = bluetoothSocket;
    }

    /**
     * Gets this BluetoothConnection's BluetoothSocket if it exists.
     * @return This BluetoothSocket.
     */
    public BluetoothSocket getBluetoothSocket() {
        if(mBluetoothSocket != null) {
            return mBluetoothSocket;
        }
        return null;
    }
}